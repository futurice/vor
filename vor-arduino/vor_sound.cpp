/*
    Sound detector with one digital (threshold) and two analog outputs
    (amplitude, AC audio signal around value 512 (0-1023)).
*/

#include "vor_sound.h"

VorSound::VorSound(int audioPin, int envelopePin, int gatePin) {
    _audio = -1 == audioPin ? NULL : new VorSensor(audioPin, ANALOG_INPUT);
    _envelope = -1 == envelopePin ? NULL : new VorSensor(envelopePin, ANALOG_INPUT);
    _gate = -1 == gatePin ? NULL : new VorSensor(gatePin, DIGITAL_INPUT_PULLUP);
}

VorSound::~VorSound() {
    delete _audio;
    delete _envelope;
    delete _gate;
    _audio = NULL;
    _envelope = NULL;
    _gate = NULL;
}

VorSensor* VorSound::getAudio() {
    return _audio;
}

VorSensor* VorSound::getEnvelope() {
    return _envelope;
}

VorSensor* VorSound::getGate() {
    return _gate;
}
