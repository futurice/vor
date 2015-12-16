'use strict';
const should = require('should');
const assert = require('assert');
const sinon = require('sinon');
const http = require('http');
const Rx = require('Rx');
const camera = require('../app/camera');
const { MESSAGE_TO_LISTEN, MESSAGE_TO_SEND } = require('config');
const socketIO = require('socket.io');
const server = http.createServer();
const io = socketIO();

describe(`App: on ${MESSAGE_TO_LISTEN.type} event`, function () {

  before((done) => {
    io.attach(server);
    io.on('error', (error => console.log(`Socket connection error: ${error}`)));
    server.listen(5000);
    sinon.stub(camera, 'takePicture', () => {
      return Rx.Observable.from(['test-image'])
    });
    const app = require('../app');
    done();
  });

  it('should send a image message', done => {
    io.on('connection', socket => {

      socket.emit('message', {
        type: MESSAGE_TO_LISTEN.type,
        id: MESSAGE_TO_LISTEN.id
      });

      socket.on('message', message => {
        should(message.id).equal(MESSAGE_TO_SEND.id);
        should(message.type).equal(MESSAGE_TO_SEND.type);
        done();
      });
    });
  });

});
