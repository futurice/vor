// A PIR motion sensor ordered from https://www.sparkfun.com/products/13285

$fn = 16;

length = 33.6;
width = 28.6;
height = 4;

module board() {
    cube([length, width, height]);
}

module dome() {
    translate([5.55, 2.84, height]) cube([22.9, 22.7, 5.6]);
    difference() {
        translate([length/2, width/2, height + 5.6]) sphere(r = 22 / 2);
        translate([0, 0, -10]) cube([length, width, 10]);
    }
}

module wire() {
    translate([-length/4, (width - 6.2)/2, height]) cube([length/2, 6.2, 1]);
}


color("green") board();
color("grey") dome();
color("black") wire();
