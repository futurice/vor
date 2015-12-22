/*
    Analog methane sensor.
*/

#ifndef VOR_METHANE_H
#define VOR_METHANE_H

#include "env.h"
#include "vor_sensor.h"

class VorMethane : public VorSensor {
public:
    VorMethane(uint8_t pin);

    float process(int value);

};

#endif
