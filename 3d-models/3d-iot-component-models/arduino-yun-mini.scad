// Arduino Yun Mini 3D model for planning

$fn = 32;

length = 71.12;
width = 22.86;
height = 1.3;
header_length = 64;

module board() {
    cube([length, width, height]);
}

module usb_power() {
    translate([-2, (width - 5.8) / 2, height]) cube([7.5, 5.8, 3]);
}

module header(y = 0, z = 0) {
    translate([2, y, z]) cube([header_length, 2.6, 9.2]);
}

module leds() {
    translate([header_length, 0, height]) cube([2, width/2, 1]);
}

module button(y = 0) {
    translate([length - 2.75, y, height]) cylinder(r = 3/2, h = 1);    
}

module bottom_button() {
    translate([length - 2, 7.5, -1]) cylinder(r = 3/2, h = 1);    
}

module hole(x = 0, y = 0) {
    translate([x, y, -9]) cylinder(h = 10 + height, r = 2.5/2);
}


module arduino_yun_mini() {
    difference() {
        color("green") board();
        union() {
            hole(x = 2.5, y = width - 4);
            hole(x = length - 2, y = 2);
        }
    }

    color("magenta") usb_power();
    color("red") leds();
    color("blue") button(y = 6.5);
    color("blue") button(y = 10.3);
    color("violet") bottom_button(y = 10.3);
    color("black") header(z = height, y = 0);
    color("black") header(z = height, y = width - 2.6);
    color("silver") header(z = -9.2, y = 0);
    color("silver") header(z = -9.2, y = width - 2.6);
}

arduino_yun_mini();