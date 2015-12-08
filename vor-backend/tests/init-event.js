'use strict';
const should = require('should');
const assert = require('assert');
const helpers = require('./helpers/index');
const cacheClient = helpers.setupCache();

describe('App: On init event', function () {

  before(() => {
    const app = require('../bin/www');
  });

  afterEach(helpers.flushCache);

  it('should send messages from cache', done => {
    var messagesForClientA;
    var messagesForClientB;
    const TEST_MESSAGE = helpers.TEST_MESSAGE;
    const clientA = helpers.createSocketConnection();
    const clientB = helpers.createSocketConnection();

    cacheClient.set(`${TEST_MESSAGE.type}:${TEST_MESSAGE.id}`, JSON.stringify(TEST_MESSAGE));

    clientA.on('connect', () => {
      clientA.emit('init');
    });

    clientB.on('init', messages => {
      messagesForClientB = messages;
    });

    clientA.on('init', messages => {
      messagesForClientA = messages;
      should(messagesForClientA[0]).deepEqual(TEST_MESSAGE);
      should(messagesForClientB).equal(undefined);
      setTimeout(done, 10);
    });
  });

});
