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

push_button();

module push_button() {
    // Uncomment one of the following 3 lines at a time
//    body();
//    left_half();
    right_half();
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
        }
    }
}

module spikes(skin_thickness=0) {
    spike(x=-50, skin=skin_thickness);
    spike(x=50, skin=skin_thickness);
    spike(x=-30, skin=skin_thickness, z = 30);
}

module spike(x=0, skin=0, z = 0) {
    translate([x - skin, -5 - skin, 2 + z])
        cube([5 + 2*skin, 12 + 2*skin, 5 + 2*skin]);
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
	    }
    }
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

module yun() {
    include <../3d-iot-component-models/arduino-yun-mini.scad>
}

module button() {
    include <../3d-iot-component-models/push-button.scad>
}

module bolt() {
    include <../3d-iot-component-models/m3.scad>
}

