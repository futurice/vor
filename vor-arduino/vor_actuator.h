/*
    Abstraction of an actuator.
*/

#ifndef VOR_ACTUATOR_H
#define VOR_ACTUATOR_H

#include "Arduino.h"

enum ACTUATOR_TYPE {
    ANALOG_OUTPUT        = 0x10,
    DIGITAL_OUTPUT       = 0x20
};

// typedef void (*WRITE_FUNC)(uint8_t, int); // TODO: this not used, see VorActuator::write()

class VorActuator {
public:
    // pin number, actuator type
    VorActuator(
        uint8_t pin,
        ACTUATOR_TYPE type,
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

    // write to actuator
    // analog actuator (PWM) accepts 0 - 255
    // digital actuator accepts LOW (0) or HIGH (1)
    virtual void write(int value);

    virtual void writeProcessed(float value) { write(process(value)); }

    // process/convert actuator value
    // this function has to be implemented by inheriting class
    virtual int process(float value) { return value; }

private:
    // WRITE_FUNC _writeFunc; // TODO: would like to have something like this

    uint8_t _pin;
    ACTUATOR_TYPE _type;

    const char* _id;
    const char* _name;
    const char* _description;

private:

    VorActuator();

};

#endif
