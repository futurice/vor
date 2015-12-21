/*
    Arduino on-board LED actuator for testing.
*/

#include "vor_led.h"

VorLed::VorLed() :
VorActuator(LED_PIN, DIGITAL_OUTPUT) {
    turnOff();
}

void VorLed::turnOn() {
    VorActuator::write(HIGH);
    _on = true;
}

void VorLed::turnOff() {
    VorActuator::write(LOW);
    _on = false;
}

void VorLed::toggle() {
    _on ? turnOff() : turnOn();
}
