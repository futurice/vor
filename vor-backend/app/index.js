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


// common helper for cache set
const cacheSetSource$ = message => {
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

// listen socket connections
app.io.on('connection', socket => {
  location.fromDeviceStream(Rx.Observable.fromEvent(socket, 'beacon'))
    .subscribe(
      location => app.io.emit('location', location),
      utils.logError(error =>`Location stream error:${error}`)
  );

  Rx.Observable.fromEvent(socket, 'message')
    .flatMap(cacheSetSource$)
    .subscribe();

  Rx.Observable.fromEvent(socket, 'init')
    .flatMap(init => Rx.Observable.fromNodeCallback(cache.get, cache)())
    .subscribe(
      messages => {
        utils.log(messages => `Init: fetched ${messages.length} messages from cache`)(messages);
        const messagesAsJson = messages.map(message => JSON.parse(message.body));
        socket.emit('init', messagesAsJson);
      },
      error => utils.logError(error => `Init error:${error}`)
    );
});

// Post interface for messages
// TODO: At the moment Arduinos' have limited websocket support. Remove this route if websockets are used in all clients.
const messageRoute = router.post('/messages', (req, res) => {
  Rx.Observable.return(JSON.parse(req.body))
    .flatMap(cacheSetSource$)
    .subscribe(
      success => res.send('OK'),
      error => res.status(500).send(`Error: ${error}`)
    );
});
app.use('/messages', bodyParser.text({type: '*/*'}), messageRoute);


// the test page
app.use('/', viewRoute(router));
views.renderTestPage(app);


module.exports = app;
