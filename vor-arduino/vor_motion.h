/*
    PIR motion sensor.
*/

#ifndef VOR_MOTION_H
#define VOR_MOTION_H

#include "vor_sensor.h"

#define DEBOUNCE 30000 // milliseconds

class VorMotion : public VorSensor {
public:
    VorMotion(uint8_t pin);

    int read();

private:

    int _motionValue;
    uint64_t _debounceTime; // milliseconds

};

#endif
