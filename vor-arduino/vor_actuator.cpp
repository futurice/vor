/*
    Abstraction of a sensor.
*/

#include "vor_actuator.h"

VorActuator::VorActuator(
    uint8_t pin,
    ACTUATOR_TYPE type,
    const char* id,
    const char* name,
    const char* description) :
_pin(pin),
_type(type),
_id(id),
_name(name),
_description(description) {

    switch (_type) {
        case ANALOG_OUTPUT:
            break;
        case DIGITAL_OUTPUT:
            pinMode(_pin, OUTPUT);
            break;
        default:
            break;
    }
}

// TODO: this is ugly
void VorActuator::write(int value) {
    if (_type == ANALOG_OUTPUT) {
        analogWrite(_pin, value);
    } else if (_type == DIGITAL_OUTPUT) {
        digitalWrite(_pin, value);
    } else {
        return;
    }
}
