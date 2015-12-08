'use strict';
const express = require('express');
const { STREAM } = require('config');

const router = express.Router();

router.get('/', (req, res) => {
  res.render('index', {
    title: 'Vör - backend',
    STREAM: STREAM
  });
});

module.exports = router;
