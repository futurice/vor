// Bathroom Sensor 3D Model with methane sensor, 2 motion sensors and Arduino Yun, http://vor.space
// ©Futurice Oy, paul.houghton@futurice.com, CC-attribution-sharealike license, http://creativecommons.org/licenses/by-sa/4.0/
// Be vary patient, this model may take an hour or more to render

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
yun_skin = 0.3;

sensor_x=94;
sensor_y=42;

yun_x = 7;
yun_y = -22.9/2;
yun_z = -3;

logo_spread=67;

text_x=27;
text_y=12;
text_spread=32;

spike_z1 = 23;
spike_z2 = 51;
spike_skin = .2;
dx=1;

//bathroom_sensor_shell();
//bathroom_sensor_shell_left();
bathroom_sensor_shell_right();
bathroom_sensors_shown();

module bathroom_sensor_shell_left() {
    difference() {
        bathroom_sensor_shell();
        union() {
            translate([-50,0,-cube_side]) cube([cube_side*2, cube_side*2, cube_side*2]);
            unspike(z=spike_z1,skin=spike_skin);
            unspike(z=spike_z2,skin=spike_skin);
        }
    }
    spike(z=spike_z1);
    spike(z=spike_z2);
}

module bathroom_sensor_shell_right() {
    difference() {
        bathroom_sensor_shell();
        union() {
            translate([-50,-cube_side*2,-cube_side]) cube([cube_side*2, cube_side*2, cube_side*2]);
            spike(z=spike_z1,skin=spike_skin);
            spike(z=spike_z2,skin=spike_skin);
        }
    }
    unspike(z=spike_z1);
    unspike(z=spike_z2);
}

module spike(z=0, skin=0) {
    translate([-3+dx,-4.3,z+skin/2]) rotate([0,0,45]) {
        minkowski() {
            cube([8,4,3.5]);
            sphere(r=skin);
        }
    }
}

module unspike(z=0, skin=0) {
    translate([4+dx,-4.3,z+skin/2]) rotate([0,0,45]) {
        minkowski() {
            cube([8,4,3.5]);
            sphere(r=skin);
        }
    }
}

module bathroom_sensor_shell() {
    linear_extrude(height=2) {
        text_imprint(theta=0,x=-text_x,y=text_y+text_spread);
        text_imprint(theta=180,x=text_x,y=-text_y-text_spread);
    }
    translate([0,0,36]) union() {
        difference() {
            union() {
                color("red") box();
                color("blue") yun_holder();
                color("pink") translate([123,0,0]) rotate([0,90,0]) {
                    cylinder(r=14.2,h=8);
                }
                logo_moved(theta=0, y=-logo_spread);
                logo_moved(theta=180, y=logo_spread);
            }
            union() {
                methane_space_moved();
                motion_moved_skin(y=-sensor_y, theta=0, rot=120);
                motion_moved_skin(y=sensor_y, theta=90, rot=120+180);
                yun_space_moved_skin();
                translate([129,-7,14]) rotate([90,0,0]) m3();
                translate([129,7,14]) rotate([-90,0,0]) m3();
            }
        }
        color("green") yun_airgap_feet();
    }
}

module bathroom_sensors_shown() {
    translate([0,0,36]) union() {
        yun_moved();
        methane_moved();
        motion_moved(y=-sensor_y, theta=0, rot=120);
        motion_moved(y=sensor_y, theta=90, rot=120+180);
    }
}

module motion_moved(y=0, theta=0, rot=0) {
    translate([sensor_x, y, -20]) 
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

module yun_space_moved_skin() {
    minkowski() {
        yun_space_moved();
        sphere(r=yun_skin);
    }
}

module yun_space_moved() {
    translate([yun_x, yun_y, yun_z])
        yun_space();
}

module yun_moved() {
    translate([yun_x, yun_y, yun_z])
        yun();
}

module yun_holder() {
    translate([-7.5, -wall_width/2, -13])
        cube([81, wall_width, 5]);
}

module yun_airgap_feet() {
    translate([-9.9, -wall_width/2, -17])
        cube([130, 6, 3]);
    translate([-9.9, wall_width/2 - 6, -17])
        cube([130, 6, 3]);
}

module methane_space_moved() {
    translate([cube_side + skin - cube_tip_clip*2 - 5, 0, 0])
        rotate([0, 90, 0]) minkowski() {
            methane_space();
            sphere(r=thin_skin);
        }
}

module methane_moved() {
    translate([cube_side + skin - cube_tip_clip*2 - 5, 0, 0])
        rotate([0, 90, 0]) methane();
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
            methane_space_moved();
            difference() {
                minkowski() {
                    translate([-skin, 0, 0])
                        poly(clip=cube_tip_clip/2, shorten=10);
                    sphere(r=inner_skin);
                }
                minkowski() {
                    wall_tunnel();
                    sphere(r=skin-inner_skin);
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
        methane_whitespace();
        sphere(r=thin_skin);
    }
}

module text_imprint(theta=0,x=0,y=0) {
    translate([x+40,y,0]) rotate([180,0,theta]) {
        translate([0,9,0]) scale([.6,.6,1]) linear_extrude(1) text("http://vor.space");
        scale(.6,.6,1) linear_extrude(1) text("by futurice");
    }
}

module logo_moved(theta=0, y=0) {
    s=.2;
    translate([41, y, -9]) rotate([90,0,theta]) {
        scale([s,s,s]) logo();
    }
}

module logo() {
    import("vor-logo-embossed.stl", convexity=10);
}

module yun() {
    include <arduino-yun-mini.scad>
}

module yun_space() {
    include <arduino-yun-mini-negative-space.scad>
}

module methane() {
    include <methane-sensor.scad>
}

module methane_whitespace() {
    include <methane-sensor-space.scad>
}

module motion() {
    include <pir-motion-sensor.scad>
}

module m3() {
    include <m3.scad>
}
