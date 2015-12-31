#include "vor_light.h"
#include "vor_led.h"

VorLight light(A0);
VorLed led;

void setup() {
    Serial.begin(9600);
    while (!Serial);
}

void loop() {
    float value = light.readProcessed();
    Serial.println(value);
    delay(1000);
}
