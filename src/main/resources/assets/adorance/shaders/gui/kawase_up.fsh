#version 150

layout (std140) uniform KawaseParams {
    vec2  TexelSize;
    float Offset;
};

uniform sampler2D InSampler;

in  vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 h = TexelSize * Offset * 0.5;
    vec4 sum = texture(InSampler, texCoord + vec2(-h.x * 2.0, 0.0));
    sum += texture(InSampler, texCoord + vec2(-h.x, h.y)) * 2.0;
    sum += texture(InSampler, texCoord + vec2(0.0, h.y * 2.0));
    sum += texture(InSampler, texCoord + vec2(h.x, h.y)) * 2.0;
    sum += texture(InSampler, texCoord + vec2(h.x * 2.0, 0.0));
    sum += texture(InSampler, texCoord + vec2(h.x, -h.y)) * 2.0;
    sum += texture(InSampler, texCoord + vec2(0.0, -h.y * 2.0));
    sum += texture(InSampler, texCoord + vec2(-h.x, -h.y)) * 2.0;
    fragColor = vec4((sum / 12.0).rgb, 1.0);
}
