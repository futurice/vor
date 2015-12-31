'use strict';
const Rx = require('rx');

class Location {
  constructor(beaconConfigs) {
    this.beaconConfigs = beaconConfigs;
  }

  fromDeviceStream(stream) {
    return stream
      .map(getConfig(this.beaconConfigs))
      .filter(([beaconMessage, config]) => !!config)
      .map(([beaconMessage, config]) => Object.assign(beaconMessage, config)) // merge message with configured data
      .bufferWithCount(3)
      .map(([beacon1, beacon2, beacon3]) => {
        const position = calculatePosition(beacon1, beacon2, beacon3);
        const messageData = Object.assign({
          email: beacon1.email, // get the email from the first beacon message
          floor: beacon1.floor, // get the floor from the first beacon message
          type: 'location' // constant for every location message
        }, position);
        const logBeacons = `${beacon1.id}, ${beacon2.id}, ${beacon3.id}`;
        console.log(`Server - location (${logBeacons}) : ${JSON.stringify(position)} : ${new Date}`);
        return messageData;
      });
  }
}

const getConfig = beaconConfigs => beaconMessage => {
  const [config] = beaconConfigs
    .filter(config => config.id === beaconMessage.id)
    .filter(config => config.floor === beaconMessage.floor);
  return [ beaconMessage, config ];
};

/*
 http://everything2.com/title/Triangulate
 http://stackoverflow.com/questions/20332856/triangulate-example-for-ibeacons#answer-20976803
 */

function calculatePosition(obj1, obj2, obj3) {

  const W = getIntersectionPoint(obj1, obj2);
  const Z = getIntersectionPoint(obj2, obj3);
  const Y = (W * (obj3.y - obj2.y) - Z * (obj2.y - obj1.y));
  const x = Y / (2 * ((obj2.x - obj1.x) * (obj3.y - obj2.y) - (obj3.x - obj2.x) * (obj2.y - obj1.y)));
  const y = (W - 2 * x * (obj2.x - obj1.x)) / (2 * (obj2.y - obj1.y));

  return {
    x: isValidPosition(x) && x || 0,
    y: isValidPosition(y) && y || 0
  };
}
const getIntersectionPoint = (obj1, obj2) => {
  const areaDifference = Math.pow(obj1.distance, 2) - Math.pow(obj2.distance, 2);
  return areaDifference - Math.pow(obj1.x, 2) - Math.pow(obj1.y, 2) + Math.pow(obj2.x, 2) + Math.pow(obj2.y, 2);
};

const isValidPosition = (x) => !(isNaN(x) || x + x === x); // infinity + infinity = infinity

module.exports = Location;
