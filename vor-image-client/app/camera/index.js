'use strict';
const fs = require('fs');
const Rx = require('Rx');
const exec = require('child_process').exec;
const { CAMERA_COMMAND, TEMP_IMAGE } = require('config');

exports.takePicture = () => {
  const commandPromise = new Promise((resolve, reject) => {
    const commandStr = `${CAMERA_COMMAND} ${TEMP_IMAGE}`;
    const command = exec(commandStr, (error) => {
      if (error) {
        reject(console.error(`Error cannot take picture: ${error} : ${new Date()}`));
      }
    });

    command.on('exit', function (stdout, stderr) {
      console.log(`Client took a picture using: ${commandStr} : ${new Date()}`);
      resolve(stdout, stderr);
    });
  });

  return Rx.Observable.fromPromise(commandPromise)
    .flatMap(success => Rx.Observable.fromPromise(readTempFile()))
    .doOnError(error => console.log(`Cannot take picture: ${error} : ${new Date()}`));
};

const readTempFile = () => new Promise((resolve, reject) => {
    try {
      resolve(fs.readFileSync(TEMP_IMAGE));
    }
    catch (error) {
      console.log(`Cannot read image file: ${error} : ${new Date()}`);
      reject(error);
    }
  });
