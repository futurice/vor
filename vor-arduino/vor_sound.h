/*
    Sound detector with one digital (threshold) and two analog outputs
    (amplitude, AC audio signal around value 512 (0-1023)).
*/

#ifndef VOR_SOUND_H
#define VOR_SOUND_H

#include "vor_sensor.h"

class VorSound {
public:
    VorSound(int audioPin = -1, int envelopePin = -1, int gatePin = -1);
    ~VorSound();

    VorSensor* getAudio();
    VorSensor* getEnvelope();
    VorSensor* getGate();

private:
    VorSensor* _audio;
    VorSensor* _envelope;
    VorSensor* _gate;
};

#endif
