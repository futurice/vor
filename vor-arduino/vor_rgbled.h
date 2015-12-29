/*
    Analog RGB LED actuator.
    Common anode RGB LED.
*/

#ifndef VOR_RGBLED_H
#define VOR_RGBLED_H

#include "vor_actuator.h"

class VorRgbLed{
public:
    VorRgbLed(int redPin, int greenPin, int bluePin);
    ~VorRgbLed();

    VorActuator* getRedLed();
    VorActuator* getGreenLed();
    VorActuator* getBlueLed();

private:
    VorActuator* _red;
    VorActuator* _green;
    VorActuator* _blue;
};

#endif
