'use strict';
const io = require('socket.io-client');
const sinon = require('sinon');
const fakeRedis = require('fakeRedis');

const socketURL = `http://0.0.0.0:${process.env.PORT}`;
var cacheClient = null;

const helpers = {

  TEST_MESSAGE: {
    "type": "test-room",
    "id": "1a",
    "text": "text"
  },

  createSocketConnection: () => io.connect(socketURL, {
    transports: ['websocket'],
    'force new connection': true
  }),

  setupCache: () => {
    if (cacheClient) {
      return cacheClient;
    } else {
      cacheClient = fakeRedis.createClient();
      sinon.spy(cacheClient, 'set');
      const redisStub = sinon.stub(require('redis'), 'createClient').returns(cacheClient);
      return cacheClient;
    }
  },

  flushCache: done => {
    cacheClient.flushdb(err => done());
  }

};

module.exports = helpers;
