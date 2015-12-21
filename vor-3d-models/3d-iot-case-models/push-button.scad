// Bathroom Sensor 3D Model with methane sensor, 2 motion sensors and Arduino Yun

$fn = 8;

cube_side = 50;
cube_tip_clip = 40;

skin = 10;
inner_skin = 8;
thin_skin = 0.1;

yun_y = -wall_width;
yun_z = -20;

color("red") box();

//yun_moved();
//color("blue") yun_holder();

module motion_moved(y=0, theta=0) {
    translate([95, y, -20]) 
        rotate([36, 137, theta]) {
            motion_angle(-60);
        }
}

module motion_moved_skin(y=0, theta=0) {
    minkowski() {
        motion_moved(y, theta);
        sphere(r=thin_skin);
    }
}

module yun_moved() {
    translate([-skin, yun_y, yun_z])
        rotate([180, 0, 0]) {
            yun();
        }
}

module yun_moved_skin() {
    minkowski() {
        yun_moved();
        sphere(r=thin_skin);
    }
}

module yun_holder() {
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
        translate([-2*cube_side, -2*cube_side, cube_tip_clip])
            cube([cube_side*4, cube_side*4, cube_side]);
    }
}

module box() {
    poly();
}

module yun() {
    include <../3d-iot-component-models/arduino-yun-mini.scad>
}

