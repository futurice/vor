'use strict';
const should = require('should');
const assert = require('assert');
const helpers = require('./helpers/index');

describe('App: On beacon event', function () {

  before(() => {
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

    const CLIENT_A_LOCATION = {email: 'ClientA', x: 2, y: 2};
    const clientA = helpers.createSocketConnection();
    const clientB = helpers.createSocketConnection();

    clientA.on('connect', () => {
      clientA.emit('beacon', {email: 'ClientA', id: 1, distance: 1, floor: 1});
      clientA.emit('beacon', {email: 'ClientA', id: 2, distance: 1, floor: 1});
      clientA.emit('beacon', {email: 'ClientA', id: 3, distance: 1, floor: 1});

      clientA.on('location', message => {
        should(message).deepEqual(CLIENT_A_LOCATION);
        done();
      });

      clientA.disconnect();
    });

    clientB.on('location', message => {
      should(message).deepEqual(CLIENT_A_LOCATION);
      clientB.disconnect();
      done();
    });

  });

});
