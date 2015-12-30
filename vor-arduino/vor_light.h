/*
    Analog ambient light sensor.
*/

#ifndef VOR_LIGHT_H
#define VOR_LIGHT_H

#include "vor_sensor.h"
#include "vor_env.h"

class VorLight : public VorSensor {
public:
    VorLight(uint8_t pin);

    float process(int value);

};

#endif
