#include "vor_rgbled.h"

#define RED_PIN 3
#define GREEN_PIN 5
#define BLUE_PIN 6

VorRgbLed led(RED_PIN, GREEN_PIN, BLUE_PIN);

void (* writeLed)(int) = led.writeRed;

void setup() {
    Serial.begin(9600);
    while (!Serial);
}

void loop() {
    for (int fadeValue = 0 ; fadeValue <= 255; fadeValue += 5) {
        writeLed(fadeValue);
        delay(30);
    }

    for (int fadeValue = 255 ; fadeValue >= 0; fadeValue -= 5) {
        writeLed(fadeValue);
        delay(30);
    }
}
