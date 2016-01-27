'use strict';
const should = require('should');
const assert = require('assert');
const sinon = require('sinon');
const http = require('http');
const Rx = require('rx');
const camera = require('../app/camera');
const { MESSAGE_TO_LISTEN, MESSAGE_TO_SEND } = require('../config');
const socketIO = require('socket.io');
const server = http.createServer();
const io = socketIO();

const LISTEN_TYPE = 'button';
const LISTEN_ID = 'button-1';
const SEND_TYPE = '3D';
const SEND_ID = '3D';

describe(`App: on socket event`, function () {

  before((done) => {

    process.env.SOCKET_SERVER = 'http://localhost:5000';
    process.env.LISTEN_TYPE = LISTEN_TYPE;
    process.env.LISTEN_ID = LISTEN_ID;
    process.env.SEND_TYPE = SEND_TYPE;
    process.env.SEND_ID = SEND_ID;

    io.attach(server);
    io.on('error', (error => console.log(`Socket connection error: ${error}`)));
    server.listen(5000);
    sinon.stub(camera, 'takePicture', () => Rx.Observable.from(['test-image']));
    const app = require('../app');
    done();
  });

  it('should send an image message', done => {
    io.on('connection', socketServer => {

      socketServer.emit('message', {
        type: LISTEN_TYPE,
        id: LISTEN_ID
      });

      socketServer.on('message', message => {
        should(message.id).equal(SEND_ID);
        should(message.type).equal(SEND_TYPE);
        should(message.image).equal('test-image');
        done();
      });
    });
  });

});
