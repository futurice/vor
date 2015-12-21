/*
    Button sensor.
*/

#ifndef VOR_BUTTON_H
#define VOR_BUTTON_H

#include "vor_sensor.h"

#define DEBOUNCE 50 // milliseconds

class VorButton : public VorSensor {
public:
    VorButton(uint8_t pin);

    int read();

private:
    uint64_t _debounceTime = 0; // milliseconds
};

#endif
