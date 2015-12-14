// A toggle switch ordered from https://www.sparkfun.com/products/11310

$fn = 16;

length = 85;
width = 56;
height = 1.3;

module board() {
    cube([length, width, height]);
}

module ethernet() {
    translate([-2, 38.3, height]) cube([21.2, 14.2, 15.7]);
}

module usb(y = 0) {
    translate([-2, y, height]) cube([17, 13.3, 15.7]);
}

module usb_power() {
    translate([70, width + 2 - 5.8, height]) cube([7.5, 5.8, 3]);
}

module micro_sd() {
    translate([length + 2 - 7.5, 21, -1.3]) cube([7.5, 12.6, 1.3]);
}

module dvi() {
    translate([45, width + 2 - 12, height]) cube([15, 12, 6.6]);
}

module audio() {
    translate([27.5, width + 2 - 15, height]) cube([6, 15, 6]);
}

module header() {
    translate([27.3, 1.2, height]) cube([50.45, 5, 9.2]);
}

board();
color("green") ethernet();
color("red") usb(y=2.35);
color("blue") usb(y=19.6);
color("orange") dvi();
audio();
color("magenta") usb_power();
color("lime") micro_sd();
color("teal") usb_power();
color("black") header();
