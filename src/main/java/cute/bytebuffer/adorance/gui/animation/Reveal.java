package cute.bytebuffer.adorance.gui.animation;

public final class Reveal {

    private final float duration;
    private final float startScale;
    private long startNanos = -1L;

    public Reveal(float duration, float startScale) {
        this.duration = duration;
        this.startScale = startScale;
    }

    public void start(float delaySeconds) {
        startNanos = System.nanoTime() + (long) (delaySeconds * 1_000_000_000L);
    }

    public void finish() {
        startNanos = 0L;
    }

    public float progress() {
        if (startNanos < 0L) {
            return 0f;
        }
        float elapsed = (System.nanoTime() - startNanos) / 1_000_000_000f;
        return Easing.clamp(elapsed / duration);
    }

    public float alpha() {
        return Easing.outCubic(progress());
    }

    public float scale() {
        return startScale + (1f - startScale) * Easing.softSpring(progress());
    }

    public float offset(float distance) {
        return distance * (1f - Easing.outQuint(progress()));
    }
}
