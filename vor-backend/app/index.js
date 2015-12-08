'use strict';
const path = require('path');
const express = require('express');
const socketIO = require('socket.io');
const Rx = require('rx');
const redis = require('redis');
const { BEACONS, MESSAGE_TTL } = require('config');
const Cache = require('app/cache');
const Location = require('app/location');
const viewRoute = require('app/views/routes');
const views = require('app/views');
const utils = require('app/utils');


// init app setup
const app = express();
const router = express.Router();


// init cache storage
const cacheClient = () => {
  let redisUrl = process.env.REDIS_URL;
  if (redisUrl) {
    return redis.createClient(redisUrl);
  } else {
    return redis.createClient();
  }
};
const appCache = new Cache(cacheClient(), MESSAGE_TTL);


// init location module
const location = new Location(BEACONS);


// set up socket.IO
app.io = socketIO();
app.io.on('error', error => console.log(`Socket connection error: ${error}`));

// helpers for sources
const observableFromSocketEvent = socket => event => Rx.Observable.fromEvent(socket, event);
const locationSource$ = source => {
  return location.fromDeviceStream(source)
    .doOnNext(location => app.io.emit('location', location))
    .doOnError(utils.logError(error =>`Location stream error:${error}`));
};
const initSource$ = source => {
  return appCache.getInitData(source)
    .doOnError(utils.logError(error => `Stream error:${error}`));
};
const messageSource$ = source => {
  return appCache.set(source)
    .doOnNext(message => app.io.emit('stream', [message]))
    .doOnError(utils.logError(error => `Stream error:${error}`));
};
app.io.on('connection', socket => {
  const observableFor = observableFromSocketEvent(socket);
  locationSource$(observableFor('beacon')).subscribe();
  initSource$(observableFor('init')).subscribe(messages => socket.emit('init', messages));
  messageSource$(observableFor('message')).subscribe();
});


// TODO: At the moment Arduinos' have limited websocket support. Remove this route if changes.
const messageRouteSource = message => Rx.Observable.from([message]);
const messageRoute = router.post('/messages', (req, res) => {
  console.log('console', req.body);
  messageSource$(messageRouteSource(req.body))
    .subscribe(
      success => res.send('OK'),
      error => res.error(`Error: ${error}`)
    );
});
app.use('/messages', messageRoute);


// the test page
app.use('/', viewRoute(router));
views.renderTestPage(app);


module.exports = app;
