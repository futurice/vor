'use strict';

module.exports = router => {
  return router.get('/', (req, res) => {
    res.render('index', {
      title: 'Vör - backend'
    });
  });
};
