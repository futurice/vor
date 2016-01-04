#include "vor_methane.h"
#include "vor_led.h"

VorMethane methane(A0);
VorLed led;

void setup() {
    Serial.begin(9600);
}

void loop() {
    float value = methane.readProcessed();
    Serial.println(value);
    delay(1000);
}
