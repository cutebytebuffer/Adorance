package cute.bytebuffer.adorance.gui.frame;

import cute.bytebuffer.adorance.Adorance;
import cute.bytebuffer.adorance.gui.animation.Easing;
import cute.bytebuffer.adorance.gui.animation.Smooth;
import cute.bytebuffer.adorance.gui.animation.Spring;
import cute.bytebuffer.adorance.gui.input.Focusable;
import cute.bytebuffer.adorance.gui.input.Interaction;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.util.render.ColorUtil;
import cute.bytebuffer.adorance.util.render.Opacity;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.util.render.font.FontWeight;
import cute.bytebuffer.adorance.util.Rect;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

final class SearchField implements Focusable {

    private static final Identifier ICON = Adorance.id("icons/search.png");
    private static final String PLACEHOLDER = "Search modules";
    private static final float HEIGHT = 16f;
    private static final float COLLAPSED_WIDTH = 100f;
    private static final float EXPANDED_WIDTH = 168f;
    private static final float ICON_SIZE = 9f;
    private static final float ICON_INSET = 7f;
    private static final float TEXT_GAP = 5f;
    private static final float TEXT_SIZE = 7.5f;
    private static final FontWeight TEXT_WEIGHT = FontWeight.REGULAR;
    private static final float CLEAR_RADIUS = 4.5f;
    private static final float CLEAR_INSET = 8f;
    private static final float DISABLED_ALPHA = 0.4f;
    private static final float BLINK_PERIOD = 1.05f;
    private static final float BLINK_HOLD = 0.5f;
    private static final int MAX_LENGTH = 48;
    private static final float SPEED = 14f;

    private final Consumer<String> listener;
    private final Spring width = new Spring(300f, 0.78f);
    private final Smooth focus = new Smooth(SPEED);
    private final Smooth hover = new Smooth(SPEED);
    private final Smooth clear = new Smooth(16f);
    private final Smooth enabled = new Smooth(10f, 1f);
    private String query = "";
    private boolean active = true;
    private long lastEditNanos;
    private Rect bounds = new Rect(0f, 0f, 0f, 0f);

    SearchField(Consumer<String> listener) {
        this.listener = listener;
    }

    String query() {
        return query;
    }

    boolean isActive() {
        return active;
    }

    void setActive(boolean active) {
        this.active = active;
        if (!active && Interaction.isFocused(this)) {
            Interaction.clearFocus();
        }
    }

    boolean contains(double mouseX, double mouseY) {
        return bounds.contains(mouseX, mouseY);
    }

