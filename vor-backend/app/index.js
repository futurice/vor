'use strict';
const Rx = require('rx');
const redis = require('redis');
const expressRedisCache = require('express-redis-cache');
const { CACHE_PREFIX, CACHE_TTL } = require('config/server');
const { BEACONS } = require('config/shared');
const Cache = require('app/cache');
const Location = require('app/location');
const viewRoute = require('app/views/routes');
const views = require('app/views');

module.exports = function (app, router, configs) {

  // listen socket connections
  const socketConnectionSource$ = Rx.Observable.fromEvent(app.io.sockets, 'connection');

  // listen socket messages
  const socketMessageSource$ = socketConnectionSource$
    .flatMap(socket => Rx.Observable.fromEvent(socket, 'message'));

  // split location messages
  let [ deviceSource$, messageSource$ ] = socketMessageSource$
    .partition(message => message.type === 'beacon');

  // set up location stream
  const location = new Location(configs.BEACONS);
  const locationSource$ = location.fromDeviceStream(deviceSource$);

  // listen socket 'init' messages
  const socketInitSource$ = socketConnectionSource$
    .flatMap(socket => Rx.Observable.fromEvent(
        socket,
        'init',
        event => socket) // we need socket to emit cache content only to one client
      ).share();

  // Post interface for messages
  // TODO: At the moment Arduinos' have limited websocket support. Remove when unnecessary.
  const postMessageSubject = new Rx.Subject();
  const postMessageRoute = router.post('/messages', (req, res) => {
    try {
      const json = JSON.parse(req.body);
      postMessageSubject.onNext(json);
      res.send('OK');
    }
    catch (error) {
      res.status(500).send(`Error: ${error}`);
    }
  });
  app.use('/messages', postMessageRoute);

  // set up cache storage
  const cache = new Cache(configs.CACHE_PREFIX, configs.CACHE_TTL);

  // subscribe init
  socketInitSource$
    .flatMap(socket => {
      return Rx.Observable.zip(Rx.Observable.return(socket), cache.getAll());
    })
    .subscribe(
      ([socket, messagesAsString]) => {
        console.log(`Server - fetched ${messagesAsString.length} messages from cache : ${new Date}`);
        const messages = messagesAsString.map(message => JSON.parse(message.body));
        socket.emit('init',
          {
            beacons: BEACONS, // send configured beacons data
            messages: messages
          });
      },
      error => console.error(`Error - init stream: ${error} : ${new Date}`)
  );

  // subscribe messages
  postMessageSubject
    .merge(messageSource$)
    .flatMap(message => cache.store(message))
    .do(message => console.log(`Server - stored ${message.type} : ${new Date}`))
    .merge(locationSource$) // merge location without storing it
    .subscribe(
      message => {
        app.io.emit('message', message);
        console.log(`Server - emit ${message.type} : ${new Date}`);
      },
      error => console.error(`Error - message stream: ${error} : ${new Date}`)
    );

  // the test page
  app.use('/', viewRoute(router));
  views.renderTestPage(app);

  return app;

};
