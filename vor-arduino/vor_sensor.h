/*
    Abstraction of a sensor.
*/

#ifndef VOR_SENSOR_H
#define VOR_SENSOR_H

#include "Arduino.h"

enum SENSOR_TYPE {
    ANALOG_INPUT         = 0x01,
    DIGITAL_INPUT        = 0x02,
    DIGITAL_INPUT_PULLUP = 0x04
};

typedef int (*READ_FUNC)(uint8_t);

class VorSensor {
public:
    // pin number, sensor type
    VorSensor(
        uint8_t pin,
        SENSOR_TYPE type,
        const char* id = NULL,
        const char* name = NULL,
        const char* description = NULL
    );

    void setId(const char* id) { _id = id; }
    const char* getId() { return _id; }

    void setName(const char* name) { _name = name; }
    const char* getName() { return _name; }

    void setDescription(const char* description) { _description = description; }
    const char* getDescription() { return _description; }

    // read raw sensor value and return it
    // analog sensor returns 0 - 1023
    // digital sensor returns LOW (0) or HIGH (1)
    virtual int read();

    // read raw sensor value and return processed/converted value
    // this function has to be implemented by inheriting class
    virtual float readProcessed();

    // get the last raw sensor value
    virtual int peek() { return _value; }

    // get the last processed sensor value
    virtual float peekProcessed() { return _processedValue; }

    // process/convert sensor value
    // this function has to be implemented by inheriting class
    virtual float process(int value) { return value; }

protected:

    READ_FUNC _readFunc;

    uint8_t _pin;
    SENSOR_TYPE _type;
    int _value;
    float _processedValue;

    const char* _id;
    const char* _name;
    const char* _description;

private:

    VorSensor();

};

#endif
