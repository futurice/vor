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
const UPDATE_TIME = 100;

describe(`App: on socket event`, function () {
  this.timeout(UPDATE_TIME * 2);

  before((done) => {

    process.env.SOCKET_SERVER = 'http://localhost:5000';
    process.env.LISTEN_TYPE = LISTEN_TYPE;
    process.env.LISTEN_ID = LISTEN_ID;
    process.env.SEND_TYPE = SEND_TYPE;
    process.env.SEND_ID = SEND_ID;
    process.env.UPDATE_TIME = UPDATE_TIME;

    io.attach(server);
    io.on('error', (error => console.log(`Socket connection error: ${error}`)));
    server.listen(5000);
    sinon.stub(camera, 'takePicture', () => Rx.Observable.from(['test-image']));
    const app = require('../app');
    done();
  });

  it('should receive a single image within the time period', done => {
    io.on('connection', socketServer => {
      socketServer.on('message', message => {
        should(message.id).equal(SEND_ID);
        should(message.type).equal(SEND_TYPE);
        should(message.image).equal('test-image');
        done();
      });
    });
  });

});
