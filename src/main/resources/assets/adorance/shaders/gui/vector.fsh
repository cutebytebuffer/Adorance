#version 330 core

layout (std140) uniform VectorParams {
    vec4  Region;
    vec2  ScreenSize;
    vec4  CornerRadii;
    vec4  FillColor;
    vec4  GradientFrom;
    vec4  GradientTo;
    vec2  GradientDir;
    float BorderWidth;
    vec4  BorderColor;
    float ShapeType;
    float FillMode;
    float Softness;
    float Backdrop;
    vec4  LinePoints;
    float GlobalAlpha;
};

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

float hash12(vec2 p) {
    vec3 p3 = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

vec3 dither(vec3 color) {
    float noise = hash12(gl_FragCoord.xy) + hash12(gl_FragCoord.xy + vec2(17.31, 41.7)) - 1.0;
    return color + noise / 255.0;
}

float grain() {
    return (hash12(gl_FragCoord.xy * 1.37 + vec2(3.1, 7.9)) - 0.5) * 0.018;
}

vec4 mixPremultiplied(vec4 from, vec4 to, float t) {
    vec4 mixed = mix(vec4(from.rgb * from.a, from.a), vec4(to.rgb * to.a, to.a), t);
    return mixed.a > 0.0001 ? vec4(mixed.rgb / mixed.a, mixed.a) : vec4(0.0);
}

vec3 hsvToRgb(vec3 hsv) {
    vec3 p = abs(fract(hsv.xxx + vec3(0.0, 0.66666667, 0.33333333)) * 6.0 - 3.0);
    return hsv.z * mix(vec3(1.0), clamp(p - 1.0, 0.0, 1.0), hsv.y);
}

float cornerRadius(vec2 pos, vec4 radii) {
    if (pos.y > 0.0) {
        return pos.x < 0.0 ? radii.x : radii.y;
    }
    return pos.x > 0.0 ? radii.z : radii.w;
}

float sdRoundedBox(vec2 pos, vec2 halfSize, vec4 radii) {
    float radius = min(cornerRadius(pos, radii), min(halfSize.x, halfSize.y));
    vec2 q = abs(pos) - halfSize + vec2(radius);
    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - radius;
}

vec2 boxNormal(vec2 pos, vec2 halfSize, vec4 radii) {
    vec2 e = vec2(0.5, 0.0);
    vec2 gradient = vec2(
        sdRoundedBox(pos + e.xy, halfSize, radii) - sdRoundedBox(pos - e.xy, halfSize, radii),
        sdRoundedBox(pos + e.yx, halfSize, radii) - sdRoundedBox(pos - e.yx, halfSize, radii));
    float len = length(gradient);
    return len > 1e-5 ? gradient / len : vec2(0.0, 1.0);
}

float sdSegment(vec2 p, vec2 a, vec2 b) {
    vec2 pa = p - a;
    vec2 ba = b - a;
    float h = clamp(dot(pa, ba) / max(dot(ba, ba), 1e-5), 0.0, 1.0);
    return length(pa - ba * h);
}

vec2 erf2(vec2 x) {
    vec2 s = sign(x);
    vec2 a = abs(x);
    x = 1.0 + (0.278393 + (0.230389 + 0.078108 * (a * a)) * a) * a;
    x *= x;
    return s - s / (x * x);
}

float gaussian(float x, float sigma) {
    return exp(-(x * x) / (2.0 * sigma * sigma)) / (2.50662827 * sigma);
}

float shadowRow(float x, float y, float sigma, float corner, vec2 halfSize) {
    float delta = min(halfSize.y - corner - abs(y), 0.0);
    float curved = halfSize.x - corner + sqrt(max(0.0, corner * corner - delta * delta));
    vec2 integral = 0.5 + 0.5 * erf2((x + vec2(-curved, curved)) * (0.70710678 / sigma));
    return integral.y - integral.x;
}

float boxShadow(vec2 p, vec2 halfSize, float sigma, float corner) {
    float low = p.y - halfSize.y;
    float high = p.y + halfSize.y;
    float start = clamp(-3.0 * sigma, low, high);
    float end = clamp(3.0 * sigma, low, high);
    float stepY = (end - start) / 4.0;
    float y = start + stepY * 0.5;
    float value = 0.0;
    for (int i = 0; i < 4; i++) {
        value += shadowRow(p.x, p.y - y, sigma, corner, halfSize) * gaussian(y, sigma) * stepY;
        y += stepY;
    }
    return value;
}

vec4 evaluateFill(vec2 pos, vec2 halfSize) {
    if (FillMode < 0.5) {
        return FillColor;
    }
    vec4 color;
    if (FillMode < 1.5) {
        float t = clamp(dot(normalize(GradientDir), pos / halfSize) * 0.5 + 0.5, 0.0, 1.0);
        color = mixPremultiplied(GradientFrom, GradientTo, t);
    } else {
        float hue = clamp(pos.x / halfSize.x * 0.5 + 0.5, 0.0, 1.0);
        color = vec4(hsvToRgb(vec3(hue, 1.0, 1.0)), 1.0);
    }
    color.a *= GlobalAlpha;
    return color;
}

vec4 shade(vec2 pos, vec2 halfSize) {
    vec4 color = evaluateFill(pos, halfSize);
    if (Backdrop > 0.5) {
        vec3 background = texture(InSampler, texCoord).rgb + grain();
        color = vec4(mix(background, color.rgb, color.a), GlobalAlpha);
    }
    return color;
}

vec4 applyBorder(vec4 color, vec2 normal, float dist, float aa, float mask) {
    if (BorderWidth <= 0.0) {
        return color;
    }
    float coverage = mask * smoothstep(-BorderWidth - aa, -BorderWidth + aa, dist);
    if (coverage <= 0.0) {
        return color;
    }
    vec4 border = BorderColor;
    border.rgb = mix(border.rgb, vec3(1.0), smoothstep(-0.4, 1.0, normal.y) * 0.12);
    border.a = clamp(border.a * (1.0 + 0.2 * normal.y), 0.0, 1.0);
    float weight = coverage * border.a;
    float alpha = weight + color.a * (1.0 - weight);
    vec3 rgb = alpha > 0.0 ? (border.rgb * weight + color.rgb * color.a * (1.0 - weight)) / alpha : color.rgb;
    return vec4(rgb, alpha);
}

void renderShadow(vec2 pos, vec2 halfSize, float dist) {
    float size = max(Softness, 0.75);
    float corner = min(max(max(CornerRadii.x, CornerRadii.y), max(CornerRadii.z, CornerRadii.w)) * 1.15, min(halfSize.x, halfSize.y));
    float key = boxShadow(pos + vec2(0.0, size * 0.22), halfSize, max(size * 0.28, 0.5), corner);
    float ambient = boxShadow(pos + vec2(0.0, size * 0.45), halfSize, max(size * 0.7, 0.5), corner);
    float strength = clamp(key * 0.75 + ambient * 0.55, 0.0, 1.0) * smoothstep(-0.75, 0.75, dist);
    if (strength <= 0.002) {
        discard;
    }
    fragColor = vec4(FillColor.rgb, FillColor.a * strength);
}

void renderRoundedRect(vec2 px) {
    vec2 halfSize = Region.zw * 0.5;
    vec2 pos = px - (Region.xy + halfSize);
    float dist = sdRoundedBox(pos, halfSize, CornerRadii);
    if (FillMode > 2.5) {
        renderShadow(pos, halfSize, dist);
        return;
    }
    float aa = max(fwidth(dist) * 0.8, 0.75);
    float mask = 1.0 - smoothstep(-aa, aa, dist);
    if (mask <= 0.0) {
        discard;
    }
    vec2 normal = BorderWidth > 0.0 ? boxNormal(pos, halfSize, CornerRadii) : vec2(0.0, 1.0);
    vec4 color = shade(pos, halfSize);
    color.a *= mask;
    fragColor = applyBorder(color, normal, dist, aa, mask);
}

void renderCircle(vec2 px) {
    vec2 halfSize = Region.zw * 0.5;
    vec2 pos = px - (Region.xy + halfSize);
    float dist = length(pos) - min(halfSize.x, halfSize.y);
    float aa = max(fwidth(dist) * 0.8, 0.75);
    float mask = 1.0 - smoothstep(-aa, aa, dist);
    if (mask <= 0.0) {
        discard;
    }
    float len = length(pos);
    vec2 normal = len > 1e-5 ? pos / len : vec2(0.0, 1.0);
    vec4 color = shade(pos, halfSize);
    color.a *= mask;
    fragColor = applyBorder(color, normal, dist, aa, mask);
}

void renderLine(vec2 px) {
    float dist = sdSegment(px, LinePoints.xy, LinePoints.zw) - BorderWidth * 0.5;
    float aa = max(fwidth(dist) * 0.8, 0.6);
    float coverage = 1.0 - smoothstep(-aa, aa, dist);
    if (coverage <= 0.0) {
        discard;
    }
    fragColor = vec4(FillColor.rgb, FillColor.a * coverage);
}

void main() {
    fragColor = vec4(0.0);
    vec2 px = texCoord * ScreenSize;
    float margin = FillMode > 2.5 ? Softness * 2.6 + 2.0 : 2.0;
    if (any(lessThan(px, Region.xy - margin)) || any(greaterThan(px, Region.xy + Region.zw + margin))) {
        discard;
    }
    if (ShapeType < 0.5) {
        renderRoundedRect(px);
    } else if (ShapeType < 1.5) {
        renderCircle(px);
    } else {
        renderLine(px);
    }
    if (fragColor.a > 0.98) {
        fragColor.rgb = dither(fragColor.rgb);
    }
}
