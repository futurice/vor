'use strict';
const path = require('path');
const express = require('express');
const bodyParser = require('body-parser');
const logger = require('morgan');
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
app.use(bodyParser.urlencoded({extended: false}));
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
const appCache = new Cache(cacheClient(), MESSAGE_TTL);


// init location module
const location = new Location(BEACONS);


// set up socket.IO
app.io = socketIO();
app.io.on('error', utils.logError(error => `Socket connection error: ${error}`));

// helpers for sources
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

// listen socket connections
app.io.on('connection', socket => {
  locationSource$(Rx.Observable.fromEvent(socket, 'beacon')).subscribe();
  initSource$(Rx.Observable.fromEvent(socket, 'init')).subscribe(messages => socket.emit('init', messages));
  messageSource$(Rx.Observable.fromEvent(socket, 'message')).subscribe();
});


// TODO: At the moment Arduinos' have limited websocket support. Remove this route if changes.
const messageRouteSource$ = message => Rx.Observable.from(message);
const messageRoute = router.post('/messages', (req, res) => {
  Rx.Observable.return(JSON.parse(JSON.parse(req.body)))
    .flatMap(json => messageSource$(messageRouteSource$([json])))
    .subscribe(
      success => res.send('OK'),
      error => res.status(300).send(`Error: ${error}`)
    );
});
app.use('/messages', bodyParser.text({type: '*/*'}), messageRoute);


// the test page
app.use('/', viewRoute(router));
views.renderTestPage(app);


module.exports = app;
