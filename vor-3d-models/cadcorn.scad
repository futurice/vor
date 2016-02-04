// Cadcorn, a parametric model of a 3D-printable unicorn
// ©2015 Paul Houghton, paul.houghton@futurice.com, CC-attribution-sharealike license, http://creativecommons.org/licenses/by-sa/4.0/
low_poly = 8;
high_poly = 128;

scale = 1;

head_big_radius = 10*scale;
head_small_radius = 4.5*scale;
head_length = 18*scale;
head_angle = 50;

horn_length = 20*scale;
horn_radius = 2.8*scale;

mane_thickness = 2*horn_radius;
mane_radius = head_big_radius;
mane_height = head_big_radius * 0.7;
mane_x = head_big_radius * 0.28;
mane_length_x = 10*scale;
mane_length_z = 20*scale;

hair_thickness = mane_thickness / 8;
hair_spacing = mane_thickness / 2;

neck_radius = 3*scale;
neck_ratio = .2; // Flattens the interface to the top body
neck_top_x = 3*scale;

body_x = 15*scale;
body_z = 28*scale;
body_radius = 14*scale;
body_back_radius = 12*scale;
body_length = 33*scale;

leg_length = (33*scale + body_radius);
leg_radius = 4*scale;

cute_leg_first_ratio = 0.5;
cute_leg_second_ratio = 0.25;
cute_leg_third_ratio = 0.11;

tail_thickness = mane_thickness;
tail_radius = 10 * hair_thickness;
tail_x = 2*body_x + body_length - tail_radius;
tail_height = -body_z + 1.7*tail_radius;
tail_tip_length = 29*scale;

base_thickness = 10*scale;
base_width = 40*scale;
base_corner_radius = 5*scale;
base_length = 60*scale;

cadcorn(poly=low_poly);

module cadcorn(poly=high_poly) {
    union() { // difference() -> no base, or union() -> solid base
        union() {
            head(p=poly);
//        horn();  // Uncomment either horn() or chili_horn or neither
            color("red") chili_horn();
            neck(p=poly);
            mane(p=poly);
            body(p=poly);
            leg(p=poly, theta=5, sideways=3*scale);
            cute_leg(p=poly, theta=-5, sideways=-3*scale, downwards=3*scale);
            leg(p=poly, lengthwards = body_length, sideways=2*scale, theta=15);
            leg(p=poly, lengthwards = body_length, sideways=-2*scale, theta=-15);
            tail(p=poly);
        }
        base(p=poly);
    }
}

module head(p=high_poly) {
    rotate([0, -head_angle, 0]) {
        hull () {
            sphere($fn=p, r=head_big_radius);
            translate([-head_length, 0, 0])
                sphere($fn=p, r=head_small_radius);    
        }
    }
}

module horn(p=high_poly) {
    rotate([0, -head_angle, 0]) {
        translate([0, 0, head_big_radius - .2*scale])
        cylinder($fn=p, h=horn_length, r1=horn_radius, r2=0);
    }
}

module chili_horn(p=high_poly) {
    rotate([0, -head_angle, 0]) {
        z = 1.1*scale + head_big_radius - .2*scale;
        translate([0, 0, z])
            hull() {
                sphere($fn=p, r=horn_radius);
                translate([0, 0, horn_length/3])
                    sphere($fn=p, r=horn_radius*.8);
            }
       translate([0, 0, z + horn_length/3])
            hull() {
                sphere($fn=p, r=horn_radius*.8);
                rotate([0, -10, 0]) {
                    translate([0, 0, 3*horn_length/6])
                        sphere($fn=p, r=horn_radius*.6);
                }
            }
    }
}

module neck(p=high_poly) {
    hull() {
        translate([neck_top_x, 0, 0]) rotate([0, 180, 0]) {
            cylinder($fn=p, h=head_big_radius - .2*scale, r1=neck_radius, r2=neck_radius);
        }
        translate([body_x, 0, -body_z])
            difference() {
                sphere($fn=p, r=body_radius);
                translate([body_radius*neck_ratio, -body_radius, -body_radius])
                    cube([body_radius, 2*body_radius, 2*body_radius]);
            }
        }
}

module hair_line(p=high_poly, x=0, z=0, hair_radius=mane_radius, delta_x=body_x, delta_z=body_z) {
    translate([x, 0, z])
            difference() {
                hull() {
                    cylinder($fn=p, h=mane_thickness, r=hair_radius);
                    translate([delta_x, -delta_z, 0])
                        cylinder($fn=p, h=mane_thickness, r=hair_radius);
                }
                hull() {
                    cylinder($fn=p, h=mane_thickness, r=hair_radius - hair_spacing);
                    translate([delta_x, -delta_z, 0])
                        cylinder($fn=p, h=mane_thickness, r=hair_radius - hair_spacing);
                }                
            }
}

