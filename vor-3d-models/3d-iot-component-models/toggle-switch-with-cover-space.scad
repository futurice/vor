// A toggle switch ordered from https://www.sparkfun.com/products/11310

$fn = 32;

length = 28;
width = 17;
height = 19.12;

plate_thickness = 1;
cover_z = 6.82;

toggle_switch();
toggle_cover();

module toggle_switch() {
    translate([0, 0, -height - cover_z]) 
        union() {
            base();
            color("silver") prong1();
            color("orange") hull() {
                prong2();
                prong3();
            }
            color("black") shaft();
            color("silver") plate();
            color("red") toggle();
         }
}

module toggle_cover() {
    translate([0, 0, -height - cover_z]) 
cover();
}

module base() {
    cube([length, width, height]);
}

module prong1() {
    translate([length-1.5-4, width/5, -10.6]) cube([4, 3*width/5, 10.6]);
}

module prong2() {
    translate([1.5, width/5, -10.6]) cube([4, 3*width/5, 10.6]);
}

module prong3() {
    corner = 7;
    fudge = .266;
    translate([-9 + corner/2, corner/2 - fudge, -11 + corner/2])
    minkowski() {
        cube([12 - corner, 4*width/5 - corner + fudge, 27 - corner]);
        rotate([0, 45, 0]) {
        sphere($fn=8, r = corner/2);
       }
    }
}

module shaft() {
    translate([length/2, width/2, height]) cylinder(h = 13, r = 11.74 / 2);
    translate([length/2, width/2, height + 3]) cylinder(h = 6, r = 16.55 / 2);
}

module plate() {
    translate([0, 0, height + 6]) cube([40.6, 17, plate_thickness]);
}

module toggle() {
    translate([length/2, width/2, height + 9]) rotate([0, -15, 0]) {
        cylinder(h = 15, r = 7.22/2);    
    }
}

module cover() {
    color("red") translate([0, 0, height + cover_z])
        cube([40.6, width, 43.9 - 17.56]);
}
