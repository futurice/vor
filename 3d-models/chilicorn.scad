// Unicorn by Paul Houghton ©2015, CC BY-NC-AS license, https://creativecommons.org/licenses/
$fn=45;

scale = 1;

head_big_radius = 10*scale;
head_small_radius = 4.5*scale;
head_length = 18*scale;
head_angle = 50*scale;

horn_length = 20*scale;
horn_radius = 2.8*scale;

neck_radius = 2*scale;
neck_ratio = .2; // Flattens the interface to the top body
neck_top_x = 3*scale;

body_x = 15*scale;
body_z = 28*scale;
body_radius = 14*scale;
body_back_radius = 12*scale;
body_length = 33*scale;

leg_length = (28 + body_radius)*scale;
leg_radius = 4*scale;

cute_leg_first_ratio = 0.65;
cute_leg_second_ratio = 0.25;
cute_leg_third_ratio = 0.11;

base_thickness = 10*scale;
base_width = 40*scale;
base_corner_radius = 5*scale;
base_length = 70*scale;

module head() {
    rotate([0, -head_angle, 0]) {
        hull () {
            sphere(r=head_big_radius);
            translate([-head_length, 0, 0])
                sphere(r=head_small_radius);    
        }
    }
}

module horn() {
    rotate([0, -head_angle, 0]) {
        translate([0, 0, head_big_radius - .2*scale])
        cylinder(h = horn_length, r1 = horn_radius, r2 = 0);
    }
}

module neck() {
    hull() {
        translate([neck_top_x, 0, 0]) rotate([0, 180, 0]) {
            cylinder(h = head_big_radius - .2*scale, r1 = neck_radius, r2 = neck_radius);
        }
        translate([body_x, 0, -body_z])
            difference() {
                sphere(r = body_radius);
                translate([body_radius*neck_ratio, -body_radius, -body_radius])
                    cube([body_radius, 2*body_radius, 2*body_radius]);
            }
        }
}

module mane() {
}

module body() {
    translate([body_x, 0, -body_z])
        hull() {
            sphere(r = body_radius);
            translate([body_length, 0, 0]) 
                sphere(r = body_back_radius);
        }
}

module leg(lengthwards=0, sideways=0, theta=20) {
    translate([body_x + lengthwards, sideways, -body_z])
        rotate([180 + theta, 0, 0]) {
        cylinder(h = leg_length, r1 = leg_radius, r2 = leg_radius);
        }
}

module cute_leg(lengthwards=0, sideways=0, downwards=0, theta=20, rise_angle=70) {
    translate([body_x + lengthwards, sideways, -body_z - downwards])
        rotate([180 + theta, rise_angle, 0]) {
            cylinder(h = leg_length*cute_leg_first_ratio, r1 = leg_radius, r2 = leg_radius);
            translate([0, 0, leg_length*cute_leg_first_ratio])
                union() {
                    sphere(r = leg_radius);
                    rotate([0, 90, 0]) {
                        cylinder(h = leg_length*cute_leg_second_ratio, r1 = leg_radius, r2 = leg_radius);
                    }
                        translate([leg_length*cute_leg_second_ratio, 0, 0])
                            union() {
                                sphere(r = leg_radius);
                                rotate([0, 90 + rise_angle, 0]) {
                                cylinder(h = leg_length*cute_leg_third_ratio, r1 = leg_radius, r2 = leg_radius);
                                }
                            }
                }
    }
}

module base() {
    translate([body_x + 0.2*body_length, 0, -leg_length - body_z])
        minkowski() {
            cube([base_length, base_width - 2*base_corner_radius, base_thickness/3], center = true);
            cylinder(h = base_thickness / 3, r = base_corner_radius);
        }
}

difference() { // difference() -> no base, or union() -> solid base
    union() {
        head();
        horn();
        neck();
        body();
        leg(theta=5, sideways=3);
        cute_leg(theta=-5, sideways=-3, downwards=3);
        leg(lengthwards = body_length, sideways=2, theta=15);
        leg(lengthwards = body_length, sideways=-2, theta=-15);
    }
 #   base();
}