'use strict';
const Rx = require('rx');

class Location {
  constructor(beacons) {
    this.beacons = beacons;
  }

  fromDeviceStream(stream) {
    return stream
      .bufferWithCount(3)
      .map(([b1, b2, b3]) => {
        console.log('Server - beacons --> ', b1, b2, b3);
        let beacons = [b1, b2, b3]
          .map(mapData(this.beacons))
          .filter(data => !!data);

        if (beacons.length < 3) {
          return;
        }

        const messageData = Object.assign({
          email: beacons[0].email, // get email data from beacon
          type: 'location' // constant for every message
        }, calculatePosition(beacons[0], beacons[1], beacons[2]));
        const logBeacons = `${beacons[0].id}, ${beacons[1].id}, ${beacons[2].id}`;
        console.log(`Server - location for (${logBeacons}) --> ${JSON.stringify(messageData)}`);
        return messageData;
      });
  }
}

function mapData(beaconConfigurations) {
  return function(beacon){
    let index = beaconConfigurations.findIndex(config => config.id === beacon.id);
    if (index === -1) {
      return false;
    }
    return {
      id: beacon.id,
      email: beacon.email,
      distance: beacon.distance,
      x: beaconConfigurations[index].x,
      y: beaconConfigurations[index].y
    };
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
