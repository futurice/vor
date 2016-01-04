// Arduino Yun Mini 3D model for planning, http://vor.space
// ©Futurice Oy, paul.houghton@futurice.com, CC-attribution-sharealike license, http://creativecommons.org/licenses/by-sa/4.0/

$fn = 32;

length = 71.12;
width = 22.9;
height = 1.3;
header_length = 64;
header_width = 3;
header_height = 9.5;
button_radius = 1.8;

arduino_yun_mini();

module arduino_yun_mini() {
    difference() {
        color("green") board();
        union() {
            hole(x = 2.5, y = width - 4);
            hole(x = length - 2, y = 2);
        }
    }

    color("magenta") usb_power();
    color("yellow") board_space();
    color("red") leds();
    color("blue") button(y = 6.5);
    color("blue") button(y = 10.3);
    color("violet") bottom_button(y = 10.3);
    color("orange") hull() {
         bottom_slot(y = 8);
         bottom_slot(y = width - 8);
    }
    color("black") header(x=2, z=height, y=0);
    color("black") header(x=2, z=height, y=width-header_width);
    color("silver") header(x=2, z=-9.2, y=0);
    color("silver") header(x=2, z=-9.2, y=width-header_width);
}

module board() {
    cube([length, width, height]);
}

module usb_power() {
    translate([-102, (width - 15) / 2, height - 5]) cube([100.5 + length, 15, 3 + 8]);
}

module board_space() {
    hull() {
        translate([0, 0, height - 5]) cube([length, width, 16.5]);
        translate([length, (width-5)/2, height-5]) cube([8, 5, 10]);
    }
}

module header(x=2, y=0, z=0) {
    translate([x, y, z]) cube([header_length, header_width, header_height]);
}

module leds() {
    translate([header_length, 0, height]) cube([2, width/2, 1]);
}

module button(y = 0) {
    translate([length - 2.75, y, height]) cylinder(r=button_radius, h=100);    
}

module bottom_button(bx = length - 2, by = 7.5) {
    translate([bx, by, -100]) cylinder(r=button_radius, h=100); 
}

module bottom_slot(y = 0) {
    hull() {
        translate([length - 10, y, -100]) cylinder(r=2, h=100); 
        translate([10, y, -100]) cylinder(r=2.2, h=100); 
    }
}

module hole(x = 0, y = 0) {
    translate([x, y, -9]) cylinder(h = 10 + height, r = 2.5/2);
}
