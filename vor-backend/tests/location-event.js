'use strict';
const should = require('should');
const assert = require('assert');
const helpers = require('./helpers/index');

describe('App: on message event "location"', function () {

  beforeEach(() => {
    helpers.setupCache();
    const app = require('../bin/www');
  });

  it('should publish user location', done => {
    /*
     Beacon locations (1,2,3) are in config/test.json
     ClientA location should be in the center: 2,2

     +---+---+---+
     |   |   |   |
     | 1 |   | 2 |
     |   |   |   |
     +-----------+
     |   |   |   |
     |   | A |   |
     |   |   |   |
     +-----------+
     |   |   |   |
     | 3 |   |   |
     |   |   |   |
     +---+---+---+
     */

    const EXPECTED_A_LOCATION_MESSAGE = {email: 'ClientA', type: 'location', floor: 1, x: 2, y: 2};
    const clientA = helpers.createSocketConnection();
    const clientB = helpers.createSocketConnection();

    clientA.on('connect', () => {
      clientA.emit('message',
      {
       type: 'beacon',
       email: 'ClientA',
       beacons: [
        {
          id: 1,
          distance: 1,
          floor: 1
        },
        {
          id: 2,
          distance: 1,
          floor: 1
        },
        {
          id: 3,
          distance: 1,
          floor: 1
        }
       ]});

      clientA.on('message', messageForClientA => {
        clientB.on('message', messageForClientB => {
          should(messageForClientA).deepEqual(EXPECTED_A_LOCATION_MESSAGE);
          should(messageForClientB).deepEqual(EXPECTED_A_LOCATION_MESSAGE);
          clientA.disconnect(); // always disconnect connection
          clientB.disconnect(); // always disconnect connection
          done();
        });
      });
    });

  });

});
