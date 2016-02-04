// Cadcorn Camera, a 3D-printable unicorn webcam holder
// ©2015 Paul Houghton, paul.houghton@futurice.com, CC-attribution-sharealike license, http://creativecommons.org/licenses/by-sa/4.0/

use <cadcorn.scad>;
use <creative-HD-1080p-webcam.scad>;
use <m3.scad>;

low_poly = 8;
high_poly = 128;

poly=high_poly;
scale=2;

cut_size=300;

front();
back();

module camera() {
    translate([0,-2,-6]) {
        rotate([20,0,0]) {
        minkowski() {
            creative_hd();
            sphere($fn=8, r=1);
        }
        lens_space();
    }
}
}

module corn() {
    translate([-50,0,50]) scale([scale,scale,scale])
        cadcorn(poly=poly);    
}

module front() {
    difference() {
        corn();
        union() {
            cut();
            camera();
            bottom_bolt();
            top_bolt();
        }
    }
}

module back() {
    difference() {
        corn();
        union() {
            uncut();
            camera();
            bottom_bolt();
            top_bolt();
        }
    }
}

module cut() {
    translate([cut_size/2,0,0]) cube([cut_size,cut_size,cut_size], center=true);
    spike=5;
    translate([-spike,7,-24]) cube(size=spike);
    translate([-spike,-9,11]) cube(size=spike);
    translate([-spike,25,-103]) cube(size=spike);
    translate([-spike,-30,-103]) cube(size=spike);
}

module uncut() {
    difference() {
        translate([cut_size/2,0,0]) cube([2*cut_size,cut_size,cut_size], center=true);
        minkowski() {
            cut();
            cube(size=.3,center=true);
        }
    }
}

module bottom_bolt() {
    height=-100.5;
    translate([0,0,height]) rotate([0,90,45]) translate([0,0,14]) m3_bolt_space(length=14);
    translate([0,0,height]) rotate([0,90,45+180]) translate([0,0,14]) m3_bolt_space(length=14);
}

module top_bolt() {
    height=13;
    translate([0,0,height]) rotate([0,90,-45]) translate([0,0,14]) m3_bolt_space(length=14);
    translate([0,0,height]) rotate([0,90,-45+180]) translate([0,0,14]) m3_bolt_space(length=14);
}