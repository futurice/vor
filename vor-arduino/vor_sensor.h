/*
    Abstraction of a sensor.
*/

#ifndef VOR_SENSOR_H
#define VOR_SENSOR_H

#include "Arduino.h"

enum SENSOR_TYPE {
    ANALOG_INPUT         = 0x01,
    ANALOG_OUTPUT        = 0x02,
    DIGITAL_INPUT        = 0x10,
    DIGITAL_OUTPUT       = 0x20,
    DIGITAL_INPUT_PULLUP = 0x30
}

typedef int (*READ_FUNC)(uint8_t);
typedef void (*WRITE_FUNC)(uint8_t, int);

class VorSensor {
    public:
        // pin number, sensor type
        VorSensor(uint8_t pin, SENSOR_TYPE type);

        // read sensor value and return it
        // analog sensor returns 0 - 1023
        // digital sensor returns LOW (0) or HIGH (1)
        virtual int read() { return (*_readFunc)(); };

        // read sensor value and return processed/converted value
        // this function has to be implemented by inheriting class
        virtual float read() = 0;

        virtual int peek() { return _value; };

        virtual float peek() { return process(_value); };

        // write to actuator
        // analog actuator (PWM) accepts 0 - 255
        // digital actuator accepts LOW (0) or HIGH (1)
        virtual void write(int value);

        // TODO: need a function to write processed value as well?

        // process/convert sensor value
        // this function has to be implemented by inheriting class
        virtual float process(int value) = 0;

        // TODO: need a function to process write value as well?

    private:
        VorSensor();

        READ_FUNC _readFunc;
        WRITE_FUNC _writeFunc;

        uint8_t _pin;
        SENSOR_TYPE _type;
        int _value;
};

#endif
