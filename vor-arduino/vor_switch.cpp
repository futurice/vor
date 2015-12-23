/*
    Digital switch sensor.
*/

#include "vor_switch.h"

VorSwitch::VorSwitch(uint8_t pin) :
VorSensor(pin, DIGITAL_INPUT),
_debounceTime(0) {

}

int VorSwitch::read() {
    int value = VorSensor::read();

    uint64_t now = millis();

    if (now - _debounceTime > DEBOUNCE && value != _switchValue) {
        _debounceTime = now;
        _switchValue = value;
    }

    return _switchValue;
}
