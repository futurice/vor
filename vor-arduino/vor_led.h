/*
    Arduino on-board LED actuator for testing.
*/

#ifndef VOR_LED_H
#define VOR_LED_H

#include "vor_sensor.h"

class VorLed : public VorSensor {
public:
    VorLed();

    bool isOn() { return _on; }

    void turnOn();

    void turnOff();

    void toggle();

protected:

private:
    bool _on;
};

#endif
