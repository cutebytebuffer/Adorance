package cute.bytebuffer.adorance.gui.widget;

import cute.bytebuffer.adorance.gui.animation.Smooth;
import cute.bytebuffer.adorance.gui.input.Focusable;
import cute.bytebuffer.adorance.gui.input.Interaction;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.util.render.ColorUtil;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.module.setting.KeybindSetting;
import org.lwjgl.glfw.GLFW;

public final class KeybindWidget extends SettingWidget<KeybindSetting> implements Focusable {

    private static final String LISTENING = "Press a key";
    private static final float CHIP_HEIGHT = 13f;
    private static final float CHIP_PADDING = 7f;
    private static final float MIN_CHIP_WIDTH = 36f;
    private static final float PULSE_SPEED = 5f;

    private final Smooth listening = new Smooth(ANIMATION_SPEED);
    private final Smooth chipWidth = new Smooth(18f);

    public KeybindWidget(KeybindSetting setting) {
        super(setting);
    }

    @Override
    public float height() {
        return ROW_HEIGHT;
    }

    @Override
    protected void draw(double mouseX, double mouseY, float dt) {
        Theme theme = theme();
        boolean active = Interaction.isFocused(this);
        float amount = listening.update(active, dt);
        drawLabel();

        String label = active ? LISTENING : setting.keyName();
        float targetWidth = Math.max(MIN_CHIP_WIDTH, Renderer2D.textWidth(label, VALUE_SIZE, VALUE_WEIGHT) + CHIP_PADDING * 2f);
        if (chipWidth.get() <= 0f) {
            chipWidth.snap(targetWidth);
        }
        float shownWidth = chipWidth.update(targetWidth, dt);
        float chipX = right() - shownWidth;
        float chipY = y + (ROW_HEIGHT - CHIP_HEIGHT) * 0.5f;
        float pulse = active ? 0.75f + 0.25f * (float) Math.sin(System.nanoTime() / 1_000_000_000.0 * PULSE_SPEED) : 1f;
        int fill = ColorUtil.lerp(theme.control(), ColorUtil.multiplyAlpha(theme.accent(), pulse), amount);
        Renderer2D.rect(chipX, chipY, shownWidth, CHIP_HEIGHT, CHIP_HEIGHT * 0.5f, fill);
        int textColor = setting.isBound() || active ? theme.text() : theme.mutedText();
        float textY = Renderer2D.textY(chipY, CHIP_HEIGHT, VALUE_SIZE, VALUE_WEIGHT);
        Renderer2D.textCentered(label, chipX + shownWidth * 0.5f, textY, VALUE_SIZE, VALUE_WEIGHT,
                ColorUtil.lerp(textColor, theme.onAccent(), amount));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            setting.clear();
            Interaction.clearFocus();
            return true;
        }
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        Interaction.focus(Interaction.isFocused(this) ? null : this);
        return true;
    }

    @Override
    public boolean keyPressed(int key) {
        switch (key) {
            case GLFW.GLFW_KEY_ESCAPE -> {
            }
            case GLFW.GLFW_KEY_BACKSPACE, GLFW.GLFW_KEY_DELETE -> setting.clear();
            default -> setting.set(key);
        }
        Interaction.clearFocus();
        return true;
    }

    @Override
    public boolean charTyped(String characters) {
        return true;
    }
}
