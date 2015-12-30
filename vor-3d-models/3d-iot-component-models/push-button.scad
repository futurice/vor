// m3 bolt negative space

$fn=64;

base_side = 12;
base_height = 3.2;

button_radius = 6.8/2;
button_height = 4.9;

prong_width=2;
prong_length=base_height/2 + 5;

push_button();

module push_button() {
    color("blue") base();
    color("black") button();
    color("silver") prongs();
}

module base() {
    translate([-base_side/2,-base_side/2,-base_height]) cube([base_side,base_side,base_height]);
}

module button() {
    cylinder(r=button_radius, h=button_height);
}

module prongs() {
    prong(-base_side/2 - prong_width);
    prong(base_side/2);
}

module prong(x=0) {
    translate([x,-base_side/2,-2*base_height/3 - prong_length]) cube([prong_width,base_side,prong_length]);
}
