/*
     Web client sketch for IDE v1.0.1 and w5100/w5200
     Uses POST method.
     Posted November 2012 by SurferTim
*/

#include <SPI.h>
#include <LTask.h>
#include <LWiFi.h>
#include <LWiFiClient.h>

#define WIFI_AP "Futurice-Guest-Helsinki"
#define WIFI_PASSWORD "isitfriday"
#define WIFI_AUTH LWIFI_WPA  // choose from LWIFI_OPEN, LWIFI_WPA, or LWIFI_WEP.
//#define SERVER_URL "10.3.2.102"
#define SERVER_URL "futu2.herokuapp.com"
#define SERVER_PORT 80
#define SERVER_ROUTE "/messages"

#define CLIENT_TIMEOUT 30000
#define CLIENT_DELAY 1000
#define CLIENT_USERAGENT = "LinkitONE"

#define CRLF "\r\n"

unsigned long thisMillis = 0;
unsigned long lastMillis = 0;
unsigned long delayMillis = 10000;

LWiFiClient client;
const char* msg = "{id:\"1\",type:\"room\",reserved:false,temperature:10,light:%u,dioxide:10,noise:10}";

void setup() {
    LWiFi.begin();
    //Serial.begin(115200);
    Serial.begin(9600);

    Serial.println(F("Connecting to WIFI..."));
    while (0 == LWiFi.connect(WIFI_AP, LWiFiLoginInfo(WIFI_AUTH, WIFI_PASSWORD))) {
            delay(1000);
    }
    char serout[64];
    sprintf(serout, "Connected to %s.", WIFI_AP);
    Serial.println(serout);

    Serial.println(F("Connecting to server..."));
    while (0 == client.connect(SERVER_URL, SERVER_PORT)) {
        Serial.println(F("Reconnecting to server..."));
        delay(1000);
    }
    sprintf(serout, "Connected to %s:%u.", SERVER_URL, SERVER_PORT);
    Serial.println(serout);
}

void loop() {
    int light = analogRead(A0);
    char out[128];

    thisMillis = millis();

    if (thisMillis - lastMillis > delayMillis) {
        lastMillis = thisMillis;
        sprintf(out, msg, light);
        if (!postPage(SERVER_URL, SERVER_PORT, SERVER_ROUTE, out)) {
            Serial.print(F("Fail\n"));
        } else {
            Serial.print(F("Pass\n"));
        }

    }
}


byte postPage(const char* url, int port, const char* route, const char* data)
{
    char out[64];

    Serial.println(F("Sending HTTP POST..."));
    if (client.connect(url, port) == 1) {

        sprintf(out, "POST %s HTTP/1.1\r\n", route);
        client.print(out);
        sprintf(out, "Host: %s\r\n", url);
        client.print(out);
        client.print(F("Connection: close\r\n"));
        client.print(F("User-Agent: LinkitONE\r\n"));
        client.print(F("Accept: application/json\r\n"));
        client.print(F("Content-Type: application/json\r\n"));
        //sprintf(out, "Referer: http://%s/\r\n");
        sprintf(out, "Content-Length: %u\r\n",strlen(data));
        client.print(out);
        client.print(CRLF);
        client.print(data);

    } else {
        return 0;
    }

    unsigned long timeoutStart = millis();
    char c;
    while ((client.connected() || client.available()) && ((millis() - timeoutStart) < CLIENT_TIMEOUT)) {
        if (client.available()) {
            c = client.read();
            Serial.print(c);
            timeoutStart = millis();
        } else {
            delay(CLIENT_DELAY);
        }
    }

    return 1;
}
