// A RGB color LED
// Part of http://vor.space by Futurice

$fn = 256;

radius = 5/2;
height = 7;

ridge_radius = 6/2;
ridge_height = 1;

pin_length = 27;
pin_width = 1;

translate([0,0,-height]) union() {
    color("red") center();
    color("blue") ridge();
    color("white") lens();
    color("grey") pins();
}

module center() {
    cylinder(r=radius, h=height);
}

module ridge() {
    cylinder(r=ridge_radius, h=ridge_height);
}

module lens() {
    translate([0, 0, height]) sphere(r=radius);
}

module pins() {
    translate([-radius, -pin_width/2, -pin_length]) cube([2*radius, pin_width, pin_length]);
}

