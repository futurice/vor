#include "vor_motion.h"
#include "vor_led.h"

#define MOTION_PIN 2

VorMotion motion(MOTION_PIN);
VorLed led;

int motionState = motion.peek();

void setup() {
    Serial.begin(9600);
}

void loop() {
    int state = motion.read();

    if (motionState != state) {
        motionState = state;
        Serial.println(motionState);

        if (LOW == motionState) {
            led.turnOn();
        } else {
            led.turnOff();
        }
    }
}
