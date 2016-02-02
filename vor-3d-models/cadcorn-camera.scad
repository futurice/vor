// Cadcorn Camera, a 3D-printable unicorn webcam holder
// ©2015 Paul Houghton, paul.houghton@futurice.com, CC-attribution-sharealike license, http://creativecommons.org/licenses/by-sa/4.0/

use <cadcorn.scad>;
use <creative-HD-1080p-webcam.scad>;

low_poly = 8;
high_poly = 128;

poly=high_poly;
scale=1.8;

cut_size=300;

front();
back();

module camera() {
    rotate([25,0,0]) {
    minkowski() {
        creative_hd();
        sphere($fn=8, r=1);
    }
    lens_space();
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
        }
    }
}

module back() {
    difference() {
        corn();
        union() {
            uncut();
            camera();
        }
    }
}

module cut() {
    translate([cut_size/2,0,0]) cube([cut_size,cut_size,cut_size], center=true);
}

module uncut() {
    difference() {
       translate([cut_size/2,0,0]) cube([2*cut_size,cut_size,cut_size], center=true);
        cut();
    }
}