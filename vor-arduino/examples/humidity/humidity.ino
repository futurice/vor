#include "vor_humidity.h"
#include "vor_led.h"

VorHumidity humidity(A0);
VorLed led;

void setup() {
    Serial.begin(9600);
}

void loop() {
    float value = humidity.readProcessed();
    Serial.println(value);
    delay(1000);
}
