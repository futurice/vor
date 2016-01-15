// M3 bolt negative space for creating holes in 3D printable models
// Part of http://vor.space
// ©Futurice Oy, paul.houghton@futurice.com, CC-attribution-sharealike license, http://creativecommons.org/licenses/by-sa/4.0/

$fn=256;

shaft_radius = 3.2/2;
head_radius = 5.9/2;

m3_bolt_space();

module m3_bolt_space(head_extra=2, shaft_extra=.25, head_height=40, length=40) {
    cylinder(h=head_height, r=head_radius + head_extra);

    translate([0, 0, -length])
        cylinder(h=length, r=shaft_radius + shaft_extra);
}