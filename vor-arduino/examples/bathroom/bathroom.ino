#include <Bridge.h>
#include <YunClient.h>

#include "HttpClient.h"
#include "vor_env.h"

#include "vor_led.h"
#include "vor_motion.h"
#include "vor_methane.h"

#define INTERVAL 30000

#define PAYLOAD_FORMAT "{\"id\":\"toilet8am\",\"type\":\"toilet\",\"reserved\":%s,\"methane\":%s}"

#define MOTION1_PIN 2
#define MOTION2_PIN 3
#define METHANE_PIN A0

YunClient client;
HttpClient http(client, SERVER_URL, SERVER_PATH, CLIENT_USERAGENT);

VorLed led;
VorMotion motion1(MOTION1_PIN);
VorMotion motion2(MOTION2_PIN);
VorMethane methane(METHANE_PIN);

int prevMotion1Value = HIGH;
int prevMotion2Value = HIGH;

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

    int motion1Value = motion1.read();
    int motion2Value = motion2.read();
    float methaneValue = methane.readProcessed();

    bool change = (prevMotion1Value != motion1Value || prevMotion2Value != motion2Value) && // some change
        ( (prevMotion1Value == HIGH && prevMotion2Value == HIGH) || // previously no motion
          (motion1Value == HIGH && motion2Value == HIGH) ); // at the moment no motion

    prevMotion1Value = motion1Value;
    prevMotion2Value = motion2Value;
    bool reservedValue = motion1Value == LOW || motion2Value == LOW;

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
