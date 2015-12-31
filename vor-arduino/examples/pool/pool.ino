#include <Bridge.h>
#include <YunClient.h>

#include "vor_utils.h"

#include "vor_led.h"
#include "vor_button.h"

#define PAYLOAD "{\"id\":\"button-pool\",\"type\":\"button\"}"

#define BUTTON_PIN 2

YunClient client;

VorLed led;
VorButton button(BUTTON_PIN);
int buttonValue = button.peek();

void setup() {
    Serial.begin(9600);

    led.turnOn();
    Bridge.begin();
    led.turnOff();
}

void loop() {
    int value = button.read();

    if (buttonValue != value) {
        buttonValue = value;

        if (LOW == buttonValue) {
            post(client, PAYLOAD);
        }
    }
}
