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
      .map(([beaconMessage, beacons]) => [ beaconMessage.email,
        Object.assign(beaconMessage.beacons[0], beacons[0]),
        Object.assign(beaconMessage.beacons[1], beacons[1]),
        Object.assign(beaconMessage.beacons[2], beacons[2])]) // merge beacons with the configured data.
      .map(([email, beacon1, beacon2, beacon3]) => {
        const position = calculatePosition(beacon1, beacon2, beacon3);
        const messageData = Object.assign({
          email: email,
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
  const [beacon1] = beaconConfigs
    .filter(config => config.id === beaconMessage.beacons[0].id)
    .filter(config => config.floor === beaconMessage.beacons[0].floor);

  const [beacon2] = beaconConfigs
    .filter(config => config.id === beaconMessage.beacons[1].id)
    .filter(config => config.floor === beaconMessage.beacons[1].floor);

  const [beacon3] = beaconConfigs
    .filter(config => config.id === beaconMessage.beacons[2].id)
    .filter(config => config.floor === beaconMessage.beacons[2].floor);

  return [beaconMessage, [ beacon1, beacon2, beacon3 ]];
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
  let y = (W - 2 * x * (obj2.x - obj1.x)) / (2 * (obj2.y - obj1.y));

  if (!isValidPosition(y)) {
    y = (Z - 2 * x * (obj3.x - obj2.x)) / (2 * (obj3.y - obj2.y));
  }

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
