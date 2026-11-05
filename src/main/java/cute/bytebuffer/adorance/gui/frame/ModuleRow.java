package cute.bytebuffer.adorance.gui.frame;

import cute.bytebuffer.adorance.gui.animation.Reveal;
import cute.bytebuffer.adorance.gui.animation.Smooth;
import cute.bytebuffer.adorance.gui.animation.Spring;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.module.Module;
import cute.bytebuffer.adorance.util.render.ColorUtil;
import cute.bytebuffer.adorance.util.render.Opacity;
import cute.bytebuffer.adorance.util.render.RenderState;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.util.render.font.FontWeight;
import cute.bytebuffer.adorance.util.Rect;

import java.util.Locale;

final class ModuleRow {

    static final float HEIGHT = 20f;

    private static final float RADIUS = 7f;
    private static final float TEXT_SIZE = 8.5f;
    private static final FontWeight TEXT_WEIGHT = FontWeight.MEDIUM;
    private static final float TEXT_INSET = 9f;
    private static final float SPEED = 14f;
    private static final float IDLE_ALPHA = 0.28f;
    private static final float HOVER_ALPHA = 0.32f;
    private static final float CHEVRON_INSET = 11f;
    private static final float CHEVRON_HALF_HEIGHT = 2.8f;
    private static final float CHEVRON_HALF_WIDTH = 1.5f;
    private static final float CHEVRON_NUDGE = 2f;
    private static final float PRESS_IMPULSE = -1.2f;

    private final Module module;
    private final Reveal reveal = new Reveal(0.32f, 0.92f);
    private final Smooth hover = new Smooth(SPEED);
    private final Smooth enabled;
    private final Smooth expanded = new Smooth(SPEED);
    private final Spring press = new Spring(520f, 0.55f);

    ModuleRow(Module module) {
        this.module = module;
        this.enabled = new Smooth(SPEED, module.isEnabled() ? 1f : 0f);
    }

    Module module() {
        return module;
    }

    Reveal reveal() {
        return reveal;
    }

    boolean matches(String query) {
        if (query.isBlank()) {
            return true;
        }
        String needle = query.toLowerCase(Locale.ROOT).trim();
        return module.name().toLowerCase(Locale.ROOT).contains(needle)
                || module.description().toLowerCase(Locale.ROOT).contains(needle);
    }

    void press() {
        press.impulse(PRESS_IMPULSE);
    }

    void render(Rect bounds, boolean isExpanded, double mouseX, double mouseY, float dt) {
        Theme theme = theme();
        float hovered = hover.update(bounds.contains(mouseX, mouseY), dt);
        float active = enabled.update(module.isEnabled(), dt);
        float open = expanded.update(isExpanded, dt);
        float scale = reveal.scale() * press.update(1f, dt);

        RenderState.pushTransform(bounds.centerX(), bounds.centerY(), scale);
        Opacity.push(reveal.alpha());
        try {
            int idle = ColorUtil.multiplyAlpha(theme.control(), IDLE_ALPHA + HOVER_ALPHA * hovered);
            int fill = ColorUtil.lerp(idle, theme.accent(), active);
            int ring = ColorUtil.multiplyAlpha(ColorUtil.lerp(theme.accent(), theme.onAccent(), active), open * 0.75f);
            Renderer2D.rect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), RADIUS, fill, ring, 1f);

            int textColor = ColorUtil.lerp(theme.text(), theme.onAccent(), active);
            float textY = Renderer2D.textY(bounds.y(), bounds.height(), TEXT_SIZE, TEXT_WEIGHT);
            String name = Renderer2D.ellipsize(module.name(), bounds.width() - TEXT_INSET - CHEVRON_INSET * 2f, TEXT_SIZE, TEXT_WEIGHT);
            Renderer2D.text(name, bounds.x() + TEXT_INSET, textY, TEXT_SIZE, TEXT_WEIGHT, textColor);

            drawChevron(bounds, theme, active, Math.max(hovered * 0.7f, open), open);
        } finally {
            Opacity.pop();
            RenderState.popTransform();
        }
    }

    private static void drawChevron(Rect bounds, Theme theme, float active, float visibility, float open) {
        if (visibility <= 0.01f) {
            return;
        }
        int color = ColorUtil.multiplyAlpha(ColorUtil.lerp(theme.mutedText(), theme.onAccent(), active), visibility);
        float centerX = bounds.right() - CHEVRON_INSET + CHEVRON_NUDGE * open;
        float centerY = bounds.centerY();
        Renderer2D.line(centerX - CHEVRON_HALF_WIDTH, centerY - CHEVRON_HALF_HEIGHT, centerX + CHEVRON_HALF_WIDTH, centerY, 1.2f, color);
        Renderer2D.line(centerX + CHEVRON_HALF_WIDTH, centerY, centerX - CHEVRON_HALF_WIDTH, centerY + CHEVRON_HALF_HEIGHT, 1.2f, color);
    }

    private static Theme theme() {
        return Theme.get();
    }
}
