'use strict';
const should = require('should');
const assert = require('assert');
const helpers = require('./helpers/index');

describe('App: on message type "location"', function () {

  beforeEach(() => {
    helpers.setupCache();
    const app = require('../bin/www');
  });

  it('should publish user location', done => {

    const EXPECTED_A_LOCATION_MESSAGE = {email: 'ClientA', type: 'location', floor: 1, x: 2, y: 2};
    const clientA = helpers.createSocketConnection();
    const clientB = helpers.createSocketConnection();

    clientA.on('connect', () => {
      clientA.emit('message', EXPECTED_A_LOCATION_MESSAGE);
    });

    clientA.on('message', messageForClientA => {
      clientB.on('message', messageForClientB => {
        should(messageForClientA).deepEqual(EXPECTED_A_LOCATION_MESSAGE);
        should(messageForClientB).deepEqual(EXPECTED_A_LOCATION_MESSAGE);
        clientA.disconnect();
        clientB.disconnect();
        done();
      });
    });

  });

});
