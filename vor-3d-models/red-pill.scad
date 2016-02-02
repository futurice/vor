$fn=256;

use <vor-logo.scad>;

length=40;
radius=length/2;
depth=12;

difference() {
    hull() {
        sphere(r=radius);
        translate([length,0,0])
        sphere(r=radius);
    }
    translate([length/2,0,radius-depth]) scale([1,1,10]) embossed_logo();
}
