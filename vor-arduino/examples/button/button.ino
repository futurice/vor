#include "vor_button.h"
#include "vor_led.h"

#define BUTTON_PIN 2

VorButton button(BUTTON_PIN);
VorLed led;

int buttonState = button.peek();

void setup() {
    Serial.begin(9600);
    while (!Serial);
}

void loop() {
    int state = button.read();

    if (buttonState != state) {
        buttonState = state;
        Serial.println(buttonState);

        if (LOW == buttonState) {
            led.turnOn();
        } else {
            led.turnOff();
        }
    }
}
