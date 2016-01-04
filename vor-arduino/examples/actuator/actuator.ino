#include "vor_actuator.h"

#define LED_PIN 13

VorActuator led(LED_PIN, DIGITAL_OUTPUT);

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
    led.write(HIGH);
    delay(500);
    led.write(LOW);
    delay(500);
}
