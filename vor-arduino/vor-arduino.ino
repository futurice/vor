#include <Bridge.h>
#include <BridgeClient.h>

#include "Arduino-Websocket/WebSocketClient.h"

#include "vor_utils.h"
#include "vor_env.h"
#include "vor_led.h"
#include "vor_motion.h"
#include "vor_methane.h"

#define MAX_ATTEMPTS 10
#define INTERVAL 30000
#define HEARTBEAT_INTERVAL 25000

#define MOTION_PIN 2
#define METHANE_PIN A0

#define MESSAGE_FORMAT "42[\"message\",{\"id\":\"toilet8am\",\"type\":\"toilet\",\"reserved\":%s,\"methane\":%s}]"

BridgeClient client;
WebSocketClient ws(SERVER_URL);

VorLed led;
VorMotion motion(MOTION_PIN);
VorMethane methane(METHANE_PIN);

int prevMotionValue = HIGH;

uint64_t heartbeatTimestamp = 0;
uint64_t intervalTimestamp = 0;

uint8_t attempts = 0;
bool wifiRestarted = false;
bool lininoRebooted = false;

void setup() {
    Serial.begin(115200);
    bridge();
}

void loop() {
    if (client.connected()) {
        uint64_t now = millis();
        int motionValue = motion.read();
        float methaneValue = methane.readProcessed();
        bool change = prevMotionValue != motionValue;
        prevMotionValue = motionValue;
        bool reservedValue = motionValue == LOW;
        if (change || now - intervalTimestamp > INTERVAL) {
            intervalTimestamp = now;

            const char* reserved = reservedValue ? "true" : "false";
            char methane[8];
            dtostrf(methaneValue, 8, 2, methane);
            char message[128];
            sprintf(message, MESSAGE_FORMAT, reserved, methane);
            ws.sendData(message);
        }
        if ((now - heartbeatTimestamp) > HEARTBEAT_INTERVAL) {
            heartbeatTimestamp = now;
            ws.sendData("2");
            // TODO: check server response
        }
    } else {
        led.turnOn();
        Serial.println(0);
        if (!client.connect(SERVER_URL, SERVER_PORT)) {
            if (!wifiRestarted) {
                writeLog("Restarting Wi-Fi");
                connectToWifi(WIFI_SSID, WIFI_ENCRYPTION, WIFI_PASSWORD);
                delay(60000);
                wifiRestarted = true;
            } else if (!lininoRebooted) {
                writeLog("Rebooting Linino");
                resetAndRebootLinino();
                bridge();
                lininoRebooted = true;
            } else {
                writeLog("Resetting Arduino");
                resetArduino();
            }
        } else {
            wifiRestarted = false;
            lininoRebooted = false;
            if (ws.handshake(client)) {
                ws.sendData("5");
                // TODO: check server response
                led.turnOff();
            }
        }
    }
}

void bridge() {
    led.turnOn();
    delay(60000); // wait until linino has booted
    Bridge.begin();
    led.turnOff();
}
