/*
    Button sensor.
*/

#include "vor_button.h"

VorButton::VorButton(uint8_t pin) :
VorSensor(pin, DIGITAL_INPUT_PULLUP) {
    _value = HIGH;
}

int VorButton::read() {
    int value = VorSensor::read();
    uint64_t now = millis();

    if (now - _debounceTime > DEBOUNCE && value != _value) {
        _debounceTime = now;
        _value = value;
    }

    return _value;
}
