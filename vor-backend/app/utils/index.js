'use strict;';
module.exports = {
  log: func => value => {
    console.log(func(value));
    return value;
  },

  logError: func => error => {
    console.error(func(error));
    return error;
  }
};
