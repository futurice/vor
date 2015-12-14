'use strict';
const http = require('http');
const RaspiCam = require('raspicam');
const socketIO = require('socket.io-client');
const { SOCKET_SERVER, CAMERA_OPTIONS }  = require('config');
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

client.on('connect', socket => {
  log(() => `Socket connection to ${SOCKET_SERVER} on ${new Date()}`)();
  client.on('stream', socket => {
    log(() => `Socket event photo:pool from ${SOCKET_SERVER} on ${new Date()}`)();
  });
});

client.on('error', logError(error => `Error with socket connection ${error}`));
