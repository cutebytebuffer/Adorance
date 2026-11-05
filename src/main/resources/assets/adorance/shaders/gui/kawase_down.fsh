#version 150

layout (std140) uniform KawaseParams {
    vec2  TexelSize;
    float Offset;
};

uniform sampler2D InSampler;

in  vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 h = TexelSize * Offset;
    vec4 sum = texture(InSampler, texCoord) * 4.0;
    sum += texture(InSampler, texCoord - h);
    sum += texture(InSampler, texCoord + h);
    sum += texture(InSampler, texCoord + vec2(h.x, -h.y));
    sum += texture(InSampler, texCoord - vec2(h.x, -h.y));
    fragColor = vec4((sum / 8.0).rgb, 1.0);
}
