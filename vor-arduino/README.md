# Vör Sensor and Actuator Resources

## Description
This part of the Vör project provides simple to use classes that wrap the handling of different sensors and actuators on Arduino based microcontrollers. This library uses some other external libraries to ease the use of [HTTP clients](https://github.com/amcewen/HttpClient), WebSocket clients (planned), [JSON messages](https://github.com/bblanchon/ArduinoJson) (planned) and [protothreads](https://github.com/ivanseidel/ArduinoThread) (or [this](http://dunkels.com/adam/pt/) or [this](http://playground.arduino.cc/Code/TimedAction), planned).

The microcontrollers:
- read raw sensor data, processes it and sends the processed data as a JSON formatted message to a server
- have either (wired) Ethernet (room) or Wi-Fi (bathroom) connectivity
- are powered mainly from the grid thus they require a stable power supply

## Microcontrollers

Current work is mostly focused on the Arduino Yun Mini board. Other boards we are testing include:

- [Arduino Mega2560](http://www.arduino.org/products/boards/4-arduino-boards/arduino-mega-2560)
- [Arduino Leonardo](http://www.arduino.org/products/boards/4-arduino-boards/arduino-leonardo)
- [Arduino Yun](http://www.arduino.org/products/boards/4-arduino-boards/arduino-yun)
- [Arduino Yun Mini](http://www.arduino.org/products/boards/4-arduino-boards/arduino-yun-mini)
- [MediaTek LinkIt ONE](http://www.seeedstudio.com/wiki/LinkIt_ONE)

## Sensors

### Room sensor
- Temperature ([Estimote](http://estimote.com/) beacons)
- [Particle sensor](https://www.sparkfun.com/products/9689) for measuring air quality
- [Humidity sensor](https://www.sparkfun.com/products/9569) for measuring relative humidity
- [Ambient light sensor](https://www.sparkfun.com/products/8688) for measuring ambient light level
- [Sound detector](https://www.sparkfun.com/products/12642) for measuring ambient noise level

### Toilet sensor
- [Methane sensor](https://www.sparkfun.com/products/9404) for measuring air quality
- [Motion sensor](https://www.sparkfun.com/products/13285) for checking if the bathroom is occupied or not

### Pool table button
- Push button that sends a message to the server
- Camera near the pool table that records video the whole time reacts to the button message and creates a video clip of the last 30 seconds of the pool game
- The camera is associated with Vör service, but is not technically linked to the Arduino implementation

### Food table switch
- Switch to notify users if there is some food served on the kitchen table
- Webcam attached to Raspberry Pi reacts to the switch message and takes a picture of the food and sends it to the server
- The webcam is associated with Vör service, but is not technically linked to the Arduino implementation

### Sauna
- [Electric current](https://www.sparkfun.com/products/11005) for checking if the sauna is turned on
- Temperature sensor for checking if the sauna is warm

## Installation
1. Buy an Arduino Yun Mini board and sensors
2. Upload a corresponding sketch to the board (modify the sketch if you want)
3. 3D print a case for the sensors
4. Solder or wire-wrap the sensors and put them inside the case
5. Deploy the sensor
6. Use the Vör app to use servises tied to the sensor

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

### Arduino Yun and Arduino Yun Mini
- Turn on Yun by connecting the mini USB cable
- [The first time](https://www.arduino.cc/en/Guide/ArduinoYun#toc14) Yun is turned on its Wi-Fi is unconfigured and Yun creates a network called LININO-*
    - On your computer connect to the network and go to [http://linino.local](http://linino.local) or [192.168.240.1](192.168.240.1) with a browser
        - The default password of the configuration page is ```doghunter```
        - Once logged in click ```Configure``` and configure Yun to connect to the desired network
    - After configuring Yun's Wi-Fi connect your computer to the same network Yun is connected to
        - You may now access the configuration page at [http://name_of_the_board.local](http://name_of_the_board.local) where ```name_of_the_board``` is the one set when configuring the board (e.g. voryunmini01).
        - You may now [upload your sketch over Wi-Fi](https://www.arduino.cc/en/Guide/ArduinoYun#toc15) by selecting the corresponding Port in the Arduino IDE
    - You may [reset](https://www.arduino.cc/en/Guide/ArduinoYun#toc6) the Wi-Fi configuration by holding the ```WLAN RST``` button on the board for more that 5 seconds and less than 30 seconds

## Major Components List
* Arduino Yun Mini [About 60€](http://de.rs-online.com/web/p/entwicklungskits-prozessor-mikrocontroller/8659007/)
* Raspberry Pi 2 [About 33€](http://de.rs-online.com/web/p/entwicklungskits-prozessor-mikrocontroller/8326274/) plus extras
* PIR motion sensor [About 5€](https://www.sparkfun.com/products/13285)
* Ambient light sensor [About 5€](https://www.sparkfun.com/products/8688)
* Noise level sensor [About 22€](https://www.sparkfun.com/products/12642)
* Humidity [About 15€](https://www.sparkfun.com/products/9569)
* Air particle sensor [About 36€](https://www.sparkfun.com/products/9689)
* Methane/biogas [About 20€](https://www.sparkfun.com/products/9404)
