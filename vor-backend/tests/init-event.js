'use strict';
const should = require('should');
const assert = require('assert');
const helpers = require('./helpers/index');
const sharedConfig = require('../config/shared');

describe('App: on "init" ', function () {

  before(() => {
    helpers.setupCache();
    const app = require('../bin/www');
  });

  afterEach(helpers.flushCache);

  it('should send data from cache', done => {
    var messagesForClientA;
    var messagesForClientB;
    const TEST_MESSAGE = helpers.TEST_MESSAGE;
    const clientA = helpers.createSocketConnection();
    const clientB = helpers.createSocketConnection();

    clientA.on('connect', () => {
      clientB.on('connect', () => {
        clientB.emit('message', TEST_MESSAGE);
        clientA.emit('init');

        clientB.on('init', messages => {
          messagesForClientB = messages
        });

        clientA.on('init', initObject => {
          messagesForClientA = initObject.messages;
          should(messagesForClientA[0]).deepEqual(TEST_MESSAGE);
          should(messagesForClientB).equal(undefined);
          setTimeout(done, 10);
        });
      });
    });
  });

  it('should send shared configurations', done => {
    const client = helpers.createSocketConnection();

    client.on('connect', () => {
      client.emit('init');

      client.on('init', initObject => {
        should(initObject.beacons).deepEqual(sharedConfig.BEACONS);
        setTimeout(done, 10);
      });
    });
  });
});
