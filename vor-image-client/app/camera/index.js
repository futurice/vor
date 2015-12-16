'use strict';
const fs = require('fs');
const Rx = require('rx');
const exec = require('child_process').exec;
const { CAMERA_COMMAND, TEMP_IMAGE } = require('config');

exports.takePicture = () => {
  const takePicturePromise = new Promise((resolve, reject) => {
    const commandStr = `${CAMERA_COMMAND} ${TEMP_IMAGE}`;
    const command = exec(commandStr, (error) => {
      if (error) {
        reject(console.error(`Error cannot take picture: ${error} : ${new Date()}`));
      }
    });

    command.on('exit', (stdout, stderr) => {
      console.log(`Client took a picture using: ${commandStr} : ${new Date()}`);
      resolve(stdout, stderr);
    });
  });

  const readTempFile = () => new Promise((resolve, reject) => {
    try {
      resolve(fs.readFileSync(TEMP_IMAGE));
    }
    catch (error) {
      console.log(`Cannot read image file: ${error} : ${new Date()}`);
      reject(error);
    }
  });

  return Rx.Observable.fromPromise(takePicturePromise)
    .flatMap(success => Rx.Observable.fromPromise(readTempFile()))
    .map(binary => new Buffer(binary))
    .map(buffer => buffer.toString('base64'))
    .doOnError(error => console.log(`Cannot take picture: ${error} : ${new Date()}`));

};
