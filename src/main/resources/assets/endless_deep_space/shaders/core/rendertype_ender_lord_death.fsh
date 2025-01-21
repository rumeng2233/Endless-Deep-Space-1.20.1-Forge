#version 150

#moj_import <matrix.glsl>

uniform sampler2D Sampler0;
uniform sampler2D Sampler1;

uniform float EDSGameTime;
uniform int EndPortalLayers;

in vec4 texProj0;

const vec3[] COLORS = vec3[](
    vec3(0.0, 0.5450980392156863, 0.5529411764705882),
    vec3(0.0, 0.9176470588235294, 0.7882352941176471),
    vec3(0.0, 0.5647058823529412, 0.8823529411764706),
    vec3(0.0, 0.7058823529411765, 0.4627450980392157),
    vec3(0.0, 0.596078431372549, 0.6627450980392157),
    vec3(0.0, 1.0, 1.0),
    vec3(0.0588235294117647, 0.1686274509803922, 0.1686274509803922),
    vec3(0.0745098039215686, 0.2196078431372549, 0.2117647058823529),
    vec3(0.1764705882352941, 0.5607843137254902, 0.5372549019607843),
    vec3(0.2196078431372549, 0.6862745098039216, 0.6627450980392157),
    vec3(0.2784313725490196, 0.8745098039215686, 0.8549019607843137),
    vec3(0.2941176470588235, 0.9137254901960784, 0.8823529411764706),
    vec3(0.2666666666666667, 0.8235294117647059, 0.796078431372549),
    vec3(0.3176470588235294, 0.9803921568627451, 0.9490196078431373),
    vec3(0.3019607843137255, 0.8745098039215686, 0.8549019607843137),
    vec3(0.3450980392156863, 1.0, 1.0)
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
