/*
    Arduino on-board LED actuator for testing.
*/

#include "vor_led.h"

VorLed::VorLed() :
VorSensor(13, DIGITAL_OUTPUT) {
    turnOff();
}

void VorLed::turnOn() {
    VorSensor::write(HIGH);
    _on = true;
}

void VorLed::turnOff() {
    VorSensor::write(LOW);
    _on = false;
}

void VorLed::toggle() {
    _on ? turnOff() : turnOn();
}