    void render(float rightX, float centerY, double mouseX, double mouseY, float dt) {
        Theme theme = Theme.get();
        boolean focused = Interaction.isFocused(this);
        boolean open = active && (focused || !query.isEmpty());
        float shownWidth = width.update(open ? EXPANDED_WIDTH : COLLAPSED_WIDTH, dt);
        bounds = new Rect(rightX - shownWidth, centerY - HEIGHT * 0.5f, shownWidth, HEIGHT);
        float focusAmount = focus.update(focused, dt);
        float hoverAmount = hover.update(active && bounds.contains(mouseX, mouseY), dt);
        float clearAmount = clear.update(active && !query.isEmpty(), dt);

        Opacity.push(enabled.update(active ? 1f : DISABLED_ALPHA, dt));
        try {
            int fill = ColorUtil.lerp(ColorUtil.multiplyAlpha(theme.control(), 0.7f), theme.control(), Math.max(hoverAmount, focusAmount));
            Renderer2D.rect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), HEIGHT * 0.5f, fill,
                    ColorUtil.multiplyAlpha(theme.accent(), focusAmount * 0.8f), 1f);
            Renderer2D.icon(ICON, bounds.x() + ICON_INSET, centerY - ICON_SIZE * 0.5f, ICON_SIZE,
                    ColorUtil.lerp(theme.mutedText(), theme.text(), focusAmount));
            drawText(theme, focused, clearAmount);
            drawClearButton(theme, clearAmount, mouseX, mouseY);
        } finally {
            Opacity.pop();
        }
    }

    private void drawText(Theme theme, boolean focused, float clearAmount) {
        float textLeft = bounds.x() + ICON_INSET + ICON_SIZE + TEXT_GAP;
        float textRight = bounds.right() - ICON_INSET - (CLEAR_RADIUS * 2f + 2f) * clearAmount;
        float available = Math.max(0f, textRight - textLeft);
        float textY = Renderer2D.textY(bounds.y(), bounds.height(), TEXT_SIZE, TEXT_WEIGHT);
        float queryWidth = Renderer2D.textWidth(query, TEXT_SIZE, TEXT_WEIGHT);
        float textX = textLeft - Math.max(0f, queryWidth - available + 2f);

        Renderer2D.pushClip(new Rect(textLeft - 1f, bounds.y(), available + 2f, bounds.height()));
        try {
            if (query.isEmpty() && !focused) {
                Renderer2D.text(PLACEHOLDER, textLeft, textY, TEXT_SIZE, TEXT_WEIGHT, theme.mutedText());
            } else {
                Renderer2D.text(query, textX, textY, TEXT_SIZE, TEXT_WEIGHT, theme.text());
            }
            if (focused) {
                Renderer2D.rect(textX + queryWidth + 0.5f, bounds.y() + 4f, 1f, bounds.height() - 8f, 0.5f,
                        ColorUtil.multiplyAlpha(theme.text(), caretAlpha()));
            }
        } finally {
            Renderer2D.popClip();
        }
    }

    private void drawClearButton(Theme theme, float amount, double mouseX, double mouseY) {
        if (amount <= 0.01f) {
            return;
        }
        float radius = CLEAR_RADIUS * Easing.outCubic(amount);
        float centerX = bounds.right() - CLEAR_INSET;
        float centerY = bounds.centerY();
        boolean hovered = clearHit(mouseX, mouseY);
        Renderer2D.circle(centerX, centerY, radius, ColorUtil.multiplyAlpha(theme.text(), (hovered ? 0.26f : 0.14f) * amount));
        float arm = radius * 0.45f;
        int color = ColorUtil.multiplyAlpha(theme.text(), amount);
        Renderer2D.line(centerX - arm, centerY - arm, centerX + arm, centerY + arm, 1f, color);
        Renderer2D.line(centerX - arm, centerY + arm, centerX + arm, centerY - arm, 1f, color);
    }

    private boolean clearHit(double mouseX, double mouseY) {
        double dx = mouseX - (bounds.right() - CLEAR_INSET);
        double dy = mouseY - bounds.centerY();
        return dx * dx + dy * dy <= (CLEAR_RADIUS + 1.5f) * (CLEAR_RADIUS + 1.5f);
    }

    private float caretAlpha() {
        float idle = (System.nanoTime() - lastEditNanos) / 1_000_000_000f;
        if (idle < BLINK_HOLD) {
            return 1f;
        }
        return (float) (0.5 + 0.5 * Math.cos((idle - BLINK_HOLD) / BLINK_PERIOD * Math.PI * 2.0));
    }

    boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!bounds.contains(mouseX, mouseY)) {
            return false;
        }
        if (active && button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (!query.isEmpty() && clear.get() > 0.5f && clearHit(mouseX, mouseY)) {
                setQuery("");
            }
            focus();
        }
        return true;
    }

    void focus() {
        if (active) {
            Interaction.focus(this);
            lastEditNanos = System.nanoTime();
        }
    }

    private void setQuery(String next) {
        if (next.equals(query)) {
            return;
        }
        query = next;
        lastEditNanos = System.nanoTime();
        listener.accept(query);
    }

    @Override
    public boolean keyPressed(int key) {
        switch (key) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (Interaction.isControlDown()) {
                    setQuery("");
                } else if (!query.isEmpty()) {
                    setQuery(query.substring(0, query.offsetByCodePoints(query.length(), -1)));
                }
            }
            case GLFW.GLFW_KEY_ESCAPE, GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> Interaction.clearFocus();
            default -> {
            }
        }
        return true;
    }

    @Override
    public boolean charTyped(String characters) {
        StringBuilder accepted = new StringBuilder(query);
        characters.codePoints()
                .filter(codepoint -> codepoint >= 32 && codepoint != 127)
                .forEach(accepted::appendCodePoint);
        String next = accepted.length() > MAX_LENGTH ? accepted.substring(0, MAX_LENGTH) : accepted.toString();
        setQuery(next);
        return true;
    }
}
