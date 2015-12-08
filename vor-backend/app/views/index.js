'use strict';
const express = require('express');
const path = require('path');

exports.renderTestPage = app => {
  // view engine setup
  app.set('views', path.join(__dirname, 'templates'));
  app.set('view engine', 'ejs');
  app.use('/css', express.static(path.join(__dirname, '../../node_modules/bootstrap/dist/css')));
};
