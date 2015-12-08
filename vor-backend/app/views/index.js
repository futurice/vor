'use strict';
const express = require('express');
const path = require('path');
const bodyParser = require('body-parser');
const logger = require('morgan');
const routes = require('app/views/routes');

exports.renderTestPage = app => {
  // view engine setup
  app.set('views', path.join(__dirname, 'templates'));
  app.set('view engine', 'ejs');
  app.use(logger('dev'));
  app.use('/css', express.static(path.join(__dirname, '../../node_modules/bootstrap/dist/css')));
  app.use(bodyParser.json());
  app.use(bodyParser.urlencoded({extended: false}));
  app.use('/', routes);

  app.use((err) => {
    logger('error:' + err.message);
  });

  app.use((req, res, next) => {
    const err = new Error('Not Found');
    err.status = 404;
    next(err);
  });

};
