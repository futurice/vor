# Sound sensor

[A sound detector](https://www.sparkfun.com/products/12642) which has [three different and independent outputs](https://learn.sparkfun.com/tutorials/sound-detector-hookup-guide).

The AUDIO pin outputs the audio signal voltage fluctuation around analog input value 512 (halfway of 0 - 1023 range).

The ENVELOPE pin outputs the amplitude of the detected sound. Ways to convert the output value to any loudness units are not known. [This example](https://learn.sparkfun.com/tutorials/sound-detector-hookup-guide/software-example) may give some idea of the loudness levels.

The GATE is a digital signal which goes HIGH when the sound amplitude exceeds a certain threshold and remains LOW otherwise. It can be used as a boolean test of the presence of sound.

## Wiring example

<img src="sound_bb.png" width="320">

## Code example

```cpp
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
```
