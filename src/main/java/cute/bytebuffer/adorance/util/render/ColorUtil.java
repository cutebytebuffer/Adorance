package cute.bytebuffer.adorance.util.render;

public final class ColorUtil {

    private static final float[] TO_LINEAR = new float[256];
    private static final int SRGB_STEPS = 4096;
    private static final int[] TO_SRGB = new int[SRGB_STEPS + 1];

    static {
        for (int i = 0; i < TO_LINEAR.length; i++) {
            float c = i / 255f;
            TO_LINEAR[i] = c <= 0.04045f ? c / 12.92f : (float) Math.pow((c + 0.055) / 1.055, 2.4);
        }
        for (int i = 0; i <= SRGB_STEPS; i++) {
            double c = i / (double) SRGB_STEPS;
            double s = c <= 0.0031308 ? c * 12.92 : 1.055 * Math.pow(c, 1.0 / 2.4) - 0.055;
            TO_SRGB[i] = (int) Math.round(Math.max(0.0, Math.min(1.0, s)) * 255.0);
        }
    }

    private ColorUtil() {
    }

    public static int alpha(int argb) {
        return argb >>> 24;
    }

    public static int red(int argb) {
        return argb >> 16 & 0xFF;
    }

    public static int green(int argb) {
        return argb >> 8 & 0xFF;
    }

    public static int blue(int argb) {
        return argb & 0xFF;
    }

    public static int argb(int alpha, int red, int green, int blue) {
        return (clamp255(alpha) << 24) | (clamp255(red) << 16) | (clamp255(green) << 8) | clamp255(blue);
    }

    public static int withAlpha(int argb, int alpha) {
        return (clamp255(alpha) << 24) | (argb & 0x00FFFFFF);
    }

    public static int multiplyAlpha(int argb, float factor) {
        if (factor >= 1f) {
            return argb;
        }
        if (factor <= 0f) {
            return argb & 0x00FFFFFF;
        }
        return withAlpha(argb, Math.round(alpha(argb) * factor));
    }

    public static float[] toVec4(int argb) {
        return new float[]{red(argb) / 255f, green(argb) / 255f, blue(argb) / 255f, alpha(argb) / 255f};
    }

    public static float luminance(int argb) {
        return 0.2126f * TO_LINEAR[red(argb)] + 0.7152f * TO_LINEAR[green(argb)] + 0.0722f * TO_LINEAR[blue(argb)];
    }

    public static int readableOn(int background) {
        return luminance(background) > 0.36f ? 0xFF15141A : 0xFFFFFFFF;
    }

    public static int fromHsv(float hue, float saturation, float value, int alpha) {
        int rgb = java.awt.Color.HSBtoRGB(hue, clamp01(saturation), clamp01(value));
        return withAlpha(rgb, alpha);
    }

    public static float[] toHsv(int argb) {
        return java.awt.Color.RGBtoHSB(red(argb), green(argb), blue(argb), null);
    }

    public static int lerp(int from, int to, float t) {
        if (from == to || t <= 0f) {
            return from;
        }
        if (t >= 1f) {
            return to;
        }
        float fromAlpha = alpha(from) / 255f;
        float toAlpha = alpha(to) / 255f;
        float alpha = fromAlpha + (toAlpha - fromAlpha) * t;
        float colorT = alpha > 1e-4f ? t * toAlpha / alpha : t;
        if (fromAlpha <= 1e-4f) {
            colorT = 1f;
        }
        if (toAlpha <= 1e-4f) {
            colorT = 0f;
        }
        return (Math.round(alpha * 255f) << 24) | oklabMix(from, to, colorT);
    }

    private static int oklabMix(int from, int to, float t) {
        if (t <= 0f) {
            return from & 0x00FFFFFF;
        }
        if (t >= 1f) {
            return to & 0x00FFFFFF;
        }
        float[] a = toOklab(from);
        float[] b = toOklab(to);
        float lightness = a[0] + (b[0] - a[0]) * t;
        float greenRed = a[1] + (b[1] - a[1]) * t;
        float blueYellow = a[2] + (b[2] - a[2]) * t;
        float l = cube(lightness + 0.3963377774f * greenRed + 0.2158037573f * blueYellow);
        float m = cube(lightness - 0.1055613458f * greenRed - 0.0638541728f * blueYellow);
        float s = cube(lightness - 0.0894841775f * greenRed - 1.2914855480f * blueYellow);
        int red = toSrgb(4.0767416621f * l - 3.3077115913f * m + 0.2309699292f * s);
        int green = toSrgb(-1.2684380046f * l + 2.6097574011f * m - 0.3413193965f * s);
        int blue = toSrgb(-0.0041960863f * l - 0.7034186147f * m + 1.7076147010f * s);
        return (red << 16) | (green << 8) | blue;
    }

    private static float[] toOklab(int argb) {
        float r = TO_LINEAR[red(argb)];
        float g = TO_LINEAR[green(argb)];
        float b = TO_LINEAR[blue(argb)];
        float l = (float) Math.cbrt(0.4122214708f * r + 0.5363325363f * g + 0.0514459929f * b);
        float m = (float) Math.cbrt(0.2119034982f * r + 0.6806995451f * g + 0.1073969566f * b);
        float s = (float) Math.cbrt(0.0883024619f * r + 0.2817188376f * g + 0.6299787005f * b);
        return new float[]{
                0.2104542553f * l + 0.7936177850f * m - 0.0040720468f * s,
                1.9779984951f * l - 2.4285922050f * m + 0.4505937099f * s,
                0.0259040371f * l + 0.7827717662f * m - 0.8086757660f * s
        };
    }

    private static float cube(float value) {
        return value * value * value;
    }

    private static int toSrgb(float linear) {
        return TO_SRGB[Math.round(clamp01(linear) * SRGB_STEPS)];
    }

    private static float clamp01(float value) {
        return Math.max(0f, Math.min(1f, value));
    }

    private static int clamp255(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
