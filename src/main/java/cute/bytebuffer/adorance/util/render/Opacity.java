package cute.bytebuffer.adorance.util.render;

public final class Opacity {

    private static final int MAX_DEPTH = 64;
    private static final float[] STACK = new float[MAX_DEPTH];

    private static int depth;
    private static float current = 1f;

    private Opacity() {
    }

    public static void push(float factor) {
        if (depth == MAX_DEPTH) {
            throw new IllegalStateException("Opacity stack overflow");
        }
        STACK[depth++] = current;
        current = Math.max(0f, Math.min(1f, current * factor));
    }

    public static void pop() {
        current = depth > 0 ? STACK[--depth] : 1f;
    }

    public static float current() {
        return current;
    }

    public static boolean isInvisible() {
        return current <= 0.002f;
    }

    public static int apply(int argb) {
        return ColorUtil.multiplyAlpha(argb, current);
    }

    public static void reset() {
        depth = 0;
        current = 1f;
    }
}
