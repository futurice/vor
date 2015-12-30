#ifndef VOR_UTILS_H
#define VOR_UTILS_H

#include <Client.h>

#include "vor_env.h"
#include "HttpClient.h"

// http://www.instructables.com/id/two-ways-to-reset-arduino-in-software/?ALLSTEPS
void (* resetArduino)(void) = 0;

void post(Client &client, char* payload) {
    HttpClient http(client);

    int res = http.post(SERVER_URL, SERVER_PATH, CLIENT_USERAGENT, TEXT_PLAIN, payload);
    if (0 == res) { // HTTP_SUCCESS in HttpClient.h
        http.skipResponseHeaders();
        uint64_t now = millis();
        while ((http.connected() || http.available()) && ((millis() - now) < CLIENT_TIMEOUT)) {
            if (http.available()) {
                http.read();
                now = millis();
            } else {
                delay(CLIENT_DELAY);
            }
        }
    }
    http.stop();
}

#endif
