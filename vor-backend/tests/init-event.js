'use strict';
const should = require('should');
const assert = require('assert');
const helpers = require('./helpers/index');
const sharedConfig = require('../config/shared');

const TEST_MESSAGE = helpers.TEST_MESSAGE;

describe('App: on "init" ', function () {

  beforeEach(() => {
    helpers.setupCache();
    const app = require('../bin/www');
  });

  afterEach(helpers.flushCache);

  it('should send data for single client(emitting "init")', done => {
    const clientA = helpers.createSocketConnection();
    const clientB = helpers.createSocketConnection();
    clientA.on('connect', () => {
      clientB.on('connect', () => {
        clientB.emit('message', TEST_MESSAGE);
        clientA.emit('init');
        clientA.on('init', initForClientA => {

          should(initForClientA.beacons).deepEqual(sharedConfig.BEACONS);
          should(initForClientA.messages[0]).deepEqual(TEST_MESSAGE);

          let initForClientB;
          clientB.on('init', init => initForClientB = init);
          setTimeout(() => {
            should(initForClientB).equal(undefined); // B will not get any init message
            clientA.disconnect(); // always disconnect connection
            clientB.disconnect(); // always disconnect connection
            done();
          }, 10);
        });
      });
    });
  });

});
