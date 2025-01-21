#version 150

#moj_import <matrix.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform float EDSGameTime;
uniform int EndPortalLayers;

in vec4 texProj0;

const vec3[] COLORS = vec3[](
    vec3(0.3294117647058824, 0.0352941176470588, 0.0549019607843137),
    vec3(0.011892, 0.095924, 0.089485),
    vec3(0.027636, 0.101689, 0.100326),
    vec3(0.046564, 0.109883, 0.114838),
    vec3(0.064901, 0.117696, 0.097189),
    vec3(0.6117647058824, 0.06274509803922, 0.09019607843137),
    vec3(1.0, 0.1098039215686, 0.1686274509804),
    vec3(1.0, 0.243137254902, 0.4039215686275),
    vec3(1.0, 0.3098039215686, 0.1686274509804),
    vec3(0.7058823529412, 0.07450980392157, 0.1176470588235),
    vec3(0.4588235294118, 0.156862745098, 0.007843137254902),
    vec3(0.7058823529412, 0.07450980392157, 0.1176470588235),
    vec3(0.6117647058823529, 0.0627450980392157, 0.0901960784313725),
    vec3(1.0, 0.3725490196078431, 0.403921568627451),
    vec3(0.6549019607843137, 0.203921568627451, 0.1450980392156863),
    vec3(1.0, 0.0, 0.0)
);

const mat4 SCALE_TRANSLATE = mat4(
    0.5, 0.0, 0.0, 0.25,
    0.0, 0.5, 0.0, 0.25,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0
);

mat4 end_portal_layer(float layer) {
    mat4 translate = mat4(
        1.0, 0.0, 0.0, 17.0 / layer,
        0.0, 1.0, 0.0, (2.0 + layer / 1.5) * (EDSGameTime * 1.5),
        0.0, 0.0, 1.0, 0.0,
        0.0, 0.0, 0.0, 1.0
    );

    mat2 rotate = mat2_rotate_z(radians((layer * layer * 4321.0 + layer * 9.0) * 2.0));

    mat2 scale = mat2((4.5 - layer / 4.0) * 2.0);

    return mat4(scale * rotate) * translate * SCALE_TRANSLATE;
}

out vec4 fragColor;

void main() {
    vec3 color = textureProj(Sampler0, texProj0).rgb * COLORS[0];
    for (int i = 0; i < EndPortalLayers; i++) {
        color += textureProj(Sampler1, texProj0 * end_portal_layer(float(i + 1))).rgb * COLORS[i];
    }
    fragColor = vec4(color, 1.0);
}
