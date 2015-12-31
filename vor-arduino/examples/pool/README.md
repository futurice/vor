# Pool table button

A simple [push button](../button) which sends a JSON string message to the server via HTTP POST when it is pushed.

The purpose of the button is to create a video clip of the last 30 seconds of the game. The video is created by a client that is connected to a camera that records a video of the pool table constantly. The client reacts to the message sent by the button and then creates a video clip.

## Wiring example

<img src="../button/button_bb.png" width="320">

## Code example

```cpp
#include <Bridge.h>
#include <YunClient.h>

#include "vor_utils.h"

#include "vor_led.h"
#include "vor_button.h"

#define PAYLOAD "{\"id\":\"button-pool\",\"type\":\"button\"}"

#define BUTTON_PIN 2

YunClient client;

VorLed led;
VorButton button(BUTTON_PIN);
int buttonValue = button.peek();

void setup() {
    Serial.begin(9600);

    led.turnOn();
    Bridge.begin();
    led.turnOff();
}

void loop() {
    int value = button.read();

    if (buttonValue != value) {
        buttonValue = value;

        if (LOW == buttonValue) {
            post(client, PAYLOAD);
        }
    }
}
```
