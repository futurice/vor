/*
    Abstraction of a sensor.
*/

#include "vor_sensor.h"

VorSensor::VorSensor(
    uint8_t pin,
    SENSOR_TYPE type,
    const char* id,
    const char* name,
    const char* description) :
_pin(pin),
_type(type),
_id(id),
_name(name),
_description(description) {

    switch (_type) {
        case ANALOG_INPUT:
            _readFunc = analogRead;
            break;
        case DIGITAL_INPUT:
            _readFunc = digitalRead;
            pinMode(_pin, INPUT);
            break;
        case DIGITAL_INPUT_PULLUP:
            _readFunc = digitalRead;
            pinMode(_pin, INPUT_PULLUP);
            break;
        default:
            break;
    }
}

int VorSensor::read() {
    _value = (*_readFunc)(_pin);
    return _value;
}

float VorSensor::readProcessed() {
    _processedValue = process(read());
    return _processedValue;
}
