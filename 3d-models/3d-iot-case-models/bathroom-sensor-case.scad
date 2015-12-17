// Bathroom Sensor 3D Model with methane sensor, 2 motion sensors and Arduino Yun

$fn = 8;

cube_side = 140;
cube_tip_clip = 12;
skin = 10;
inner_skin = 5;
thin_skin = 0.05;

difference() {
    color("red") box();
    union() {
        methane_moved_skin();
    }
}

//yun_moved();
methane_moved();

module yun() {
    include <../3d-iot-component-models/arduino-yun-mini.scad>
}

module methane() {
    include <../3d-iot-component-models/methane-sensor.scad>
}

module motion() {
    include <../3d-iot-component-models/pir-motion-sensor.scad>
}

module yun_moved() {
    translate([0, -cube_side/3 + skin, 0]) yun();
}

module yun_moved_skin() {
    minkowski() {
        yun_moved();
        sphere(r=thin_skin);
    }
}

module methane_moved() {
    translate([cube_side + skin - cube_tip_clip*2, 0, 0])
        rotate([0, 90, 0]) {
            methane();
        }
}

module methane_moved_skin() {
    minkowski() {
        methane_moved();
        sphere(r=thin_skin);
    }
}

module poly(clip = 0) {
    difference() {
        polyhedron(
            points=[[0,cube_side,0],
                [0,0,-cube_side],
                [0,-cube_side,0],
                [0,0,cube_side],
                [cube_side,0]],
            faces=[[0,1,4],[1,2,4],[2,3,4],[3,0,4],[1,0,3],[2,1,3]],
            convexivity=8);        
        translate([cube_side - clip/2, 0, 0])
            cube([clip*2, clip*3, clip*3], center = true);
    }
}

module box() {
    difference() {
        minkowski() {
            poly(cube_tip_clip);
            sphere(r=skin);
        }
        union() {
            methane_moved_skin();
            minkowski() {
                translate([-skin, 0, 0])
                    poly(cube_tip_clip/2);
                sphere(r=inner_skin);
            }
        }
    }
}
