#include <Bridge.h>
#include <YunClient.h>

#include "vor_utils.h"

#include "vor_led.h"
#include "vor_switch.h"

#define INTERVAL 30000

#define PAYLOAD "{\"id\":\"button-food\",\"type\":\"button\"}"

#define SWITCH_PIN 2

YunClient client;

VorLed led;
VorSwitch vorSwitch(SWITCH_PIN);
int switchValue = LOW;
uint64_t intervalTime = 0;

void setup() {
    Serial.begin(9600);

    led.turnOn();
    Bridge.begin();
    led.turnOff();
}

void loop() {
    int value = vorSwitch.read();

    uint64_t now = millis();
    if (switchValue != value || now - intervalTime > INTERVAL) {
        switchValue = value;
        intervalTime = now;
        if (HIGH == switchValue) {
            post(client, PAYLOAD);
        }
    }
}
