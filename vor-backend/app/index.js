'use strict';
const path = require('path');
const express = require('express');
const bodyParser = require('body-parser');
const logger = require('morgan');
const socketIO = require('socket.io');
const Rx = require('rx');
const redis = require('redis');
const { BEACONS, CACHE_PREFIX, CACHE_TTL } = require('config');
const Location = require('app/location');
const viewRoute = require('app/views/routes');
const views = require('app/views');
const utils = require('app/utils');


// init app setup
const app = express();
const router = express.Router();
app.use(bodyParser.urlencoded({extended: false}));
app.use(bodyParser.text({type: '*/*'}));
app.use(logger('dev'));


// init cache storage
const cacheClient = () => {
  let redisUrl = process.env.REDIS_URL;
  if (redisUrl) {
    return redis.createClient(redisUrl);
  } else {
    return redis.createClient();
  }
};
const cache = require('express-redis-cache')({ client: cacheClient(), prefix: CACHE_PREFIX });

// init location module
const location = new Location(BEACONS);


// set up socket.IO
app.io = socketIO();
app.io.on('error', utils.logError(error => `Socket connection error: ${error}`));


// listen socket connections
const socketConnectionSource$ = Rx.Observable.fromEvent(app.io.sockets, 'connection');


// listen socket messages
const socketMessageSource$ = socketConnectionSource$
  .flatMap(socket => Rx.Observable.fromEvent(socket, 'message'));

// Post interface for messages
// TODO: At the moment Arduinos' have limited websocket support. Remove when unnecessary.
const messageRouteSource$ =
  Rx.Observable.fromCallback(router.post, router)('/messages')
    .doOnNext(([req, res]) => res.send('OK'))
    .doOnError(([err, req, res]) => res.status(500).send(`Error: ${error}`))
    .map(([req, res]) => JSON.parse(req.body));

// common helpers for cache set
const onMessageEvent = message => {
  const key = message.id ? `${message.type}:${message.id}` : `${message.type}`;
  const data = JSON.stringify(message);
  const config = { expire: CACHE_TTL, type: 'json' };
  return Rx.Observable.fromNodeCallback(cache.add, cache)(key, data, config)
    .doOnNext(([key, data, status]) => {
      utils.log(message => `Message: cache added ${message}`)(data.body);
      const messageAsJson = JSON.parse(data.body);
      app.io.emit('stream', [messageAsJson]);
    })
    .doOnError(error => utils.logError(error => `Message error:${error}`));
};

socketMessageSource$
  .flatMap(onMessageEvent)
  .subscribe();

messageRouteSource$
  .flatMap(onMessageEvent)
  .subscribe();

// listen socket beacons
const socketBeaconSource$ = socketConnectionSource$
  .flatMap(socket => Rx.Observable.fromEvent(socket, 'beacon'));

location.fromDeviceStream(socketBeaconSource$)
  .subscribe(
    location => app.io.emit('location', location),
    utils.logError(error =>`Location stream error:${error}`)
  );


// listen socket init
const socketInitSource$ = socketConnectionSource$
  .flatMap(socket => Rx.Observable.fromEvent(
      socket,
      'init',
      event => socket)
    );

socketInitSource$
  .flatMap(socket => {
    const observable = Rx.Observable.fromNodeCallback(cache.get, cache)();
    return observable.map(messages => [socket, messages]);
  })
  .subscribe(function([socket, messages]) {
      utils.log(messages => `Init: fetched ${messages.length} messages from cache`)(messages);
      const messagesAsJson = messages.map(message => JSON.parse(message.body));
      socket.emit('init', messagesAsJson);
    },
    error => utils.logError(error => `Init error:${error}`)
  );


// the test page
app.use('/', viewRoute(router));
views.renderTestPage(app);


module.exports = app;
