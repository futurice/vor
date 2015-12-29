/*
    AAnalog ambient light sensor.
*/

#include "vor_light.h"

VorLight::VorLight(uint8_t pin) :
VorSensor(pin, ANALOG_INPUT) {

}

float VorLight::process(int value) {
    // https://www.sparkfun.com/datasheets/Sensors/Imaging/TEMT6000.pdf
    // 10 K ohm resistor
    // 20   lx = 10  uA = 0.00001 A = 0.1 V
    // 100  lx = 50  uA = 0.00005 A = 0.5 V
    // 1000 lx = 500 uA = 0.00050 A = 5.0 V
    //
    // illuminance (lx) = 2000000 * (V / 10000 ohms)
    // this equation does not calculate illuminance correctly but the size of
    // the value should be about correct...
    float illuminance = 2000.0 * (value / MAX_ANALOG_VALUE);
    return illuminance;
}
