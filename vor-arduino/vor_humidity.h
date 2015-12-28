/*
    Analog humidity sensor.
*/

#ifndef VOR_HUMIDITY_H
#define VOR_HUMIDITY_H

#include "vor_sensor.h"
#include "env.h"

class VorHumidity : public VorSensor {
public:
    VorHumidity(uint8_t pin);

    float process(int value);

};

#endif
