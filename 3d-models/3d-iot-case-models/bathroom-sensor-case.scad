// Bathroom Sensor 3D Model with methane sensor, 2 motion sensors and Arduino Yun

$fn = 16;

cube_side = 140;
cube_tip_clip = 12;
skin = 3;

difference() {
    color("red") box();
    //methane_moved_skin();
}

yun_moved();
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
    rotate([-0, 0, 0]) {
        translate([0, -cube_side/3, 0]) yun();
    }
}

module yun_moved_skin() {
    minkowski() {
        yun_moved();
        sphere(r=skin);
    }
}

module methane_moved() {
    translate([cube_side - cube_tip_clip, 0, 0])
        rotate([0, 90, 0]) {
            methane();
        }
}

module methane_moved_skin() {
    minkowski() {
        methane_moved();
        sphere(r=skin);
    }
}

module box() {
    difference() {
        polyhedron(
            points=[[0,cube_side,0],
                    [0,0,-cube_side],
                    [0,-cube_side,0],
                    [0,0,cube_side],
                    [cube_side,0]],
            faces=[[0,1,4],[1,2,4],[2,3,4],[3,0,4],[1,0,3],[2,1,3]]);
        union() {
            translate([cube_side - cube_tip_clip/2, 0, 0])
                cube([cube_tip_clip*2, cube_tip_clip*3, cube_tip_clip*3], center = true);
//            minkowski() {
//                methane_moved();
//                sphere(r=skin);
            }
    }
}


//translate([0, -10, 0)
//    motion();