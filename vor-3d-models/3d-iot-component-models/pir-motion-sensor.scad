// A PIR motion sensor ordered from https://www.sparkfun.com/products/13285
// Part of http://vor.space by Futurice
// ©Futurice Oy, paul.houghton@futurice.com, CC-attribution-sharealike license, http://creativecommons.org/licenses/by-sa/4.0/

$fn = 32;

length = 33.6;
width = 28.6;
height = 4;

base_length = 22.9;
base_width = 22.7;
base_height = 5.6;

dome_radius = 22/2;

pir_motion_sensor();

module board() {
    cube([length, width, height]);
}

module dome() {
    translate([5.55, 2.84, height])
        cube([base_length, base_width, base_height]);
    difference() {
        translate([length/2, width/2, height + 5.6]) sphere(r=dome_radius);
        translate([0, 0, -10]) cube([length, width, 10]);
    }
}

module wire() {
    translate([-length/5, (width - 6.2)/2, 0])
        cube([length/2, 6.2, height + 3]);
}

module hole(x = 0, y = 0) {
    translate([x, y, -10 + height]) cylinder(h = 10 + height, r = 2.6/2);
}

module pir_motion_sensor() {
    translate([-length/2, -width/2, -base_height - height]) union() {
        color("green") difference() {
            board();
            union() {
                hole(x = 2.9, y = 2.5);
                hole(x = length - 2.9, y = width - 2.5);
                hole(x = 2.9, y = width - 2.5);
                hole(x = length - 2.9, y = 2.5);
            }
        }
        color("grey") dome();
        color("black") wire();
    }    
}
