// Bathroom Sensor 3D Model with methane sensor, 2 motion sensors and Arduino Yun, http://vor.space
// ©Futurice Oy, paul.houghton@futurice.com, CC-attribution-sharealike license, http://creativecommons.org/licenses/by-sa/4.0/

$fn = 8;

cube_side = 140;
cube_tip_clip = 10;
wall_width = 29;  // Bathroom divider wall tunnel size
side_clip_ratio = 2;
left_wing_clip_height = 35;
right_wing_clip_height = 35;

skin = 10;
inner_skin = 8;
thin_skin = 0.1;
motion_skin = 0.2;
yun_skin = 0.4;

sensor_x=0;
sensor_y=40;

yun_x = 0;
yun_y = -22.9/2;
yun_z = -3;

//bathroom_sensor_shell();
bathroom_sensor_shell_left();
//bathroom_sensor_shell_right();
//bathroom_sensors_shown();

module bathroom_sensor_shell_left() {
    difference() {
        bathroom_sensor_shell();
        translate([-50,0,-cube_side]) cube([cube_side*2, cube_side*2, cube_side*2]);
    }
}

module bathroom_sensor_shell() {
    difference() {
        union() {
            color("red") box();
            color("blue") yun_holder();
            color("pink") translate([118,0,0]) rotate([0,90,0]) {
                cylinder(r=14,h=15);
    }
        }
        union() {
            methane_space_moved_skin();
            motion_moved_skin(y=-sensor_y, theta=0, rot=120);
            motion_moved_skin(y=sensor_y, theta=90, rot=120+180);
            yun_moved_skin();
        }
    }
    color("green") yun_airgap_feet();
}

module bathroom_sensors_shown() {
    methane_moved();
    motion_moved(y=-sensor_y, theta=0, rot=120);
    motion_moved(y=sensor_y, theta=90, rot=120+180);
}

module motion_moved(y=0, theta=0, rot=0) {
    translate([91, y, -20]) 
        rotate([36, 137, theta]) {
            motion_angle(rot);
        }
}

module motion_moved_skin(y=0, theta=0, rot=0) {
    minkowski() {
        motion_moved(y, theta, rot);
        sphere(r=motion_skin);
    }
}

module motion_angle(angle = 0) {
    rotate([0, 0, angle]) {
        motion();
    }
}

module yun_moved_skin() {
    minkowski() {
        yun_moved();
        sphere($fn=8, r=yun_skin);
    }
}

module yun_moved() {
    translate([yun_x, yun_y, yun_z])
        yun();
}

module yun_holder() {
    translate([0, -wall_width/2, -13])
        cube([75, wall_width, 5]);
}

module yun_airgap_feet() {
    translate([-10, -wall_width/2, -17])
        cube([130, 6, 3]);
    translate([-10, wall_width/2 - 6, -17])
        cube([130, 6, 3]);
}

module methane_moved_skin() {
    minkowski() {
        methane_moved();
        sphere(r=thin_skin);
    }
}

module methane_space_moved_skin() {
    minkowski() {
        methane_space_moved();
        sphere(r=thin_skin);
    }
}

module methane_space_moved() {
    translate([cube_side + skin - cube_tip_clip*2 - 5, 0, 0])
        rotate([0, 90, 0]) {
            methane_space();
        }
}

module methane_moved() {
    translate([cube_side + skin - cube_tip_clip*2 - 5, 0, 0])
        rotate([0, 90, 0]) {
            methane();
        }
}

module poly(clip=0, shorten=0) {
    difference() {
        polyhedron(
            points=[[shorten,cube_side,0],
                [shorten,0,-cube_side],
                [shorten,-cube_side,0],
                [shorten,0,cube_side],
                [cube_side,0,0]],
            faces=[[0,1,4],[1,2,4],[2,3,4],[3,0,4],[1,0,3],[2,1,3]]);
        union() {
            translate([-2*cube_side, -2*cube_side, clip/.85])
                cube([cube_side*4, cube_side*4, cube_side]);
            translate([cube_side - clip/2, 0, 0])
                cube([clip*2, clip*3, clip*3], center = true);
            side_block(wall_width*side_clip_ratio, 180);
            side_block(wall_width*side_clip_ratio, 0);
        }
    }
}

module wall_tunnel() {
    translate([-2*cube_side, -wall_width/2, -2*cube_side - cube_tip_clip/.7])
        cube([4*cube_side, wall_width, 2*cube_side]);
    left_wing_clip();     // Comment this line to make wing full size
    right_wing_clip();     // Comment this line to make wing full size
}

module left_wing_clip() {
    translate([-cube_side, 0, -left_wing_clip_height - cube_side])
        cube([cube_side*2, cube_side, cube_side]);            
}

module right_wing_clip() {
    translate([-cube_side, -cube_side, -right_wing_clip_height - cube_side])
        cube([cube_side*2, cube_side, cube_side]);            
}

module box() {
    difference() {
        minkowski() {
            difference() {
                union() {
                    poly(clip=cube_tip_clip, shorten=0);
                    intersection() {
                        hull() {
                            poly(clip=cube_tip_clip, shorten=0);
                        }
                        minkowski() {
                            wall_tunnel();
                            sphere(r=skin);
                        }
                    }
                }
            }
            sphere(r=skin);
        }
        union() {
            methane_moved_skin();
            difference() {
                minkowski() {
                    translate([-skin, 0, 0])
                        poly(clip=cube_tip_clip/2, shorten=10);
                    sphere(r=inner_skin);
                }
                minkowski() {
                    wall_tunnel();
                    sphere(r=skin - inner_skin);
                }
            }
            wall_tunnel();
        }
    }
}

module side_block(y=0, theta=0) {
    rotate([theta, 0, 0]) {
        translate([-2*cube_side, y, -2*cube_side])
            cube([4*cube_side, 3*wall_width, 4*cube_side]);
    }
}

module methane_space() {
    minkowski() {
        methane();
        sphere(r=thin_skin);
    }
}

module yun() {
    include <../3d-iot-component-models/arduino-yun-mini.scad>
}

module methane() {
    include <../3d-iot-component-models/methane-sensor.scad>
}

module motion() {
    include <../3d-iot-component-models/pir-motion-sensor.scad>
}
