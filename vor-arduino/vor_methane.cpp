/*
    Analog methane sensor.
*/

#include "vor_methane.h"

VorMethane::VorMethane(uint8_t pin) :
VorSensor(pin, ANALOG_INPUT) {

}

float VorMethane::process(int value) {
    float volts = value / MAX_ANALOG_INPUT_VALUE * MAX_VOLTS;
    // this equation was determined from a graph in datasheet, page 5:
    // https://cdn.sparkfun.com/datasheets/Sensors/Biometric/MQ-4%20Ver1.3%20-%20Manual.pdf
    float ppm = pow(M_E, (volts + 1.8992) / 0.619);
    //float ppm = 23.953 * pow(M_E, 1.58 * volts); // alternative equation
    return ppm;
}
