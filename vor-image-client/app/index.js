'use strict';
const http = require('http');
const Rx = require('rx');
const socketIO = require('socket.io-client');
const { MESSAGE_TO_LISTEN, MESSAGE_TO_SEND, SOCKET_SERVER } = require('config');
const camera = require('app/camera');

const app = http.createServer((request, response) => {
  log(() => `Request to server on ${new Date()}`);
  response.writeHead(200);
  response.end();
});

app.listen(9000, console.log(`Client server listening :9000: ${new Date()}`));

// setup socket
const client = socketIO.connect(SOCKET_SERVER);
console.log(`Client trying to connect ${SOCKET_SERVER} : ${new Date()}`);
client.on('connect', () => console.log(`Client socket connected ${SOCKET_SERVER} : ${new Date()}`));
client.on('disconnect', () => console.log(`Client socket disconnected ${SOCKET_SERVER} :  ${new Date()}`));
client.on('reconnect_attempt', error => console.error(`Error cannot connect to ${SOCKET_SERVER} : ${error} : ${new Date()}`));
client.on('error', error => console.error(`Error with socket connection: ${error} : ${new Date()}`));

// listen socket messages
const socketMessageSource$ = Rx.Observable.fromEvent(client, 'message');

const triggerCameraSource$ = socketMessageSource$
  .filter(message => message.type === MESSAGE_TO_LISTEN.type)
  .filter(message => message.id === MESSAGE_TO_LISTEN.id);

triggerCameraSource$
  .flatMap(camera.takePicture)
  .subscribe(
    imageString => {
      client.emit('message', {
        type: MESSAGE_TO_SEND.type,
        id: MESSAGE_TO_SEND.id,
        image: imageString
      });
      console.log(`Client emit message to ${SOCKET_SERVER} :  ${new Date()}`);
    },
    error => console.error(`Error while taking picture: ${error} : ${new Date()}`)
  );


module.exports = app;
