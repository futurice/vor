// Chilicorn
$fn=45;

head_big_radius = 10;
head_small_radius = 5;
head_spacing = 18;
head_angle = 30;

horn_length = 18;
horn_radius = 3;

neck_angle = 30;
neck_radius = 4;
neck_length = 5;

body_x = 15;
body_z = 27;
body_radius = 15;
body_length = 32;

leg_length = 26 + body_radius;
leg_radius = 4;

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
        rotate([0, -neck_angle + 180, 0]) {
            cylinder(h = neck_length + head_big_radius - .2, r1 = neck_radius, r2 = neck_radius);
        }
        translate([body_x, 0, -body_z])
            sphere(r = body_radius);
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
                sphere(r = body_radius);
        }
}

module leg(lengthwards=0, sideways=0, theta=20) {
    translate([body_x + lengthwards, sideways, -body_z])
        rotate([180 + theta, 0, 0]) {
        cylinder(h = leg_length, r1 = leg_radius, r2 = leg_radius);
        }
}

module cute_leg(lengthwards=0, sideways=0, theta=20, rise_angle=70) {
    translate([body_x + lengthwards, sideways, -body_z])
        rotate([180 + theta, rise_angle, 0]) {
        cylinder(h = leg_length, r1 = leg_radius, r2 = leg_radius);
        }
}

head();
horn();
neck();
body();
leg(theta=10);
color("red") cute_leg(theta=-10);
leg(lengthwards = body_length, theta=20);
leg(lengthwards = body_length, theta=-20);
