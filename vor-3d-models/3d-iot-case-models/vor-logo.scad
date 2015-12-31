// Vör Logo 3D Model
// Part of http://vor.space
// ©Futurice Oy, paul.houghton@futurice.com, CC-attribution-noderivatives license, http://creativecommons.org/licenses/by-nd/4.0/

$fn = 64;

logo_height = 15;
base_height = 15;
center_x = 75;
center_y = 30;
surround_radius = 90;
litho_cone_height = 1.6*surround_radius;
led_length = 7;

// Uncomment one of the following at a time
//plate_logo();
//negative_logo();
//lithophane_negative_logo(thickness=.5);
lithophane_negative_rgb_cone(thickness=.5);

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

module lithophane_negative_logo(thickness=.5) {
    difference() {
        base();
        logo_text(z=-thickness);
    }
}

module lithophane_negative_rgb_cone(thickness=.5) {
    cut_width = 100;
    cut_height = 100;
    cut_start_height = 20;
    
    lithophane_negative_logo();
    translate([0,0,-litho_cone_height]) difference() {
        translate([0,0,-led_length]) cylinder(r=surround_radius, h=litho_cone_height+led_length);
        union() {
            cylinder(r1=0, r2=surround_radius, h=litho_cone_height);
            rgb_led();
            translate([-cut_width/2, -200, cut_start_height]) cube([cut_width, 400, cut_height]); 
        }
    }
}

module logo_text(z=0) {
    intersection() {
        translate([-center_x, -center_y, z]) linear_extrude(height = logo_height, center = false, convexity = 10) import (file = "logo_vor.dxf");
        translate([0,0,-500]) cylinder(r=surround_radius, h = 1000);
    }
}

module base() {
    cylinder(r=surround_radius, h=base_height);
}

module rgb_led() {
    include <../3d-iot-component-models/rgb-led.scad>
}
