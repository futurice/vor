// Arduino Yun Mini 3D model for planning

$fn = 16;

length = 71.12;
width = 22.9;
height = 1.3;
header_length = 64;
header_height = 9.5;
button_radius = 1.8;

arduino_yun_mini();

module board() {
    cube([length, width, height]);
}

module usb_power() {
    translate([-102, (width - 15) / 2, height - 5]) cube([105.5 + length, 15, 3 + 8]);
    translate([0, 0, height - 5]) cube([5 + length, width, 16.5]);
}

module header(y = 0, z = 0, extra_height=0) {
    translate([2, y, z]) cube([header_length, 3, header_height+extra_height]);
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
    color("orange") hull() {
         bottom_slot(y = 8);
         bottom_slot(y = width - 8);
    }
    color("black") hull() {
        hull() {
            header(z = height, y = 0, extra_height=2);
            header(z = height, y = width - 2.6, extra_height=2);
        }
    }
    color("silver") header(z = -9.2, y = 0);
    color("silver") header(z = -9.2, y = width - 2.6);
}
