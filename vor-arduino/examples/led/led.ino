#include "vor_led.h"

VorLed led;

void setup() {
    Serial.begin(9600);
    led.setId("1");
    led.setName("led-1");
    led.setDescription("Arduino onboard LED.");
}

void loop() {
    Serial.println(led.getId());
    Serial.println(led.getName());
    Serial.println(led.getDescription());
    led.toggle();
    delay(500);
    led.toggle();
    delay(500);
}
