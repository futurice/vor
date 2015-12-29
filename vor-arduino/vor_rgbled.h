/*
    Analog RGB LED actuator.
    Common anode RGB LED.
*/

#ifndef VOR_RGBLED_H
#define VOR_RGBLED_H

#include "vor_actuator.h"
#include "env.h"

class VorRgbLed{
public:
    VorRgbLed(int redPin, int greenPin, int bluePin);
    ~VorRgbLed();

    void writeRed(int value);
    void writeGreen(int value);
    void writeBlue(int value);
    void write(int red, int green, int blue);

private:
    VorActuator* _red;
    VorActuator* _green;
    VorActuator* _blue;
};

#endif
