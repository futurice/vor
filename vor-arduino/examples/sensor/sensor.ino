#include "vor_sensor.h"

#define BUTTON_PIN 2
#define DEBOUNCE 100 // milliseconds

VorSensor button(BUTTON_PIN, DIGITAL_INPUT_PULLUP);
int buttonValue = HIGH; // initial button state
uint64_t debounceTime = 0; // milliseconds

void setup() {
    Serial.begin(9600);

    button.setId("1");
    button.setName("button-1");
    button.setDescription("A simple push button.");
}

void loop() {
    int value = button.read();
    uint64_t now = millis();

    if (now - debounceTime > DEBOUNCE && value != buttonValue) {
        debounceTime = now;
        buttonValue = value;

        Serial.println(button.getId());
        Serial.println(button.getName());
        Serial.println(button.getDescription());

        if (LOW == buttonValue) {
            Serial.println("Button down");
        } else {
            Serial.println("Button up");
        }
    }
}
