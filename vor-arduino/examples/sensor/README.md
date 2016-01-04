# Vör Sensor

An initializable base class for sensors (digital and analog input).

## Member functions

### Constructor

```cpp
VorSensor(
    uint8_t pin,
    SENSOR_TYPE type,
    const char* id = NULL,
    const char* name = NULL,
    const char* description = NULL
);
```

Parameters:
- pin: pin number (digital pin for digital input and analog pin for analog input)
- type: ANALOG_INPUT, DIGITAL_INPUT or DIGITAL_INPUT_PULLUP (internal pull-up resistor of Arduino board)
- id, name, description: identification information for the sensor

### Setters and getters

```cpp
void setId(const char* id)
const char* getId()
```

```cpp
void setName(const char* name)
const char* getName()
```

```cpp
void setDescription(const char* description)
const char* getDescription()
```

### Signal input

```cpp
virtual int read();
```

Function for reading an input value of a digital or analog pin.

Returns:
- either digital (HIGH or LOW) or analog (0 - 1023 in case of 10-bit resolution) input value

```cpp
virtual float readProcessed();
```

Function for reading a value converted from a raw digital or analog input. Uses the ```process()``` function below by default and hence a class inheriting this class should implement the ```process()``` function.

Returns:
- any value that is converted from either digital or analog input value

```cpp
virtual int peek();
```

Function for getting the last read digital or analog input value.

Returns:
- either digital (HIGH or LOW) or analog (0 - 1023 in case of 10-bit resolution) input value

```cpp
virtual float peekProcessed();
```

Function for getting the last read and converted digital or analog input value.

Returns:
- any value that is converted from either digital or analog input value

```cpp
virtual float process(int value);
```

Function for converting a raw digital (HIGH or LOW) or analog (0 - 1023 in case of 10-bit resolution) input value to a processed value (e.g. from an analog input value (0 - 1023) to a percentage value). A class inheriting this class should implement this function if there is a need to process an input value before using it.

Returns:
- any value that is converted from either digital or analog input value

## Code example

```cpp
#include "vor_sensor.h"

#define BUTTON_PIN 2
#define DEBOUNCE 100 // milliseconds

VorSensor button(BUTTON_PIN, DIGITAL_INPUT_PULLUP);
int buttonValue = HIGH; // initial button state
uint64_t debounceTime = 0; // milliseconds

void setup() {
    Serial.begin(9600);

    button.setId("1");
    button.setName("button-1");
    button.setDescription("A simple push button.");
}

void loop() {
    Serial.println(button.getId());
    Serial.println(button.getName());
    Serial.println(button.getDescription());

    int value = button.read();
    uint64_t now = millis();

    if (now - debounceTime > DEBOUNCE && value != buttonValue) {
        debounceTime = now;
        buttonValue = value;
        if (LOW == buttonValue) {
            Serial.println("Button down");
        } else {
            Serial.println("Button up");
        }
    }
}
```
