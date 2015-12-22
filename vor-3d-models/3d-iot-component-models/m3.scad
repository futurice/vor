// m3 bolt negative space

$fn=16;

shaft_radius = 2.9/2;
head_radius = 5.9/2;

m3_bolt_space();

module m3_bolt_space(head_extra=2, shaft_extra=.25, head_height=40, length=40) {
    cylinder(h=head_height, r=head_radius + head_extra);

    translate([0, 0, -length])
        cylinder(h=length, r=shaft_radius + shaft_extra);
}