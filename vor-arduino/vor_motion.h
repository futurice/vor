/*
    Digital PIR motion sensor.
*/

#ifndef VOR_MOTION_H
#define VOR_MOTION_H

#include "vor_sensor.h"

class VorMotion : public VorSensor {
public:
    VorMotion(uint8_t pin, uint32_t debounce = 30000);

    int read();

    void setDebounce(uint32_t debounce) { _debounce = debounce; }
    uint32_t getDebounce() { return _debounce; }

private:

    int _motionValue;
    uint32_t _debounce;
    uint64_t _debounceTime; // milliseconds

};

#endif
