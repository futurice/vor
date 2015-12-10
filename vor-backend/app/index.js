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

// listen socket connections
app.io.on('connection', socket => {
  location.fromDeviceStream(Rx.Observable.fromEvent(socket, 'beacon'))
    .subscribe(
      location => app.io.emit('location', location),
      utils.logError(error =>`Location stream error:${error}`)
  );

  Rx.Observable.fromEvent(socket, 'message')
    .flatMap(appCache.transaction.bind(appCache))
    .subscribe(
      message => app.io.emit('stream', [message]),
      error => utils.logError(error => `Message error:${error}`)
    );

  Rx.Observable.fromEvent(socket, 'init')
    .flatMap(appCache.getAllStream.bind(appCache))
    .subscribe(
      messages => socket.emit('init', messages),
      error => utils.logError(error => `Init error:${error}`)
    );
});


// TODO: At the moment Arduinos' have limited websocket support. Remove this route if changes.
const messageRoute = router.post('/messages', (req, res) => {
  Rx.Observable.return(JSON.parse(req.body))
    .flatMap(appCache.transaction.bind(appCache))
    .doOnNext(message => app.io.emit('stream', [message]))
    .doOnError(error => utils.logError(error => `Message error:${error}`))
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
