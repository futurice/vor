#!/usr/bin/env node
'use strict';

// module dependencies.
const app = require('app');
const debug = require('debug')('vor-backend:server');
const http = require('http');

// Get port from environment and store in Express.
const port = process.env.PORT || '8080';
app.set('port', port);

// create HTTP server
var server = http.createServer(app);

// attach socket.io
app.io.attach(server);

// listen on provided port, on all network interfaces.
server.listen(port);
server.on('error', onError);
server.on('listening', onListening);

// event listener for HTTP server "error" event.
function onError(error) {
  if (error.syscall !== 'listen') {
    throw error;
  }

  var bind = typeof port === 'string'
    ? 'Pipe ' + port
    : 'Port ' + port;

// handle specific listen errors with friendly messages
  switch (error.code) {
    case 'EACCES':
      console.error(bind + ' requires elevated privileges');
      process.exit(1);
      break;
    case 'EADDRINUSE':
      console.error(bind + ' is already in use');
      process.exit(1);
      break;
    default:
      throw error;
  }
}

// event listener for HTTP server "listening" event.
function onListening() {
  var addr = server.address();
  var bind = typeof addr === 'string'
    ? 'pipe ' + addr
    : 'port ' + addr.port;
  console.log('Server - listening on ' + bind);
}

module.exports = app;
