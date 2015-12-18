'use strict';
const should = require('should');
const assert = require('assert');
const helpers = require('./helpers/index');

describe('App: On message event', function () {

  before(() => {
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

    clientA.on('message', message => {
      should(message).deepEqual(TEST_MESSAGE);
    });

    clientB.on('message', message => {
      should(message).deepEqual(TEST_MESSAGE);
      clientA.disconnect();
      clientB.disconnect();
      done();
    });

  });

});
