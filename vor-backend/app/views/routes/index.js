'use strict';
const { STREAM } = require('config');

module.exports = router => {
  return router.get('/', (req, res) => {
    res.render('index', {
      title: 'Vör - backend',
      STREAM: STREAM
    });
  });
};
