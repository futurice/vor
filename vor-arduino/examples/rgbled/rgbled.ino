#include "vor_rgbled.h"

#define RED_PIN 3
#define GREEN_PIN 5
#define BLUE_PIN 6

#define ALL 99

VorRgbLed led(RED_PIN, GREEN_PIN, BLUE_PIN);

int leds[] = { RED_PIN, GREEN_PIN, BLUE_PIN, ALL };
int index = -1;

void setup() {
    Serial.begin(9600);
}

void loop() {
    int selectedLed = leds[++index % 4];

    for (int fadeValue = 0 ; fadeValue <= 255; fadeValue += 5) {
        writeLed(selectedLed, fadeValue);
        delay(30);
    }

    for (int fadeValue = 255 ; fadeValue >= 0; fadeValue -= 5) {
        writeLed(selectedLed, fadeValue);
        delay(30);
    }
}

void writeLed(int selectedLed, int value) {
    if (RED_PIN == selectedLed) {
        led.writeRed(value);
    } else if (GREEN_PIN == selectedLed) {
        led.writeGreen(value);
    } else if (BLUE_PIN == selectedLed) {
        led.writeBlue(value);
    } else {
        led.write(value, value, value);
    }
}
