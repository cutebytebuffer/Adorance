package cute.bytebuffer.adorance.gui.frame;

import cute.bytebuffer.adorance.gui.animation.Easing;
import cute.bytebuffer.adorance.gui.animation.Smooth;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.util.render.ColorUtil;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.util.Rect;

final class ScrollView {

    private static final float STEP = 28f;
    private static final float SPEED = 16f;
    private static final float BAR_WIDTH = 2.5f;
    private static final float BAR_MIN_HEIGHT = 18f;
    private static final float IDLE_ALPHA = 0.3f;
    private static final float ACTIVE_ALPHA = 0.85f;
    private static final long ACTIVE_NANOS = 900_000_000L;

    private final Smooth barAlpha = new Smooth(10f);
    private float target;
    private float offset;
    private float maxScroll;
    private long lastScrollNanos;

    void update(float contentHeight, float viewHeight, float dt) {
        maxScroll = Math.max(0f, contentHeight - viewHeight);
        target = Math.max(0f, Math.min(target, maxScroll));
        offset = Easing.approach(offset, target, SPEED, dt);
        if (Math.abs(offset - target) < 0.05f) {
            offset = target;
        }
    }

    void scroll(double amount) {
        target = Math.max(0f, Math.min(maxScroll, target - (float) amount * STEP));
        lastScrollNanos = System.nanoTime();
    }

    float offset() {
        return offset;
    }

    boolean overflows() {
        return maxScroll > 0.5f;
    }

    void reset() {
        target = 0f;
        offset = 0f;
    }

    void drawBar(Rect view, boolean hovered, float dt) {
        boolean recent = System.nanoTime() - lastScrollNanos < ACTIVE_NANOS;
        float targetAlpha = !overflows() ? 0f : hovered || recent ? ACTIVE_ALPHA : IDLE_ALPHA;
        float alpha = barAlpha.update(targetAlpha, dt);
        if (alpha < 0.01f || maxScroll <= 0f) {
            return;
        }
        float content = view.height() + maxScroll;
        float barHeight = Math.max(BAR_MIN_HEIGHT, view.height() * view.height() / content);
        float barY = view.y() + offset / maxScroll * (view.height() - barHeight);
        Renderer2D.rect(view.right() - BAR_WIDTH - 1f, barY, BAR_WIDTH, barHeight, BAR_WIDTH * 0.5f,
                ColorUtil.multiplyAlpha(Theme.get().text(), alpha * 0.4f));
    }
}
