'use strict';
const path = require('path');
const express = require('express');
const bodyParser = require('body-parser');
const logger = require('morgan');
const socketIO = require('socket.io');
const Rx = require('rx');
const redis = require('redis');
const expressRedisCache = require('express-redis-cache');
const { BEACONS, CACHE_PREFIX, CACHE_TTL } = require('config');
const Location = require('app/location');
const viewRoute = require('app/views/routes');
const views = require('app/views');
const utils = require('app/utils');

// set up app
const app = express();
const router = express.Router();
app.use(bodyParser.urlencoded({extended: false}));
app.use(bodyParser.text({type: '*/*'}));
app.use(logger('dev'));
// set up socket.IO
app.io = socketIO();
app.io.on('error', utils.logError(error => `Socket connection error: ${error}`));

// set up cache storage
const cacheClient = () => {
  let redisUrl = process.env.REDIS_URL;
  if (redisUrl) {
    return redis.createClient(redisUrl);
  } else {
    return redis.createClient();
  }
};
const cache = expressRedisCache({ client: cacheClient(), prefix: CACHE_PREFIX });

// listen socket connections
const socketConnectionSource$ = Rx.Observable.fromEvent(app.io.sockets, 'connection');

// listen socket beacons
const socketBeaconSource$ = socketConnectionSource$
  .flatMap(socket => Rx.Observable.fromEvent(socket, 'beacon'));

// listen socket messages
const socketMessageSource$ = socketConnectionSource$
  .flatMap(socket => Rx.Observable.fromEvent(socket, 'message'));

// listen socket init
const socketInitSource$ = socketConnectionSource$
  .flatMap(socket => Rx.Observable.fromEvent(
      socket,
      'init',
      event => socket) // we need socket to emit cache content only to one client
    );

// Post interface for messages
// TODO: At the moment Arduinos' have limited websocket support. Remove when unnecessary.
const postMessageSubject = new Rx.Subject();
const postMessageRoute = router.post('/messages', (req, res) => postMessageSubject.onNext([req,res]));
const postMessageSource$ =
  postMessageSubject
    .doOnNext(([req, res]) => res.send('OK'))
    .doOnError(([err, req, res]) => res.status(500).send(`Error: ${error}`))
    .map(([req, res]) => JSON.parse(req.body));

app.use('/messages', postMessageRoute);

// init location module
const location = new Location(BEACONS);
// subscribe location
location.fromDeviceStream(socketBeaconSource$)
  .subscribe(
    location => app.io.emit('location', location),
    utils.logError(error =>`Location stream error:${error}`)
  );

// subscribe init
const cacheGet = Rx.Observable.fromNodeCallback(cache.get, cache);
socketInitSource$
  .flatMap(socket => cacheGet().map(messages => [socket, messages]))
  .subscribe(([socket, messages]) => {
      utils.log(messages => `Init: fetched ${messages.length} messages from cache`)(messages);
      const messagesAsJson = messages.map(message => JSON.parse(message.body));
      socket.emit('init', messagesAsJson);
    },
    error => utils.logError(error => `Init error:${error}`)
  );

// subscribe messages
const cacheAdd = Rx.Observable.fromNodeCallback(cache.add, cache);
postMessageSource$
  .merge(socketMessageSource$)
  .flatMap(message => {
    const key = message.id ? `${message.type}:${message.id}` : `${message.type}`;
    const data = JSON.stringify(message);
    const config = { expire: CACHE_TTL, type: 'json' };
    return cacheAdd(key, data, config)
      .doOnNext(([key, data, status]) => {
        utils.log(message => `Message: cache added ${message}`)(data.body);
        const messageAsJson = JSON.parse(data.body);
        app.io.emit('stream', [messageAsJson]);
      })
      .doOnError(error => utils.logError(error => `Message error:${error}`));
  })
  .subscribe();

// the test page
app.use('/', viewRoute(router));
views.renderTestPage(app);

module.exports = app;
