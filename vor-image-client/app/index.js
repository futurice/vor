'use strict';
const http = require('http');
const socketIO = require('socket.io-client');
const cameraUsb = require('camera-usb');
const fs = require('fs');
const { SOCKET_SERVER, SOCKET_EVENT_TYPE, SOCKET_EVENT_ID }  = require('config');
const log = func => message => console.log(func(message));
const logError = func => error => console.log(func(error));

const app = http.createServer((request, response) => {
  log(() => `Request to server on ${new Date()}`);
  response.writeHead(200);
  response.end();
});

app.listen(9000, function () {
  log(() => `Server is listening on port 8080 on ${new Date()}`);
});

// init socket client connection
const client = socketIO.connect(SOCKET_SERVER);

const matchesEventType = eventType => new RegExp(SOCKET_EVENT_TYPE).test(eventType);
const matchesEventId = eventId => new RegExp(SOCKET_EVENT_ID).test(eventId);

client.on('connect', socket => {
  log(() => `Socket connection to ${SOCKET_SERVER}`)();
  client.on('message', event => {
    if (matchesEventType(event.type) && matchesEventId(event.id)) {
      log(() => `Socket event ${SOCKET_EVENT_TYPE} from ${SOCKET_SERVER}`)();
      let bufs = [];
      const saveImageStream = cameraUsb.capture();
      saveImageStream.on('data', (buffer) => bufs.push(buffer));
      saveImageStream.on('finish', () => {
        client.emit('message', {
          id: 'pool',
          type: 'pool',
          image: Buffer.concat(bufs).toString('base64')
        });
      });
    }
  });
});

client.on('error', logError(error => `Error with socket connection ${error}`));
