# Vör sensor resources

## Description
- Microcontroller reads sensor data
- Microcontroller processes the sensor data
- Microcontroller sends the processed data to server with HTTP POST in JSON format
- Microcontrollers have either (wired) Ethernet (room) or Wi-Fi (toilet) connectivity
- Microcontrollers are powered from the grid or with PoE (Power over Ethernet)

## Microcontrollers
- [Arduino Mega2560](http://www.arduino.org/products/boards/4-arduino-boards/arduino-mega-2560)
- [Arduino Leonardo](http://www.arduino.org/products/boards/4-arduino-boards/arduino-leonardo)
- [Arduino Yun](http://www.arduino.org/products/boards/4-arduino-boards/arduino-yun)
- [Arduino Yun Mini](http://www.arduino.org/products/boards/4-arduino-boards/arduino-yun-mini)
- [MediaTek LinkIt ONE](http://www.seeedstudio.com/wiki/LinkIt_ONE)

## Sensors

### Room
- Temperature (beacons)
- [Humidity](https://www.sparkfun.com/products/9569)
- [Ambient light](https://www.sparkfun.com/products/8688)
- [Sound](https://www.sparkfun.com/products/12642)
- [Particle](https://www.sparkfun.com/products/9689)

### Toilet
- [Methane](https://www.sparkfun.com/products/9404)
- [Motion](https://www.sparkfun.com/products/13285)

### Pool table
- Button (player needed)

### Kitchen table
- Switch (food)

### Sauna
- [Electric current](https://www.sparkfun.com/products/11005)

## Installation

### Arduino SDK
- Download and install the [Arduino SDK](https://www.arduino.cc/en/Main/Software)
    - You may need to (find and) install USB drivers more or less manually
- When you run the Arduino IDE you need to setup:
    - Tools > Board (select the board you are developing for)
    - Tools > Port
- Upload a sketch (*.ino) to the board by clicking the arrow button in the toolbar
- You may print debugging info to Tools > Serial Monitor

### LinkIt ONE
- For installing LinkIt ONE SDK (on top of the Arduino SDK) follow [this guide](http://www.seeedstudio.com/wiki/LinkIt_ONE) or [this guide](http://labs.mediatek.com/forums/posts/list/559.page)

### Arduino Yun
- Turn on Yun by connecting the mini USB cable
- [The first time](https://www.arduino.cc/en/Guide/ArduinoYun#toc14) Yun is turned on its Wi-Fi is unconfigured and Yun creates a network called LININO-*
    - On your computer connect to the network and go to [http://arduinoyun.local](http://arduinoyun.local) or [192.168.240.1](192.168.240.1) with a browser
        - The default password of the configuration page is ```doghunter```
        - Once logged in click ```Configure``` and configure Yun to connect to the desired network
    - After configuring Yun's Wi-Fi connect your computer to the same network Yun is connected to
        - You may now access the configuration page at [http://arduinoyun.local](http://arduinoyun.local)
        - You may now [upload your sketch over Wi-Fi](https://www.arduino.cc/en/Guide/ArduinoYun#toc15) by selecting the corresponding Port in the Arduino IDE
    - You may [reset](https://www.arduino.cc/en/Guide/ArduinoYun#toc6) the Wi-Fi configuration by holding the ```WLAN RST``` button on the board for more that 5 seconds and less than 30 seconds
