#include <Bridge.h>
#include <YunClient.h>

#include "HttpClient.h"
#include "vor_env.h"

#include "vor_led.h"
#include "vor_motion.h"
#include "vor_methane.h"

#define INTERVAL 30000

#define PAYLOAD_FORMAT "{\"id\":\"toilet8am\",\"type\":\"toilet\",\"reserved\":%s,\"methane\":%s}"

#define MOTION_PIN 2
#define METHANE_PIN A0

YunClient client;
HttpClient http(client, SERVER_URL, SERVER_PATH, CLIENT_USERAGENT);

VorLed led;
VorMotion motion(MOTION_PIN);
VorMethane methane(METHANE_PIN);

int prevMotionValue = HIGH;

uint64_t intervalTime = 0;

void setup() {
    Serial.begin(9600);

    led.turnOn();
    Bridge.begin();
    led.turnOff();

    http.setClientId("toilet8am");
}

void loop() {
    uint64_t now = millis();

    int motionValue = motion.read();
    float methaneValue = methane.readProcessed();

    bool change = prevMotionValue != motionValue;

    prevMotionValue = motionValue;
    bool reservedValue = motionValue == LOW;

    if (change || now - intervalTime > INTERVAL) {
        intervalTime = now;

        const char* reserved = reservedValue ? "true" : "false";
        char methane[16];
        dtostrf(methaneValue, 16, 2, methane);
        char message[128];
        sprintf(message, PAYLOAD_FORMAT, reserved, methane);

        http.post(message);
    }

    http.postKeepAlive();
}
