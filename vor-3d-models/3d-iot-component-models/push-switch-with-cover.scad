// A push switch cover from https://www.sparkfun.com/products/9278

$fn = 16;

length = 15;
width = 16.55;
height = 8;

plate_height = 0;
shaft_offset = 7;

toggle_switch_with_cover();

module base() {
    translate([shaft_offset, 0, 0])
        cube([length, width, height]);
}

module shaft() {
    translate([length/2 + shaft_offset, width/2, height]) cylinder(h = 4, r = 6.75 / 2);
}

module plate() {
    translate([0, 0, height + plate_height]) cube([40.6, 17, .9]);
}

module button() {
    translate([length/2 + shaft_offset, width/2, height])
        cylinder(h = 4.2, r = 6/2);    
}

module cover() {
    color("red") translate([40.6 - 17.56, 0, height])
        cube([17.56, width, 43.9 - 17.56]);
}

module toggle_switch_with_cover() {
    translate([0, 0, -height - .82 - plate_height]) 
        union() {
            base();
            color("black") shaft();
            color("silver") plate();
            color("red") button();
            color("red") cover();
        }
}
