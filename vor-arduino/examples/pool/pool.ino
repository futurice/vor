#include <Bridge.h>
#include <YunClient.h>

#include "HttpClient.h"
#include "vor_env.h"

#include "vor_led.h"
#include "vor_button.h"

#define PAYLOAD "{\"id\":\"button-pool\",\"type\":\"button\"}"

#define BUTTON_PIN 2

YunClient client;
HttpClient http(client, SERVER_URL, SERVER_PATH, CLIENT_USERAGENT);

VorLed led;
VorButton button(BUTTON_PIN);
int buttonValue = button.peek();

void setup() {
    Serial.begin(9600);

    led.turnOn();
    Bridge.begin();
    led.turnOff();

    http.setClientId("button-pool");
}

void loop() {
    int value = button.read();

    if (buttonValue != value) {
        buttonValue = value;

        if (LOW == buttonValue) {
            http.post(PAYLOAD);
        }
    }

    http.postKeepAlive();
}
