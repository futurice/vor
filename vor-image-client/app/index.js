'use strict';
const http = require('http');
const Rx = require('rx');
const socketIO = require('socket.io-client');
const { MESSAGE_TO_LISTEN, MESSAGE_TO_SEND } = require('config');
const camera = require('app/camera');

const app = http.createServer((request, response) => {
  console.log(`Client request : ${new Date()}`);
  response.writeHead(200);
  response.end();
});

app.listen(9000, console.log(`Client listening :9000: ${new Date()}`));

// setup socket
const socketServer = process.env.SOCKET_SERVER;
const client = socketIO.connect(socketServer);
console.log(`Client trying to connect ${socketServer} : ${new Date()}`);
client.on('connect', () => console.log(`Client socket connected ${socketServer} : ${new Date()}`));
client.on('disconnect', () => console.log(`Client socket disconnected ${socketServer} :  ${new Date()}`));
client.on('reconnect_attempt', error => console.error(`Error cannot connect to ${socketServer} : ${error} : ${new Date()}`));
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
      console.log(`Client emit message to ${socketServer} :  ${new Date()}`);
    },
    error => console.error(`Error while taking picture: ${error} : ${new Date()}`)
  );


module.exports = app;
