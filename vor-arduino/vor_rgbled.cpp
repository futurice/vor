/*
    Analog RGB LED actuator.
    Common anode RGB LED.
*/

#include "vor_rgbled.h"

VorRgbLed::VorRgbLed(int redPin, int greenPin, int bluePin) {
    _red = new VorActuator(redPin, ANALOG_OUTPUT);
    _green = new VorActuator(greenPin, ANALOG_OUTPUT);
    _blue = new VorActuator(bluePin, ANALOG_OUTPUT);
}

VorRgbLed::~VorRgbLed() {
    delete _red;
    delete _green;
    delete _blue;
    _red = NULL;
    _green = NULL;
    _blue = NULL;
}

VorActuator* VorRgbLed::getRedLed() {
    return _red;
}

VorActuator* VorRgbLed::getGreenLed() {
    return _green;
}

VorActuator* VorRgbLed::getBlueLed() {
    return _blue;
}
