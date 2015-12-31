#include "vor_led.h"
#include "vor_sound.h"

#define AUDIO_PIN A0
#define ENVELOPE_PIN A1
#define GATE_PIN 2

VorLed led;
VorSound sound(AUDIO_PIN, ENVELOPE_PIN, GATE_PIN);

void setup() {
    Serial.begin(9600);
}

void loop() {
    int audioValue = sound.getAudio()->read();
    int envelopeValue = sound.getEnvelope()->read();
    int gateValue = sound.getGate()->read();

    Serial.print("Audio: ");
    Serial.print(audioValue);
    Serial.print(", Envelope: ");
    Serial.println(envelopeValue);

    if (HIGH == gateValue) {
        led.turnOn();
    } else {
        led.turnOff();
    }
    delay(100);
}
