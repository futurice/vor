/*
    Analog humidity sensor.
*/

#include "vor_humidity.h"

VorHumidity::VorHumidity(uint8_t pin) :
VorSensor(pin, ANALOG_INPUT) {

}

float VorHumidity::process(int value) {
    float sensorRH = (value / MAX_ANALOG_INPUT_VALUE - 0.16) / 0.0062;
    //float temperature = 23.0;
    //float trueRH = sensorRH / (1.0546 - 0.00216 * temperature);
    return sensorRH;
}
