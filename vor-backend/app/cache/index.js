'use strict';
const Rx = require('rx');
const url = require('url');
const utils = require('app/utils');

class Cache {

  constructor(client, messageExpirations) {
    this.client = client;
    this.client.on('error', utils.logError(error => `Cache error: cache connection error: ${error}`));
    this.messageExpirations = messageExpirations;
  }

  set(messageSource) {
    return messageSource
      .flatMap(message => {
        const expiresInSeconds = this.messageExpirations[message.type] || this.messageExpirations.default;
        const key = message.id ? `${message.type}:${message.id}` : `${message.type}`;
        const multi = this.client
          .multi()
          .set(key, JSON.stringify(message), utils.log(val => `Cache.set ${key}`))
          .expire(key, expiresInSeconds)
          .get(key, utils.log(value => `Cache.client.get ${key}`)); // use client method get to ensure atomic operation

          return Rx.Observable.fromNodeCallback(multi.exec, multi)()
            .map(([setStatus, ttlStatus, message]) => JSON.parse(message))
            .doOnError(utils.logError(error => `Cache error: Cache.set(${key}): ${error}`));
      });
  }

  getInitData(initSource) {
    return initSource
      .flatMap(event => {
        return this.keys()
          .flatMap(keys => keys.map(this.get.bind(this)))
          .flatMap(value => value)
          .bufferWithCount(1000)
          .doOnError(utils.logError(error => `Cache error: Cache.getInitData -> ${error}`));
      });
  }

  keys() {
    const observable = Rx.Observable.fromNodeCallback(this.client.keys, this.client);
    return observable('*')
      .map(utils.log(value => `Cache.keys fetched ${value}`))
      .doOnError(utils.logError(error => `Cache error: Cache.keys(): ${error}`));
  }

  get(key) {
    const observable = Rx.Observable.fromNodeCallback(this.client.get, this.client);
    return observable(key)
      .map(JSON.parse)
      .map(utils.log(val => `Cache.get ${key}`))
      .doOnError(utils.logError(error => `Cache error: Cache.get(${key}): ${error}`));
  }
}

module.exports = Cache;
