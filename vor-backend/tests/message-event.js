'use strict';
const should = require('should');
const assert = require('assert');
const helpers = require('./helpers/index');
const config = require('../config');
const cacheClient = helpers.setupCache();

describe('App: On message event', function () {

  before(() => {
    const app = require('../bin/www');
  });

  after(helpers.flushCache);

  it('should publish message', done => {
    const TEST_MESSAGE = helpers.TEST_MESSAGE;
    const clientA = helpers.createSocketConnection();
    const clientB = helpers.createSocketConnection();

    clientA.on('connect', () => {
      clientA.emit('message', TEST_MESSAGE);
    });

    clientA.on('stream', message => {
      should(message[0]).deepEqual(TEST_MESSAGE);
    });

    clientB.on('stream', message => {
      should(message[0]).deepEqual(TEST_MESSAGE);
      clientA.disconnect();
      clientB.disconnect();
      done();
    });

  });

  it('should add expiration time for message', done => {
    const TEST_MESSAGE = helpers.TEST_MESSAGE;
    cacheClient.ttl(`${TEST_MESSAGE.type}:${TEST_MESSAGE.id}`, (err, ttl) => {
      should(ttl).equal(config.MESSAGE_TTL.default);
      done();
    });

  });

});
