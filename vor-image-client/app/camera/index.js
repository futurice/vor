'use strict';
const fs = require('fs');
const Rx = require('rx');
const exec = require('child_process').exec;
const { CAMERA_COMMAND } = require('config');

const TEMP_IMAGE = 'snapshot.jpg';

exports.takePicture = () => {
  const takePicturePromise = new Promise((resolve, reject) => {
    const commandStr = `${CAMERA_COMMAND} ${TEMP_IMAGE}`;
    const command = exec(commandStr, (error) => {
      if (error) {
        reject(console.error(`Error - cannot run '${CAMERA_COMMAND}': ${error} : ${new Date()}`));
      }
    });

    command.on('close', (stdout, stderr) => {
      console.log(`Client took a picture using: ${commandStr} : ${new Date()}`);
      resolve(stdout, stderr);
    });
  });

  const readTempFile = Rx.Observable.fromNodeCallback(fs.readFile, this);

  return Rx.Observable.fromPromise(takePicturePromise)
    .flatMap(tookPicture => readTempFile(TEMP_IMAGE))
    .map(binary => new Buffer(binary))
    .map(buffer => buffer.toString('base64'))
    .doOnError(error => console.error(`Error - cannot take picture: ${error} : ${new Date()}`));

};
