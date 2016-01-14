// Vör Logo 3D Model
// Part of http://vor.space
// ©Futurice Oy, paul.houghton@futurice.com, CC-attribution-noderivatives license, http://creativecommons.org/licenses/by-nd/4.0/

$fn = 64;

scale=20; // Percent reduction of the entire lithophane diameter, 100=full size

logo_height = 15*scale/100;
base_height = 15*scale/100;
center_x = 75*scale/100;
center_y = 30*scale/100;
surround_radius = 90*scale/100;
litho_cone_height = 8*surround_radius*scale/100;
led_length = 7;

// Uncomment one of the following at a time
//plate_logo();
//negative_logo();
//lithophane_negative_logo(thickness=.5);
lithophane_negative_rgb_cone(thickness=.1);

module plate_logo() {
    logo_text(z=base_height);
    base();
}

module negative_logo() {
    difference() {
        base();
        logo_text();
    }
}

module lithophane_negative_logo(thickness=.5*100/scale) {
    difference() {
        base();
#        logo_text(z=-.4);
    }
}

module lithophane_negative_rgb_cone(thickness=.5) {
    cut_width = 100*scale/100;
    cut_height = 100*scale/100;
    cut_start_height = 5*scale/100;
    
    lithophane_negative_logo();
    translate([0,0,-litho_cone_height]) difference() {
        translate([0,0,-led_length]) minkowski() {
            cylinder(r=surround_radius, h=litho_cone_height+led_length);
            sphere(r=base_height/2);
        }
        union() {
            cylinder(r1=5/2, r2=surround_radius, h=litho_cone_height);
            rgb_led();
            translate([-cut_width/2, -200, cut_start_height]) cube([cut_width, 400, cut_height]); 
        }
    }
}

module logo_text(z=0) {
    intersection() {
        translate([-center_x, -center_y, z]) resize([135*scale/100, 69*scale/100,logo_height])
linear_extrude(height = logo_height, center = false, convexity = 10) import (file = "logo_vor.dxf");
        translate([0,0,-500]) cylinder(r=surround_radius, h = 1000);
    }
}

module base() {
    cylinder(r=surround_radius, h=base_height);
}

module rgb_led() {
    include <../3d-iot-component-models/rgb-led.scad>
    translate([0,0,-100]) cylinder(r1=100,r2=6/2,h=100-7);
}
