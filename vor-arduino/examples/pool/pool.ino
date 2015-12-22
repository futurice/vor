#include <Bridge.h>
#include <YunClient.h>

#include "HttpClient.h"
#include "vor_led.h"
#include "vor_button.h"

#define SERVER_URL "rubix.futurice.com"
#define SERVER_PORT 80
#define SERVER_PATH "/messages"

#define USERAGENT "yunmini01"

#define TIMEOUT 20000
#define DELAY 500
#define INTERVAL 5000

#define MSG_FORMAT "{\"id\":\"button-pool\",\"type\":\"button\"}"

#define BUTTON_PIN 2

YunClient client;
HttpClient http(client);

VorLed led;
VorButton button(BUTTON_PIN);
int buttonState = button.peek();

void setup() {
    Serial.begin(9600);

    led.turnOn();
    Bridge.begin();
    led.turnOff();
}

void loop() {
    int state = button.read();

    if (buttonState != state) {
        buttonState = state;

        if (LOW == buttonState) {
            post();
            led.turnOn();
        } else {
            led.turnOff();
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
}


