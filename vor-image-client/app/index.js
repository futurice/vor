'use strict';
const http = require('http');
const Rx = require('Rx');
const socketIO = require('socket.io-client');
const { EVENT, SOCKET_SERVER } = require('config');
const camera = require('app/camera');

const app = http.createServer((request, response) => {
  log(() => `Request to server on ${new Date()}`);
  response.writeHead(200);
  response.end();
});

app.listen(9000, console.log(`Client server listening :9000: ${new Date()}`));

// setup socket
const client = socketIO.connect(SOCKET_SERVER);
client.on('connect', () => console.log(`Image client socket connected ${SOCKET_SERVER} : ${new Date()}`));
client.on('disconnect', () => console.log(`Client socket disconnected ${SOCKET_SERVER} :  ${new Date()}`));
client.on('error', error => console.error(`Error with socket connection: ${error} : ${new Date()}`));

// listen socket messages
const socketMessageSource$ = Rx.Observable.fromEvent(client, 'message');

const triggerCameraSource$ = socketMessageSource$
  .filter(message => message.type === EVENT.type)
  .filter(message => message.id === EVENT.id);

triggerCameraSource$
  .flatMap(res => camera.takePicture())
  .map(binary => new Buffer(binary))
  .map(buffer => buffer.toString('base64'))
  .subscribe(
    asBase64 => {
      client.emit('message', {
        type: 'pool',
        id: 'pool',
        image: asBase64
      })
    },
    error => console.error(`Error while taking picture: ${error} : ${new Date()}`));


module.exports = app;
