'use strict';
const Rx = require('rx');
const utils = require('app/utils');

class Location {
  constructor (beacons) {
    this.beacons = beacons;
  }

  fromDeviceStream (stream) {
    const beaconStreams = splitInToBeaconStreams(stream, this.beacons);
    return Rx.Observable.combineLatest(
      beaconStreams,
      (beacon1, beacon2, beacon3, rest) => {
        const logBeacons = `${beacon1.id}, ${beacon2.id}, ${beacon3.id}`;
        const log = utils.log(value => `Location.fromDeviceStream (${logBeacons}) --> x:${value.x}, y:${value.y}`);
        return log(calculatePosition(beacon1, beacon2, beacon3));
      });
  }
};

function splitInToBeaconStreams(stream, beaconsConfiguration) {
  return beaconsConfiguration.map(beaconConfig => {
    return stream
      .filter(beaconData => beaconData.floor === beaconConfig.floor)
      .filter(beaconData => beaconData.id === beaconConfig.id)
      .map(beaconData => mapDataWithConfig(beaconData, beaconConfig));
  });
}

function mapDataWithConfig(data, config) {
  return {
    id: data.id,
    email: data.email,
    distance: data.distance,
    x: config.x,
    y: config.y
  };
}

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
    email: obj1.email, // pick the email from first object
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
