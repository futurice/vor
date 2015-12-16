// A toggle switch ordered from https://www.sparkfun.com/products/11310

$fn = 64;

length = 71.2;
width = 14.4;
height = 24.75;

module camera() {
    intersection() {
        cube([length, width, height]);        
        translate([length/2, 0, height/2]) rotate([-90, 0, 0]) {
            cylinder(h = width, r = length/2);
        }
    }
}

module lens() {
    translate([length/2, 0, height/2]) rotate([90, 0, 0]) {
        cylinder(h = 10, r = 27/2);
    }
}

module cable() {
    translate([length/2, width, 4]) rotate([-90, 0, 0]) {
        cylinder(h = 10, r = 3.3/2);
    }
}

color("dimgrey") camera();
color("lightgrey") lens();
color("black") cable();