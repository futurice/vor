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
};

typedef int (*READ_FUNC)(uint8_t);
typedef void (*WRITE_FUNC)(uint8_t, int); // TODO: this not used, see VorSensor::write()

class VorSensor {
public:
    // pin number, sensor type
    VorSensor(uint8_t pin, SENSOR_TYPE type, const char* id = "", const char* name = "", const char* description = "");

    void setId(const char* id) { _id = id; }
    const char* getId() { return _id; }

    void setName(const char* name) { _name = name; }
    const char* getName() { return _name; }

    void setDescription(const char* description) { _description = description; }
    const char* getDescription() { return _description; }

    // read sensor value and return it
    // analog sensor returns 0 - 1023
    // digital sensor returns LOW (0) or HIGH (1)
    virtual int read();

    // read sensor value and return processed/converted value
    // this function has to be implemented by inheriting class
    virtual float readProcessed();

    virtual int peek() { return _value; }

    virtual float peekProcessed() { return _processedValue; }

    // write to actuator
    // analog actuator (PWM) accepts 0 - 255
    // digital actuator accepts LOW (0) or HIGH (1)
    virtual void write(int value);

    // TODO: need a function to write processed value as well?

    // process/convert sensor value
    // this function has to be implemented by inheriting class
    virtual float process(int value) { return value; }

    // TODO: need a function to process write value as well? e.g. convert percentage to write value?

protected:

private:
    VorSensor();

    READ_FUNC _readFunc;
    // WRITE_FUNC _writeFunc; // TODO: would like to have something like this

    uint8_t _pin;
    SENSOR_TYPE _type;
    int _value;
    float _processedValue;

    const char* _id;
    const char* _name;
    const char* _description;
};

#endif