module mane(p=high_poly) {
    translate([mane_x, mane_thickness/2, mane_height])
        rotate([90, 0, 0]) {
                hull() {
                    translate([0, 0, hair_thickness])
                    cylinder($fn=p, h=mane_thickness - 2*hair_thickness, r=mane_radius);
                    translate([body_x, -body_z, 0])
                       cylinder($fn=p, h=mane_thickness - 2*hair_thickness, r=mane_radius);
                }
              hair_line(p=p);
              hair_line(p=p, hair_radius = mane_radius - 2*hair_spacing);
        }
}

module body(p=high_poly) {
    translate([body_x, 0, -body_z])
        hull() {
            sphere($fn=p, r=body_radius);
            translate([body_length, 0, 0]) 
                sphere($fn=p, r=body_back_radius);
        }
}

module leg(p=high_poly, lengthwards=0, sideways=0, theta=20) {
    translate([body_x + lengthwards, sideways, -body_z])
        rotate([180 + theta, 0, 0]) {
        cylinder($fn=p, h=leg_length, r1=leg_radius, r2=leg_radius);
        }
}

module cute_leg(p=high_poly, lengthwards=0, sideways=0, downwards=0, theta=20, rise_angle=70) {
    translate([body_x + lengthwards, sideways, -body_z - downwards])
        rotate([180 + theta, rise_angle, 0]) {
            cylinder($fn=p, h=leg_length*cute_leg_first_ratio, r1=leg_radius, r2=leg_radius);
            translate([0, 0, leg_length*cute_leg_first_ratio])
                union() {
                    sphere($fn = 2*p, r=leg_radius);
                    rotate([0, 90, 0]) {
                        cylinder($fn=p, h=leg_length*cute_leg_second_ratio, r1=leg_radius, r2=leg_radius);
                    }
                    translate([leg_length*cute_leg_second_ratio, 0, 0])
                        union() {
                            sphere($fn=2*p, r=leg_radius);
                            rotate([0, 90 + rise_angle, 0]) {
                                cylinder($fn=p, h=leg_length*cute_leg_third_ratio, r1=leg_radius, r2=leg_radius);
                            }
                        }
                }
    }
}

module tail_outer_hair(p=high_poly) {
    translate([0, 0, -hair_thickness])
        difference() {
            hull() {
                cylinder($fn=p, h=tail_thickness, r=tail_radius);
                translate([body_x, -1.2*body_z, 0])
                    rotate([0, 0, 11]) {
                        cube([tail_tip_length, tail_tip_length, tail_thickness]);
                    }
            }
            hull() {
                cylinder($fn=p, h=tail_thickness, r=tail_radius - hair_spacing);
                translate([body_x + 2*hair_spacing, -1.2*body_z + hair_spacing, 0]) rotate([0, 0, 11]) {
                    cube([tail_tip_length - 2.8*hair_spacing, tail_tip_length - 2.8*hair_spacing, tail_thickness]);
                }
            }
        }
}

module tail_single_hair(p=high_poly, n=1) {
    translate([0, -4.5*n, -hair_thickness])
        rotate([0, 0, -15*n]) {
            difference() {
                hull() {
                    cylinder($fn=p, h=tail_thickness, r=tail_radius);
                    translate([body_x, -1.2*body_z, 0])
                        rotate([0, 0, 11]) {
                            cube([tail_tip_length, tail_tip_length, tail_thickness]);
                        }
                }
                union() {
                    hull() {
                        cylinder($fn=p, h=tail_thickness, r=tail_radius - hair_spacing);
                        translate([body_x + 2*hair_spacing, -1.2*body_z + hair_spacing, 0]) rotate([0, 0, 11]) {
                            cube([tail_tip_length - 2.8*hair_spacing, tail_tip_length - 2.8*hair_spacing, tail_thickness]);
                        }
                    }
                    rotate([50, 90, 0]) {
                        translate([-3*scale, -7*scale, 0]) cylinder($fn=p, r=6*scale, h=40*scale);
                    }
                }
            }
        }
}

module tail(p=high_poly) {
    translate([tail_x, tail_thickness/2 - hair_thickness, tail_height])
        rotate([90, 0, 0]) {
            intersection() {
                union() {
                    hull() {
                        cylinder($fn=p, h=tail_thickness - 2*hair_thickness, r=tail_radius);
                        translate([body_x, -1.2*body_z, 0])
                            rotate([0, 0, 11]) {
                               cube([tail_tip_length, tail_tip_length, tail_thickness - 2*hair_thickness]);
                            }
                    }
                    tail_outer_hair(p=p);
                    tail_single_hair(p=p, n=1);
                    tail_single_hair(p=p, n=2);
                }
                translate([0, 0, -hair_thickness])
                    cylinder($fn = 2*p, h=10*scale+tail_thickness, r=tail_tip_length*1.25);
            }
        }
}

module base(p=high_poly) {
    translate([body_x + .5*body_length, 0, -leg_length - body_z - 2])
        minkowski() {
            cylinder($fn=p, h = base_thickness / 3, r = base_corner_radius);
            cube([base_length, base_width - 2*base_corner_radius, base_thickness/3], center = true);
        }
}
