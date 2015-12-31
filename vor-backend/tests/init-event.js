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

  it('should send data from cache for one client requesting for it', done => {
    var messagesForClientA;
    var messagesForClientB;
    const TEST_MESSAGE = helpers.TEST_MESSAGE;
    const clientA = helpers.createSocketConnection();
    const clientB = helpers.createSocketConnection();

    clientA.on('connect', () => {
      clientB.on('connect', () => {
        clientB.emit('message', TEST_MESSAGE);
        clientA.emit('init');

        clientA.on('init', initObject => {
          clientB.on('init', messages => messagesForClientB = messages);
          messagesForClientA = initObject.messages;
          should(messagesForClientA[0]).deepEqual(TEST_MESSAGE);
          should(messagesForClientB).equal(undefined); // B will not get any init message
          setTimeout(done, 10); // wait for B's 'init' message which will never be happen
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
        done();
      });
    });
  });
});
