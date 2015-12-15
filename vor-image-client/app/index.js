'use strict';
const os = require('os');
const exec = require('child_process').exec;
const fs = require('fs');
const http = require('http');
const socketIO = require('socket.io-client');
const { EVENT, SOCKET_SERVER, SUPPORTED_APPS, TEMP_IMAGE } = require('config');
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

const client = socketIO.connect(SOCKET_SERVER);

const takePicture = app => new Promise((resolve, reject) => {
  const commandStr = `${app.command} ${app.parameters} ${TEMP_IMAGE}`;
  log(message => `Taking picture using: ${message}`)(commandStr);
  const command = exec(commandStr, (error) => {
    if (error) {
      reject(logError(error => `Cannot take picture: ${error}`)(error));
    }
  });

  command.on('exit', function (stdout, stderr) {
    resolve(stdout, stderr);
  })
});

const convertBase64 = file => {
  try {
    const bitmap = fs.readFileSync(file);
    return new Buffer(bitmap).toString('base64');
  }
  catch (e) {
    reject(logError(error => `Cannot read file: ${error}`)(e));
  }
};

const imageApp = SUPPORTED_APPS.find(app => app.platform === os.platform());

if (!imageApp) {
  logError(() => `no image app available!`)()
} else {
  client.on('connect', socket => {
    log(() => `Socket connection to ${SOCKET_SERVER}`)();
    client.on('message', event => {
      if (event.type === EVENT.type && event.id === EVENT.id) {
        takePicture(imageApp)
          .then(data => {
            client.emit('message', {
              type: 'pool',
              id: 'pool',
              image: convertBase64('snapshot.jpg')
            });
          });
      }
    });
  });
}

client.on('error', logError(error => `Error with socket connection ${error}`));

module.exports = app;
