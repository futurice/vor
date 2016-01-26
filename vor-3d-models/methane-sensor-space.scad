// A CNG-Methane sensor MQ-4 ordered from https://www.sparkfun.com/products/9404
// Part of Vör, http://vor.space by Futurice
// ©Futurice Oy, paul.houghton@futurice.com, CC-attribution-sharealike license, http://creativecommons.org/licenses/by-sa/4.0/

$fn = 32;

pin_length = 8;

// Uncomment one or the other depending on the use case shape desired
//methane_sensor();
methane_sensor_space();

module center() {
    hull() {
        cylinder(h = 6.4, r = 19/2);
        cylinder(h = 8.4, r = 15/2);
    }
}

module top() {
    translate([0, 0, 18 - 16/2])
        sphere(r=16/2);
}

module pin(theta = 0) {
    color("black") rotate([180, 0, theta]) {
        translate([6, 0, 0])
            cylinder(r=.3, h=pin_length);
    }
}

module pin_space(radius = 7) {
    rotate([180, 0, 0]) {
        cylinder(r=radius, h=pin_length + 1);
    }
}

module methane_sensor() {
    color("grey") center();
    color("violet") top();
    pin(theta = 0);
    pin(theta = 45);
    pin(theta = -45);
    pin(theta = 180);
    pin(theta = 180 + 45);
    pin(theta = 180 - 45);
}

module methane_sensor_space() {
    color("grey") center();
    color("violet") top();
    color("black") pin_space();
}
