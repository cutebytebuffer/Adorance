package cute.bytebuffer.adorance.gui.animation;

public final class Easing {

    private static final float SPRING_FREQUENCY = 9f;
    private static final float SPRING_DECAY = 7.5f;

    private Easing() {
    }

    public static float approach(float current, float target, float speed, float dt) {
        if (Math.abs(target - current) < 0.0005f) {
            return target;
        }
        return current + (target - current) * (1f - (float) Math.exp(-speed * dt));
    }

    public static float outCubic(float t) {
        float inverse = 1f - clamp(t);
        return 1f - inverse * inverse * inverse;
    }

    public static float outQuint(float t) {
        float inverse = 1f - clamp(t);
        return 1f - inverse * inverse * inverse * inverse * inverse;
    }

    public static float inOutCubic(float t) {
        t = clamp(t);
        return t < 0.5f ? 4f * t * t * t : 1f - (float) Math.pow(-2f * t + 2f, 3f) / 2f;
    }

    public static float softSpring(float t) {
        t = clamp(t);
        if (t <= 0f || t >= 1f) {
            return t;
        }
        return 1f - (float) Math.exp(-SPRING_DECAY * t)
                * ((float) Math.cos(SPRING_FREQUENCY * t) + (SPRING_DECAY / SPRING_FREQUENCY) * (float) Math.sin(SPRING_FREQUENCY * t));
    }

    public static float lerp(float from, float to, float t) {
        return from + (to - from) * t;
    }

    public static float clamp(float t) {
        return Math.max(0f, Math.min(1f, t));
    }
}
