// Arduino Yun Mini 3D model for planning

$fn = 16;

length = 73;
width = 54;
height = 1.6;

// Uncommont to test the module
//arduino_yun();

module board() {
    difference() {
        cube([length, width, height]);
        union() {
            translate([3.3, -1,  -1]) cube([7.4-3.3, 1+1, 3]);
            translate([length - 2.15, -1,  -1]) cube([3, 1+5.4, 3]);
            translate([length - 2.4, width - 2.4,  -1]) cube([3, 3, 3]);
            hole(13.7, 2.7);
            hole(length - 2.6, 7.8);
            hole(length - 2.6, 35.6);
            hole(15.5, width - 2.6);
        }
    }
}

module usb_power() {
    translate([-1, 16.5, height]) cube([7.5, 5.8, 3]);
}

module ethernet() {
    translate([-4.2, 31.5, height]) cube([21.2, 14.2, 15.7]);
}

module usb() {
    translate([-4.2, 6.15, height]) cube([21.2, 5.8, 15.7]);
}

module header(x = 0, y = 0, z = 0, length = 10) {
    translate([x, y, z]) cube([length, 2.6, 9.2]);
}

module shield() {
    translate([20.5, 24.6, height]) cube([40.9, 24.8, 2.5]);
}


module leds() {
    translate([length - 1.4 - 11.6, 10.65, height]) cube([2.2, 11.6, 1]);
}

module button(x = 0, y = 0) {
    translate([x, y, height]) cylinder(r = 3/2, h = 1);    
}

module side_button() {
    translate([5.2, 0, height + 3/4]) rotate([-90, 0, 0]) {
        cylinder(r = 3/2, h = 2);    
    }
}

module hole(x = 0, y = 0) {
    translate([x, y, -1]) cylinder(h = 3 + height, r = 3.5/2);
}

module micro_sd() {
    translate([length + 2 - 7.5, 10.2, -1.3]) cube([7.5, 12.6, 1.3]);
}

module arduino_yun() {
    color("green") board();
    color("magenta") usb_power();
    color("yellow") ethernet();
    color("brown") usb();
    color("silver") shield();
    color("red") leds();
    color("blue") button(length - 8.8, 7.9);
    color("blue") button(4.4, width - 2.5);
    color("violet") side_button();
    color("lime") micro_sd();
    color("black") header(x = 26.3, y = 1.3, z = height, length = 20.8);
    color("black") header(x = 49.3, y = 1.3, z = height, length = 20.8);
    color("black") header(x = 17.05, y = width - 1.3 - 2.6, z = height, length = 26.6);
    color("black") header(x = 44.2, y = width - 1.3 - 2.6, z = height, length = 26.6);
}
