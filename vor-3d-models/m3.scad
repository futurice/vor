// M3 bolt negative space for creating holes in 3D printable models
// Part of Vör, http://vor.space
// ©Futurice Oy, paul.houghton@futurice.com, CC-attribution-sharealike license, http://creativecommons.org/licenses/by-sa/4.0/

shaft_radius = 3.2/2;
head_radius = 5.9/2;

m3_bolt_space();

module m3_bolt_space(head_extra=2, shaft_extra=.3, head_height=50, length=30) {
    cylinder($fn=128, h=head_height, r=head_radius + head_extra);

    translate([0, 0, -length])
        cylinder($fn=128, h=length, r=shaft_radius + shaft_extra);
}