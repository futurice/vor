'use strict';
const Rx = require('rx');
const redis = require('redis');
const expressRedisCache = require('express-redis-cache');

class Cache {

  constructor(cachePrefix, cacheTTL) {
    this.TTL = cacheTTL;
    const cacheClient = () => {
      let redisUrl = process.env.REDIS_URL;
      if (redisUrl) {
        return redis.createClient(redisUrl);
      } else {
        return redis.createClient();
      }
    };
    this.client = expressRedisCache({client: cacheClient(), prefix: cachePrefix});
  }

  store(message) {
    const cacheAdd = Rx.Observable.fromNodeCallback(this.client.add, this.client);
    const key = message.id ? `${message.type}:${message.id}` : `${message.type}`;
    const data = JSON.stringify(message);
    const config = {expire: this.TTL, type: 'json'};
    return cacheAdd(key, data, config)
      .map(([key, data, status]) => JSON.parse(data.body));
  }

  getAll() {
    const cacheGet = Rx.Observable.fromNodeCallback(this.client.get, this.client);
    return cacheGet()
      .map(messagesAsString => {
        return messagesAsString.map(message => JSON.parse(message.body));
      });
  }

}

module.exports = Cache;
