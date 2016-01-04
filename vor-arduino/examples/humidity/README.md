# Humidity sensor

[The humidity sensor](https://www.sparkfun.com/products/9569) reports relative humidity in percents. The output value of the sensor has to be adjusted with temperature value in celcius degrees according to the following [equation (see page 2)](https://www.sparkfun.com/datasheets/Sensors/Weather/SEN-09569-HIH-4030-datasheet.pdf):

float relativeHumidity = sensorValue / (1.0546 - 0.00216 * temperature);

## Wiring example

<img src="humidity_bb.png" width="320">

## Code example

```cpp
#include "vor_humidity.h"
#include "vor_led.h"

VorHumidity humidity(A0);
VorLed led;

void setup() {
    Serial.begin(9600);
}

void loop() {
    float value = humidity.readProcessed();
    Serial.println(value);
    delay(1000);
}
```
