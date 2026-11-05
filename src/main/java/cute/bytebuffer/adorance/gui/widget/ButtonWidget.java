package cute.bytebuffer.adorance.gui.widget;

import cute.bytebuffer.adorance.gui.animation.Spring;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.util.render.ColorUtil;
import cute.bytebuffer.adorance.util.render.RenderState;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import org.lwjgl.glfw.GLFW;

public final class ButtonWidget extends Widget {

    private static final float HEIGHT = 20f;
    private static final float BUTTON_HEIGHT = 15f;
    private static final float RADIUS = 6f;
    private static final float IDLE_ALPHA = 0.65f;
    private static final float PRESS_IMPULSE = -3.5f;

    private final String label;
    private final Runnable action;
    private final Spring press = new Spring(420f, 0.5f);

    public ButtonWidget(String label, Runnable action) {
        this.label = label;
        this.action = action;
    }

    @Override
    public float height() {
        return HEIGHT;
    }

    @Override
    protected void draw(double mouseX, double mouseY, float dt) {
        Theme theme = theme();
        float scale = press.update(1f, dt);
        float buttonX = x + PADDING_X;
        float buttonY = y + (HEIGHT - BUTTON_HEIGHT) * 0.5f;
        float buttonWidth = width - PADDING_X * 2f;
        RenderState.pushTransform(buttonX + buttonWidth * 0.5f, buttonY + BUTTON_HEIGHT * 0.5f, scale);
        try {
            int fill = ColorUtil.multiplyAlpha(theme.control(), IDLE_ALPHA + (1f - IDLE_ALPHA) * hover.get());
            Renderer2D.rect(buttonX, buttonY, buttonWidth, BUTTON_HEIGHT, RADIUS, fill,
                    ColorUtil.multiplyAlpha(theme.accent(), hover.get() * 0.6f), 1f);
            float textY = Renderer2D.textY(buttonY, BUTTON_HEIGHT, VALUE_SIZE, VALUE_WEIGHT);
            Renderer2D.textCentered(label, buttonX + buttonWidth * 0.5f, textY, VALUE_SIZE, VALUE_WEIGHT, theme.text());
        } finally {
            RenderState.popTransform();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        press.impulse(PRESS_IMPULSE);
        action.run();
        return true;
    }
}
