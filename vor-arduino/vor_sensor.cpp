/*
    Abstraction of a sensor.
*/

#include "vor_sensor.h"

VorSensor::VorSensor(uint8_t pin, SENSOR_TYPE type) :
    _pin(pin),
    _type(type) {

    switch (_type) {
        case ANALOG_INPUT:
            _readFunc = analogRead;
            break;
        case ANALOG_OUTPUT:
            _writeFunc = analogWrite;
            break;
        case DIGITAL_INPUT:
            _readFunc = digitalRead;
            pinMode(_pin, INPUT);
            break;
        case DIGITAL_OUTPUT:
            _writeFunc = digitalWrite;
            pinMode(_pin, OUTPUT);
            break;
        case DIGITAL_INPUT_PULLUP:
            _readFunc = digitalRead;
            pinMode(_pin, INPUT_PULLUP);
            break;
        default:
            break;
    }
}
