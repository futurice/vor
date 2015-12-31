'use strict';
const io = require('socket.io-client');
const sinon = require('sinon');
const envs = require('envs');

const socketURL = `http://0.0.0.0:${process.env.PORT}`;
var cacheClient = null;

const helpers = {

  TEST_MESSAGE: {
    type: 'test-room',
    id: '1a',
    text: 'text'
  },

  createSocketConnection: () => io.connect(socketURL, {
    transports: ['websocket'],
    'force new connection': true
  }),

  setupCache: () => {
    envs('EX_RE_CA_HOST', '');
    envs('EX_RE_CA_PORT', '');
    envs('EX_RE_CA_PREFIX', '');

    if (cacheClient) {
      return cacheClient;
    } else {
      cacheClient = require('redis-mock').createClient();
      const redisStub = sinon.stub(require('redis'), 'createClient').returns(cacheClient);
      return cacheClient;
    }
  },

  flushCache: done => {
    cacheClient.flushdb(cleared => done()); // clear db for every test case
  }

};

module.exports = helpers;
