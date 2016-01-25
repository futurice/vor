'use strict';
const Rx = require('rx');
const Cache = require('app/cache');
const viewRoute = require('app/views/routes');
const views = require('app/views');

module.exports = function (app, router, configs, sharedConfigs) {

  // listen socket connections
  const socketConnectionSource$ = Rx.Observable.fromEvent(app.io.sockets, 'connection');

  // listen socket messages
  const socketMessageSource$ = socketConnectionSource$
    .flatMap(socket => Rx.Observable.fromEvent(socket, 'message'));

  // split location messages
  let [ deviceSource$, messageSource$ ] = socketMessageSource$
    .partition(message => message.type === 'location');

  // Add timestamp to every location message.
  deviceSource$.subscribe(message => message.timestamp = Date.now());

  // listen socket 'init' messages
  const socketInitSource$ = socketConnectionSource$
    .flatMap(socket => Rx.Observable.fromEvent(
      socket,
      'init',
      event => socket // we need socket to emit cache content only to one client
    )
  );

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
      ([socket, messages]) => {
        socket.emit('init',
          {
            beacons: sharedConfigs.BEACONS, // send configured beacons data
            messages: messages
          });
        console.log(`Server - fetched ${messages.length} messages from cache : ${new Date}`);
      },
      error => console.error(`Error - init stream: ${error} : ${new Date}`)
    );

  // subscribe messages
  postMessageSubject
    .merge(messageSource$)
    .flatMap(message => cache.store(message))
    .do(message => console.log(`Server - stored ${message.type} : ${new Date}`))
    .merge(deviceSource$) // merge location without storing it
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
