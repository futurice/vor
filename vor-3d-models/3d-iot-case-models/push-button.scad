// WIFI Switch 3D Model, Arduino Yun version

$fn = 8;

cube_side = 72;
cube_tip_clip = 59;
side_clip = cube_tip_clip*.665;

skin = 10;
inner_skin = 8;
thin_skin = 0.1;

yun_length = 71.12;
yun_width = 22.86;
yun_height = 1.3 + 2*64;
yun_x = -yun_length/2;
yun_y = -yun_width/2;
yun_z = 13;
yun_skin = .2;

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
    left_half();
//    right_half();
    
//    yun_moved();
//    toggle_moved();
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
    color("pink") spike(x=-45);
    color("pink") spike(x=40);
}

module spike(x=0, skin=0) {
    translate([x - skin, -5 - skin, 3 - skin])
        cube([5 + 2*skin, 10 + 2*skin, 5 + 2*skin]);
}

module right_half() {
    difference() {
        body();
        union() {
            translate([-2*cube_side, 0, -1])
                cube([cube_side*4, cube_side*4, cube_side*2]);
            spike(x=-45, skin=.1);
            spike(x=40, skin=.1);
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
	        toggle_moved(dx=yun_skin, dy=yun_skin, dz=yun_skin);
	        toggle_moved(dx=yun_skin, dy=yun_skin, dz=-yun_skin);
	        toggle_moved(dx=yun_skin, dy=-yun_skin, dz=yun_skin);
	        toggle_moved(dx=yun_skin, dy=-yun_skin, dz=-yun_skin);
	        toggle_moved(dx=-yun_skin, dy=yun_skin, dz=yun_skin);
	        toggle_moved(dx=-yun_skin, dy=yun_skin, dz=-yun_skin);
	        toggle_moved(dx=-yun_skin, dy=-yun_skin, dz=yun_skin);
	        toggle_moved(dx=-yun_skin, dy=-yun_skin, dz=-yun_skin);
	    }
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
        cube(side=yun_skin, center=true);
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
                cube([tip_length, tip_width, cube_tip_clip + .1 - 20]);
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
                [0,0,cube_side]],
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

module yun() {
    include <../3d-iot-component-models/arduino-yun-mini.scad>
}

module toggle() {
    include <../3d-iot-component-models/toggle-switch-with-cover.scad>
}

