'use strict';
const should = require('should');
const assert = require('assert');
const helpers = require('./helpers/index');
var request = require('supertest');

describe('App: On message post', function () {
  let app;

  beforeEach(function (done) {
    helpers.setupCache();
    app = require('../bin/www');
    done();
  });

  afterEach(helpers.flushCache);

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
        clientB.on('message', message => {

          clientA.on('message', message => {
            should(message).deepEqual(expectedResponse);
          });

          should(message).deepEqual(expectedResponse);
          clientA.disconnect();
          clientB.disconnect();
          done();
        });
      });

  });

  it('should fail on invalid json', done => {
    const TEST_MESSAGE = 'INVALID';
    request(app)
      .post('/messages')
      .set('Content-type', 'text/plain')
      .send(TEST_MESSAGE)
      .expect(500)
      .end(function (err, res) {
        done();
      });

  });

});
