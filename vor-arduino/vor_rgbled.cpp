/*
    Analog RGB LED actuator.
    Common anode RGB LED.
    Voltage drop resistors:
    Red   220 ohm
    Green 100 ohm
    Blue  220 ohm
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

void VorRgbLed::writeRed(int value) {
    _red->write(MAX_ANALOG_OUTPUT_VALUE - value);
}

void VorRgbLed::writeGreen(int value) {
    _green->write(MAX_ANALOG_OUTPUT_VALUE - value);
}

void VorRgbLed::writeBlue(int value) {
    _blue->write(MAX_ANALOG_OUTPUT_VALUE - value);
}

void VorRgbLed::write(int red, int green, int blue) {
    writeRed(red);
    writeGreen(green);
    writeBlue(blue);
}
