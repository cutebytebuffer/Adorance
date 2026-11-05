#version 150

uniform sampler2D IconSampler;

layout (std140) uniform IconParams {
    vec4  Rect;
    vec2  ScreenSize;
    float Rotation;
    float Smoothing;
    vec4  Tint;
};

out vec4 fragColor;

void main() {
    vec2 local = gl_FragCoord.xy - (Rect.xy + Rect.zw * 0.5);
    float s = sin(-Rotation);
    float c = cos(-Rotation);
    vec2 rotated = vec2(local.x * c - local.y * s, local.x * s + local.y * c);
    vec2 uv = (rotated + Rect.zw * 0.5) / Rect.zw;

    vec2 edge = max(vec2(Smoothing) / max(Rect.zw, vec2(0.0001)), vec2(0.0001));
    float mask = smoothstep(-edge.x, edge.x, uv.x) * smoothstep(-edge.x, edge.x, 1.0 - uv.x)
               * smoothstep(-edge.y, edge.y, uv.y) * smoothstep(-edge.y, edge.y, 1.0 - uv.y);

    float alpha = texture(IconSampler, clamp(vec2(uv.x, 1.0 - uv.y), 0.0, 1.0)).a * Tint.a * mask;
    if (alpha < 0.003) {
        discard;
    }
    fragColor = vec4(Tint.rgb, alpha);
}
