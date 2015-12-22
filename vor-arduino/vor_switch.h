/*
    Digital switch sensor.
*/

#ifndef VOR_SWITCH_H
#define VOR_SWITCH_H

#include "vor_sensor.h"

#define DEBOUNCE 100 // milliseconds

class VorSwitch : public VorSensor {
public:
    VorSwitch(uint8_t pin);

    int read();

private:

    int _switchValue;
    uint64_t _debounceTime; // milliseconds

};

#endif
