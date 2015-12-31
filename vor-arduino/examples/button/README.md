# Button

A simple push button which uses the [internal pull-up resistor](https://www.arduino.cc/en/Tutorial/InputPullupSerial) of Arduino board.

The button has a 100 milliseconds [software debouncing](http://playground.arduino.cc/Learning/SoftwareDebounce). You may want to implement [hardware debouncing](http://www.jeremyblum.com/2011/03/07/arduino-tutorial-10-interrupts-and-hardware-debouncing/) because software debouncing does not solve the bouncing problem completely.

## Wiring example

<img src="button_bb.png" width="320">

## Code example

```cpp
#include "vor_button.h"
#include "vor_led.h"

#define BUTTON_PIN 2

VorButton button(BUTTON_PIN);
VorLed led;

int buttonState = button.peek();

void setup() {
    Serial.begin(9600);
    while (!Serial);
}

void loop() {
    int state = button.read();

    if (buttonState != state) {
        buttonState = state;
        Serial.println(buttonState);

        if (LOW == buttonState) {
            led.turnOn();
        } else {
            led.turnOff();
        }
    }
}
```
