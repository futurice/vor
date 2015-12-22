#include <Bridge.h>
#include <YunClient.h>

#include "HttpClient.h"
#include "vor_led.h"
#include "vor_motion.h"
#include "vor_methane.h"

#define SERVER_URL "rubix.futurice.com"
#define SERVER_PORT 80
#define SERVER_PATH "/messages"

#define USERAGENT "yunmini02"

#define TIMEOUT 20000
#define DELAY 500
#define INTERVAL 5000

#define MSG_FORMAT "{\"id\":\"toilet8am\",\"type\":\"toilet\",\"reserved\":%s,\"methane\":%d}"

#define MOTION1_PIN 2
#define MOTION2_PIN 3
#define METHANE_PIN A0

YunClient client;
HttpClient http(client);

VorLed led;
VorMotion motion1(MOTION1_PIN, 10000);
//VorMotion motion2(MOTION2_PIN);
VorMethane methane(METHANE_PIN);

uint64_t intervalTime = 0;

void setup() {
    Serial.begin(9600);
    while (!Serial);

    led.turnOn();
    Bridge.begin();
    led.turnOff();
}

void loop() {
    uint64_t now = millis();

    if (now - intervalTime > INTERVAL) {
        intervalTime = now;
        int motion1Value = motion1.read();
        //int motion2Value = motion2.read();
        float methaneValue = methane.readProcessed();

        bool reservedValue = motion1Value == LOW;

        post(reservedValue, methaneValue);
    }
}

void post(bool reserved, float methane) {
    char buffer[128];
    const char* reservedStr = reserved ? "true" : "false";
    int methaneInt = round(methane);
    sprintf(buffer, MSG_FORMAT, reservedStr, methaneInt);

    int res = http.post(SERVER_URL, SERVER_PATH, USERAGENT, TEXT_PLAIN, buffer);
    if (0 == res) { // HTTP_SUCCESS in HttpClient.h
        int code = http.responseStatusCode();
        Serial.println(code);
        http.skipResponseHeaders();
        uint64_t now = millis();
        char c;
        while ((http.connected() || http.available()) && ((millis() - now) < TIMEOUT)) {
            if (http.available()) {
                c = http.read();
                Serial.print(c);
                now = millis();
            } else {
                delay(DELAY);
            }
        }
    }
    http.stop();
}


