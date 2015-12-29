#include <Bridge.h>
#include <YunClient.h>

#include "HttpClient.h"
#include "vor_led.h"
#include "vor_switch.h"

#define SERVER_URL "rubix.futurice.com"
#define SERVER_PORT 80
#define SERVER_PATH "/messages"

#define USERAGENT "yunmini02"

#define TIMEOUT 20000
#define DELAY 500
#define INTERVAL 30000

#define MSG_FORMAT "{\"id\":\"button-food\",\"type\":\"button\"}"

#define SWITCH_PIN 2

YunClient client;
HttpClient http(client);

VorLed led;
VorSwitch vorSwitch(SWITCH_PIN);
int state = HIGH;
uint64_t intervalTime = 0;

void setup() {
    Serial.begin(9600);

    led.turnOn();
    Bridge.begin();
    led.turnOff();
}

void loop() {
    int value = vorSwitch.read();

    uint64_t now = millis();
    if (state != value || now - intervalTime > INTERVAL) {
        state = value;
        intervalTime = now;
        if (HIGH == state) {
            post();
        }
    }
}

void post() {
    int res = http.post(SERVER_URL, SERVER_PATH, USERAGENT, TEXT_PLAIN, MSG_FORMAT);
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
    Serial.println();
}
