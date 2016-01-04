# Vör Actuator

An initializable base class for actuators (digital and analog output).

## Member functions

### Constructor

```cpp
VorActuator(
    uint8_t pin,
    ACTUATOR_TYPE type,
    const char* id = NULL,
    const char* name = NULL,
    const char* description = NULL
)
```

Parameters:
- pin: pin number (digital pin for digital output and PWM pin for analog output)
- type: ANALOG_OUTPUT or DIGITAL_OUTPUT
- id, name, description: identification information for the actuator

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

### Signal output

```cpp
virtual void write(int value);
```

Function for outputting a value to a digital pin.

Parameters:
- value: either digital (HIGH or LOW) or analog (0 - 255) output value

```cpp
virtual void writeProcessed(float value);
```

Function for outputting a converted value to a digital pin. Uses the ```process()``` function below by default and hence a class inheriting this class should implement the ```process()``` function.

Parameters:
- value: any value that is converted to either digital (HIGH or LOW) or analog (0 - 255) output value

```cpp
virtual int process(float value);
```

Function for converting a value to be outputted to a digital pin (e.g. from a percentage value to an analog output value (0 - 255)). A class inheriting this class should implement this function if there is a need to convert a value before outputting it.

Parameters:
- value: any value that is converted to either digital (HIGH or LOW) or analog (0 - 255) output value

## Code example

```cpp
#include "vor_actuator.h"

#define LED_PIN 13

VorActuator led(LED_PIN, DIGITAL_OUTPUT);

void setup() {
    Serial.begin(9600);

    led.setId("1");
    led.setName("led-1");
    led.setDescription("Arduino onboard LED.");
}

void loop() {
    Serial.println(led.getId());
    Serial.println(led.getName());
    Serial.println(led.getDescription());
    led.write(HIGH);
    delay(500);
    led.write(LOW);
    delay(500);
}
```
