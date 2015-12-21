/*
    Button sensor.
*/

#include "vor_button.h"

VorButton::VorButton(uint8_t pin) :
VorSensor(pin, DIGITAL_INPUT_PULLUP),
_buttonValue(HIGH),
_debounceTime(0) {

}

int VorButton::read() {
    int value = VorSensor::read();
    uint64_t now = millis();

    if (now - _debounceTime > DEBOUNCE && value != _buttonValue) {
        _debounceTime = now;
        _buttonValue = value;
    }

    return _buttonValue;
}
