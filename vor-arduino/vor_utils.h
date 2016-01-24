#ifndef VOR_UTILS_H
#define VOR_UTILS_H

#include <Arduino.h>

void printToSerial(Process &p) {
    char c;
    while (Serial && p.available() > 0) {
        c = p.read();
        Serial.print(c);
    }
    Serial.println();
}

void runCommand(const char* command) {
    Process p;
    p.runShellCommand(command);
    printToSerial(p);
}

// http://www.instructables.com/id/two-ways-to-reset-arduino-in-software/?ALLSTEPS
void (* resetArduino)(void) = 0;

void resetAndRebootLinino() {
    Process p;
    p.runShellCommand("/usr/bin/wifi-reset-and-reboot");
}

void rebootLinino() {
    Process p;
    p.runShellCommand("/sbin/reboot");
}

// http://forum.arduino.cc/index.php?topic=220440
void getWifiList() {
    runCommand("iwlist wlan0 scan | grep \"Address:\\|Channel:\\|Quality\\|Encryption key:\\|ESSID\\|Mode:\\|WPA\\|CCMP\\|PSK\\|TKIP\"");
}

void getWifiInfo() {
    runCommand("/usr/bin/pretty-wifi-info.lua");
}

void getNetworkInterfaceInfo() {
    runCommand("ifconfig");
}


// http://forum.arduino.cc/index.php?topic=216850
// https://github.com/sambrenner/yun-easy-wifi-switch
void connectToWifi(const char* ssid, const char* encryption, const char* password) {
    Process p;
    p.runShellCommand("/sbin/uci set network.lan=interface");
    p.runShellCommand("/sbin/uci set network.lan.proto=dhcp");
    p.runShellCommand("/sbin/uci delete network.lan.ipaddr");
    p.runShellCommand("/sbin/uci delete network.lan.netmask");
    p.runShellCommand("/sbin/uci set wireless.@wifi-iface[0].mode=sta");
    char buffer[128];
    sprintf(buffer, "/sbin/uci set wireless.@wifi-iface[0].ssid=%s", ssid);
    p.runShellCommand(buffer);
    sprintf(buffer, "/sbin/uci set wireless.@wifi-iface[0].encryption=%s", encryption);
    p.runShellCommand(buffer);
    sprintf(buffer, "/sbin/uci set wireless.@wifi-iface[0].key=%s", password);
    p.runShellCommand(buffer);
    p.runShellCommand("/sbin/uci commit wireless; /sbin/wifi");
    p.runShellCommand("/etc/init.d/network restart");

    printToSerial(p);
}

void writeLog(const char* message) {
    uint64_t ms = millis();
    uint8_t s = (ms / 1000) % 60;
    uint8_t m = (ms / (60000)) % 60;
    uint8_t h = (ms / (3600000)) % 24;

    char buffer[128]; // use String instead?
    sprintf(buffer, "echo \"%s%d:%s%d:%s%d %s\" >> /vor.log",
        h < 10 ? "0" : "", h,
        m < 10 ? "0" : "", m,
        s < 10 ? "0" : "", s,
        message);
    Process p;
    p.runShellCommand(buffer);
}

void readFile(const char* filename) {
    char buffer[128];
    sprintf(buffer, "cat %s", filename);
    Process p;
    p.runShellCommand(buffer);

    printToSerial(p);
}

void readLog() {
    readFile("/vor.log");
}

#endif
