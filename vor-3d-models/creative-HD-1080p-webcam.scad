// A Webcam negative space model
// Part of Vör, http://vor.space
// ©Futurice Oy, paul.houghton@futurice.com, CC-attribution-sharealike license, http://creativecommons.org/licenses/by-sa/4.0/

$fn = 128;

length = 71.2;
width = 14.4;
height = 24.75;
lens_width = 10;

creative_hd();
#lens_space();

module creative_hd() {
    translate([-length/2,-width+(width+lens_width)/2,-height/2]) {
        color("dimgrey") camera();
        color("lightgrey") lens();
        color("black") cable();
    }
}

module lens_space() {
    translate([0,8,0]) {
        rotate([90,0,0]) cylinder(r1=0,r2=10*4,h=13*4);
    }
}

module camera() {
    intersection() {
        union() {
            cube([length, width, height]);
            cube([length, width+8,8]);
        }
        translate([length/2, 0, height/2]) rotate([-90, 0, 0]) {
            cylinder(h = width+8, r = length/2);
        }
    }
}

module lens() {
    translate([length/2, 0, height/2]) rotate([90, 0, 0]) {
        cylinder(h = lens_width, r = 27/2);
    }
}

module cable() {
    translate([length/2, width, 17.5]) rotate([-90, 0, 0]) {
        cylinder(h = 100, r = 3.3/2);
    }
}
    