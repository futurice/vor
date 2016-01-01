'use strict';
const should = require('should');
const assert = require('assert');
const helpers = require('./helpers/index');

describe('App: on message event', function () {

  beforeEach(() => {
    helpers.setupCache();
    const app = require('../bin/www');
  });

  afterEach(helpers.flushCache);

  it('should publish message', done => {
    const TEST_MESSAGE = helpers.TEST_MESSAGE;
    const clientA = helpers.createSocketConnection();
    const clientB = helpers.createSocketConnection();

    clientA.on('connect', () => {
      clientA.emit('message', TEST_MESSAGE);
    });

    clientA.on('message', messageForClientA => {
      clientB.on('message', messageForClientB => {
        should(messageForClientA).deepEqual(TEST_MESSAGE);
        should(messageForClientB).deepEqual(TEST_MESSAGE);
        clientA.disconnect(); // always disconnect connection
        clientB.disconnect(); // always disconnect connection
        done();
      });
    });

  });

});
