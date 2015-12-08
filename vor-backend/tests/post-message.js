'use strict';
const should = require('should');
const assert = require('assert');
const helpers = require('./helpers/index');
const config = require('../config');
var request = require('supertest');
const cacheClient = helpers.setupCache();

describe('App: On message post', function () {
  let app;

  beforeEach(function (done) {
    app = require('../bin/www');
    done();
  });

  after(helpers.flushCache);

  it('should publish message', done => {
    const TEST_MESSAGE = helpers.TEST_MESSAGE;
    const clientA = helpers.createSocketConnection();
    const clientB = helpers.createSocketConnection();

    request(app)
      .post('/messages')
      .send(TEST_MESSAGE)
      .expect(200)
      .end(function (err, res) {
        clientA.on('stream', message => {
          should(message[0]).deepEqual(TEST_MESSAGE);
        });

        clientB.on('stream', message => {
          should(message[0]).deepEqual(TEST_MESSAGE);
          clientA.disconnect();
          clientB.disconnect();

        });
      });

  });

});
