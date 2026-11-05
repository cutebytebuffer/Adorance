package cute.bytebuffer.adorance.gui.frame;

import cute.bytebuffer.adorance.gui.animation.Easing;

final class Entrance {

    private static final float FRAME_DURATION = 0.34f;
    private static final float FRAME_START_SCALE = 0.94f;
    private static final float ITEM_START = 0.12f;
    private static final float ITEM_STAGGER = 0.035f;
    private static final float ITEM_DURATION = 0.3f;
    private static final float ITEM_SLIDE = 8f;
    private static final float CONTENT_START = 0.2f;
    private static final float CONTENT_DURATION = 0.32f;

    private long startNanos = -1L;

    void restart() {
        startNanos = System.nanoTime();
    }

    private float elapsed() {
        return startNanos < 0L ? Float.MAX_VALUE : (System.nanoTime() - startNanos) / 1_000_000_000f;
    }

    private float progress(float start, float duration) {
        return Easing.clamp((elapsed() - start) / duration);
    }

    float frameScale() {
        return FRAME_START_SCALE + (1f - FRAME_START_SCALE) * Easing.softSpring(progress(0f, FRAME_DURATION));
    }

    float frameAlpha() {
        return Easing.outCubic(progress(0f, FRAME_DURATION));
    }

    float itemAlpha(int index) {
        return Easing.outCubic(progress(ITEM_START + index * ITEM_STAGGER, ITEM_DURATION));
    }

    float itemOffset(int index) {
        return -ITEM_SLIDE * (1f - Easing.outQuint(progress(ITEM_START + index * ITEM_STAGGER, ITEM_DURATION)));
    }

    float contentAlpha() {
        return Easing.outCubic(progress(CONTENT_START, CONTENT_DURATION));
    }

    float contentDelay() {
        return Math.max(0f, CONTENT_START - elapsed());
    }
}
