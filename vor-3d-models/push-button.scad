// m3 bolt negative space
// Part of http://vor.space by Futurice
// ©Futurice Oy, paul.houghton@futurice.com, CC-attribution-sharealike license, http://creativecommons.org/licenses/by-sa/4.0/

$fn=128;

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
    color("green") translate([0,0,-prong_length]) hull() {
        prongs(length=30);
    }
}

module base() {
    translate([-base_side/2,-base_side/2,-base_height]) cube([base_side,base_side,base_height]);
}

module button() {
    cylinder(r=button_radius, h=button_height);
}

module prongs(length=prong_length) {
    prong(x=-base_side/2 - prong_width,l=length);
    prong(x=base_side/2,l=length);
}

module prong(x=0,l=prong_length) {
    translate([x,-base_side/2,-2*base_height/3 - l]) cube([prong_width,base_side,l]);
}
