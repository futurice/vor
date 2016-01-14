// WIFI Push Switch 3D Model, Arduino Yun Mini version, http://vor.space
// ©Futurice Oy, paul.houghton@futurice.com, CC-attribution-sharealike license, http://creativecommons.org/licenses/by-sa/4.0/

$fn = 8;

cube_side = 72;
cube_tip_clip = 59;
side_clip = cube_tip_clip*.41;

yun_length = 71.12;
yun_width = 22.86;
yun_height = 1.3 + 2*64;
yun_x = -yun_length/2;
yun_y = -yun_width/2;
yun_z = 13;
yun_skin = .3;

toggle_length = 28;
toggle_width = 16.55;
toggle_z = 55;

tip_surround_width = 3;
tip_width = toggle_width + 2*tip_surround_width;
tip_length = 46;

button_height = 4 + 4.8;
flexure_length = 24.9;
flexure_width = 6;
rounding = 4;

push_button();

module push_button() {
    // Uncomment one of the following for left side
    color("brown") mushroom();
//    color("grey") 8_ball();

    // Uncomment one of the following
//    right_half();
    left_half();
}

module left_half() {
    difference() {
        body();
        union() {
            translate([-2*cube_side, -4*cube_side, -1])
                cube([cube_side*4, cube_side*4, cube_side*2]);        
            yun_hole();
        }
    }
    color("pink") spikes();
}

module right_half() {
    difference() {
        body();
        union() {
            translate([-2*cube_side, 0, -1])
                cube([cube_side*4, cube_side*4, cube_side*2]);
            spikes(skin_thickness=.2);
            logo_moved(theta=0,y=-22);
        }
    }
}

module body() {
    difference() {
        union() {
            color("purple") difference() {
                box();
                yun_hole();
            }
            color("orange") tip();
        }
        union() {
	        button_moved();
            bolt_hole_low();
            bolt_hole_high();            
             logo_moved(theta=0,y=-22); // Right
           logo_moved(theta=180,y=22); // Left
	    }
    }
}

module mushroom() {
    flexure();
//    translate([-flexure_length/2, 12, toggle_z -5])
//         cube([flexure_length, flexure_width, button_height+5]);

    translate([0, 0, toggle_z + button_height + rounding])
         minkowski() {
             cylinder($fn=128, r=25 - rounding, h = 10 - 2*rounding);
             sphere($fn=128, r=rounding);
         }
}

module 8_ball() {
    flexure();

    translate([0, 0, toggle_z + button_height])
        difference() {
            sphere($fn=128, r=25);
            union() {
                translate([0, 0, -25]) cube(size=50,center=true);
                text_8();
            }
        }
}

module flexure() {
    fat_width = 12;

    difference() {    
        intersection() {
            translate([-flexure_length*sin(45), 0, toggle_z + button_height]) rotate([0,45,0]) cube([flexure_length, flexure_width + fat_width, flexure_length]);
            translate([0,0,45]) cube(size=45,center=true);
        }
#        translate([0,0,0]) union() {
            flexure_cut_right();
            flexure_cut_left();
        }
    }
}

flexure_cut_depth=12;

module flexture_cut_right() {
    cube([40,40,3]);
    // Continue here
}

module flexture_cut_left() {
    
}

module spikes(skin_thickness=0) {
    spike(x=-50, skin=skin_thickness);
    spike(x=50, skin=skin_thickness);
    spike(x=-30, skin=skin_thickness, z = 30);
    spike(x=-5/2, skin=skin_thickness, z = 45);
}

module spike(x=0, skin=0, z = 0) {
    translate([x - skin, -5 - skin, 2 + z])
        cube([5 + 2*skin, 12 + 2*skin, 5 + 2*skin]);
}

module bolt_hole_low() {
    translate([47, -10, 8]) rotate([90, 0, 0]) {
        bolt();
    }
    translate([47, 10, 8]) rotate([-90, 0, 0]) {
        bolt();
    }
}

module bolt_hole_high() {
    translate([-24, -9, 47]) rotate([90, 0, 0]) {
        bolt();
    }
    translate([-24, 9, 47]) rotate([-90, 0, 0]) {
        bolt();
    }
}

module yun_moved() {
    translate([yun_x, yun_y, yun_z])
        yun();
}

module yun_hole() {
    translate([yun_x - yun_skin, yun_y - yun_skin, yun_z - 9.6 - yun_skin])
        cube([yun_length + 2*yun_skin, yun_width + 2*yun_skin, 9.2*2 + 1.6 + 2*yun_skin]);
    minkowski() {
        yun_moved();
        sphere(r=yun_skin);
    }
}

module yun_holder() {
    translate([yun_x, yun_y, yun_z])
        yun();    
}

module tip() {
    translate([-30, -tip_width/2, yun_z + 8])
        difference() {
            union() {
                cube([tip_length, tip_width, cube_tip_clip + .1 - 26]);
                translate([10, 0, 0])
                    cube([tip_length, tip_width, 10]);
            }
            translate([0, 0, toggle_z])
                cube([tip_length/2, tip_width, 20]);
        }
}

module poly() {
    difference() {
        polyhedron(
            points=[[cube_side,0,0],
                [0,-cube_side,0],
                [-cube_side,0,0],
                [0,cube_side,0],
                [0,0,cube_side*1.1]],
            faces=[[0,1,4],[1,2,4],[2,3,4],[3,0,4],[1,0,3],[2,1,3]]);
        union() {
            translate([-2*cube_side, -2*cube_side, cube_tip_clip])
                cube([cube_side*4, cube_side*4, cube_side]);
            translate([-2*cube_side, -side_clip - 4*cube_side, -1])
                cube([cube_side*4, cube_side*4, cube_side]);
            translate([-2*cube_side, side_clip, -1])
                cube([cube_side*4, cube_side*4, cube_side]);            
        }
    }
}

module box() {
    difference() {
        poly();
        yun_holder();
    }
}

module button_moved() {
    translate([0, -6, toggle_z + 4.5])
         button();
}

module text_8() {
    translate([20,15,18]) rotate([15,15,50+90]) {
        translate([0,9,0]) scale([2.2,2.2,1]) linear_extrude(height=6) {
            text("8");
        }
    }
}

module logo_moved(theta=0, y=0) {
    s=.2;
    translate([0, y, 22]) rotate([90,0,theta]) {
        scale([s,s,s]) logo();
    }
}

module logo() {
    import("vor-logo-negative.stl", convexity=10);
}

module yun() {
    include <../3d-iot-component-models/arduino-yun-mini-negative-space.scad>
}

module button() {
    include <../3d-iot-component-models/push-button.scad>
}

module bolt() {
    include <../3d-iot-component-models/m3.scad>
}

