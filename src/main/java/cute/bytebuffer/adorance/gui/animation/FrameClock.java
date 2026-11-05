package cute.bytebuffer.adorance.gui.animation;

public final class FrameClock {

    private static final float MAX_DELTA = 0.1f;

    private long lastNanos = -1L;

    public float tick() {
        long now = System.nanoTime();
        if (lastNanos < 0L) {
            lastNanos = now;
            return 0f;
        }
        float delta = (now - lastNanos) / 1_000_000_000f;
        lastNanos = now;
        return Math.max(0f, Math.min(delta, MAX_DELTA));
    }

    public void reset() {
        lastNanos = -1L;
    }
}
