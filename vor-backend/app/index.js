'use strict';
const path = require('path');
const express = require('express');
const socketIO = require('socket.io');
const Rx = require('rx');
const redis = require('redis');
const { BEACONS, MESSAGE_TTL } = require('config');
const Cache = require('app/cache');
const Location = require('app/location');
const views = require('app/views');
const utils = require('app/utils');


// init app setup
const app = express();


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
app.io.on('connection', socket => {
  const observableFor = observableFromSocketEvent(socket);
  publishLocation(observableFor('beacon'), app.io);
  publishMessage(observableFor('message'), app.io);
  sendInitData(observableFor('init'), socket);
});

const observableFromSocketEvent = socket => event => Rx.Observable.fromEvent(socket, event);

const publishLocation = (source, socket) => {
  location.fromDeviceStream(source)
    .subscribe(
      location => socket.emit('location', location),
      utils.logError(error =>`Location stream error:${error}`));
};

const publishMessage = (source, socket) => {
  appCache.set(source)
    .subscribe(
      value => socket.emit('stream', [value]),
      utils.logError(error => `Stream error:${error}`)
    );
};

const sendInitData = (source, socket) => {
  appCache.getInitData(source)
    .subscribe(
      messages => socket.emit('init', messages),
      utils.logError(error => `Init stream error:${error}`)
    );
};


// render test page
views.renderTestPage(app);


module.exports = app;
