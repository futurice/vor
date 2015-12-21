/*
    Arduino on-board LED actuator for testing.
*/

#ifndef VOR_LED_H
#define VOR_LED_H

#include "vor_actuator.h"

#define LED_PIN 13

class VorLed : public VorActuator {
public:
    VorLed();

    bool isOn() { return _on; }

    void turnOn();

    void turnOff();

    void toggle();

private:
    bool _on;
};

#endif
