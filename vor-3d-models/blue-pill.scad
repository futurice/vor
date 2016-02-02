$fn=256;

length=40;
radius=length/2;
depth=2;
text_offset=0;
text_scale=.5;

difference() {
    hull() {
        sphere(r=radius);
        translate([length,0,0])
        sphere(r=radius);
    }
    union() {
        message(theta=-135, s="Not Supported");
        message(theta=-90, s="Monitored");
        message(theta=-45, s="Hacked Cloud");
        message(theta=0, s="Proprietary");
        message(theta=45, s="Vendor Lock");
        message(theta=90, s="Monthly Fee");
        message(theta=135, s="Agent Smith");
        message(theta=180, s="Centralized");
    }
}

module message(theta=0, s="message") {
    rotate([theta,0,0]) translate([text_offset,0,radius-depth]) scale([text_scale, text_scale,10]) linear_extrude(1) text(s);
}

