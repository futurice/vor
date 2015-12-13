// Chilicorn
$fn=45;

head_big_radius = 10;
head_small_radius = 5;
head_spacing = 18;
head_angle = 45;

horn_length = 18;
horn_radius = 3;

neck_radius = 2;
neck_ratio = .2; // Flattens the interface to the top body
neck_top_x = 3;

body_x = 15;
body_z = 28;
body_radius = 15;
body_back_radius = 13;
body_length = 33;

leg_length = 28 + body_radius;
leg_radius = 4;

cute_leg_first_ratio = 0.65;
cute_leg_second_ratio = 0.25;
cute_leg_third_ratio = 0.11;

module head() {
    rotate([0, -head_angle, 0]) {
        hull () {
            sphere(r=head_big_radius);
            translate([-head_spacing, 0, 0])
                sphere(r=head_small_radius);    
        }
    }
}

module horn() {
    rotate([0, -head_angle, 0]) {
        translate([0, 0, head_big_radius - .2])
        cylinder(h = horn_length, r1 = horn_radius, r2 = 0);
    }
}

module neck() {
    hull() {
        translate([neck_top_x, 0, 0]) rotate([0, 180, 0]) {
            cylinder(h = head_big_radius - .2, r1 = neck_radius, r2 = neck_radius);
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
    rotate([0, -neck_angle + 180, 0]) {
        cylinder(h = neck_length + head_big_radius - .2, r1 = neck_radius, r2 = neck_radius);
    }
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

head();
horn();
neck();
body();
leg(theta=5, sideways=3);
cute_leg(theta=-5, sideways=-3, downwards=3);
leg(lengthwards = body_length, sideways=2, theta=15);
leg(lengthwards = body_length, sideways=-2, theta=-15);
