/*
    Digital PIR motion sensor.
*/

#include "vor_motion.h"

VorMotion::VorMotion(uint8_t pin, uint32_t debounce) :
VorSensor(pin, DIGITAL_INPUT_PULLUP),
_motionValue(HIGH),
_debounce(debounce),
_debounceTime(0) {

}

int VorMotion::read() {
    int value = VorSensor::read();
    uint64_t now = millis();

    if (LOW == value) {
        _motionValue = value;
        _debounceTime = now;
    }

    if (now - _debounceTime > _debounce) {
        _motionValue = value;
    }

    return _motionValue;
}
