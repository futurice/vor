// WIFI Toggle Switch 3D Model, Arduino Yun Mini version, http://vor.space
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

// "mode" variable is passed in from command line invocation during a batch build
// "mode" variable can be set manually for testing
if (mode=="left") {
    left_half();    
} else if (mode="right") {
    right_half();    
} else {
    body();
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
            // A bit ugly poor-man's-minkowski to avoid crashing SCAD
	        toggle_moved(dx=yun_skin, dy=yun_skin, dz=yun_skin);
	        toggle_moved(dx=yun_skin, dy=yun_skin, dz=-yun_skin);
	        toggle_moved(dx=yun_skin, dy=-yun_skin, dz=yun_skin);
	        toggle_moved(dx=yun_skin, dy=-yun_skin, dz=-yun_skin);
	        toggle_moved(dx=-yun_skin, dy=yun_skin, dz=yun_skin);
	        toggle_moved(dx=-yun_skin, dy=yun_skin, dz=-yun_skin);
	        toggle_moved(dx=-yun_skin, dy=-yun_skin, dz=yun_skin);
	        toggle_moved(dx=-yun_skin, dy=-yun_skin, dz=-yun_skin);
            bolt_hole_low();
            bolt_hole_high();
            logo_moved(theta=0,y=-22); // Right
            logo_moved(theta=180,y=22); // Left
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

module toggle_moved(dx=0, dy=0, dz=0) {
    rotate([0, 0, 180]) {
        translate([-toggle_length/2 + dx, -toggle_width/2 + dy, toggle_z + dz])
            toggle();
    }
}

module logo_moved(theta=0, y=0) {
    s=.2;
    translate([0, y, 22]) rotate([90,0,theta]) {
        scale([s,s,s]) logo();
    }
}

module yun() {
    include <arduino-yun-mini-negative-space.scad>
}

module toggle() {
    include <toggle-switch-with-cover.scad>
}

module bolt() {
    include <m3.scad>
}

module logo() {
    import("vor-logo-embossed.stl", convexity=10);
}
