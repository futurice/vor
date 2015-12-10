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

  after(function(done) {
    helpers.flushCache(done);
  });

  it('should publish message', done => {
    const TEST_MESSAGE = '{"id":"1","type":"room","reserved":false,"temperature":10,"light":774,"dioxide":10,"noise":10}';
    const expectedResponse = JSON.parse(TEST_MESSAGE);
    const clientA = helpers.createSocketConnection();
    const clientB = helpers.createSocketConnection();
    request(app)
      .post('/messages')
      .set('Content-type', 'text/plain')
      .send(TEST_MESSAGE)
      .expect(200)
      .end(function (err, res) {
        clientB.on('stream', message => {

          clientA.on('stream', message => {
            should(message[0]).deepEqual(expectedResponse);
          });

          should(message[0]).deepEqual(expectedResponse);
          clientA.disconnect();
          clientB.disconnect();
          done();
        });
      });

  });

});
