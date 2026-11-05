package cute.bytebuffer.adorance.gui.widget;

import cute.bytebuffer.adorance.gui.animation.Smooth;
import cute.bytebuffer.adorance.gui.input.Focusable;
import cute.bytebuffer.adorance.gui.input.Interaction;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.util.render.ColorUtil;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.module.setting.StringSetting;
import cute.bytebuffer.adorance.util.Rect;
import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

public final class TextWidget extends SettingWidget<StringSetting> implements Focusable {

    private static final float FIELD_HEIGHT = 13f;
    private static final float FIELD_RADIUS = 5f;
    private static final float FIELD_INSET = 5f;
    private static final float MIN_FIELD_WIDTH = 64f;
    private static final float MAX_FIELD_WIDTH = 140f;
    private static final float FIELD_RATIO = 0.5f;
    private static final float CARET_WIDTH = 1f;
    private static final float BLINK_PERIOD = 1.05f;
    private static final float BLINK_HOLD = 0.5f;

    private final Smooth focus = new Smooth(ANIMATION_SPEED);
    private final Smooth scroll = new Smooth(20f);
    private int caret;
    private long lastEditNanos;

    public TextWidget(StringSetting setting) {
        super(setting);
    }

    @Override
    public float height() {
        return ROW_HEIGHT;
    }

    private Rect field() {
        float fieldWidth = Math.max(MIN_FIELD_WIDTH, Math.min(MAX_FIELD_WIDTH, width * FIELD_RATIO));
        return new Rect(right() - fieldWidth, y + (ROW_HEIGHT - FIELD_HEIGHT) * 0.5f, fieldWidth, FIELD_HEIGHT);
    }

    private static float measure(String text) {
        return Renderer2D.textWidth(text, LABEL_SIZE, LABEL_WEIGHT);
    }

    @Override
    protected void draw(double mouseX, double mouseY, float dt) {
        Theme theme = theme();
        boolean focused = Interaction.isFocused(this);
        float focusAmount = focus.update(focused, dt);
        drawLabel();

        Rect field = field();
        Renderer2D.rect(field.x(), field.y(), field.width(), field.height(), FIELD_RADIUS,
                ColorUtil.lerp(theme.control(), ColorUtil.lerp(theme.control(), theme.text(), 0.06f), hover.get()),
                ColorUtil.multiplyAlpha(theme.accent(), focusAmount), 1f);

        String value = setting.get();
        caret = Math.max(0, Math.min(caret, value.length()));
        float innerWidth = field.width() - FIELD_INSET * 2f;
        float caretOffset = measure(value.substring(0, caret));
        float targetScroll = focused ? Math.max(0f, caretOffset - innerWidth + CARET_WIDTH * 2f) : 0f;
        float offset = scroll.update(targetScroll, dt);
        float textX = field.x() + FIELD_INSET - offset;
        float textY = Renderer2D.textY(field.y(), field.height(), LABEL_SIZE, LABEL_WEIGHT);

        Renderer2D.pushClip(field.inset(FIELD_INSET - 1f, 0f));
        try {
            if (value.isEmpty() && !focused) {
                Renderer2D.text(setting.placeholder(), field.x() + FIELD_INSET, textY, LABEL_SIZE, LABEL_WEIGHT, theme.mutedText());
            } else {
                Renderer2D.text(value, textX, textY, LABEL_SIZE, LABEL_WEIGHT, theme.text());
            }
            if (focused) {
                float caretAlpha = caretAlpha();
                Renderer2D.rect(textX + caretOffset, field.y() + 3f, CARET_WIDTH, field.height() - 6f, 0.5f,
                        ColorUtil.multiplyAlpha(theme.text(), caretAlpha));
            }
        } finally {
            Renderer2D.popClip();
        }
    }

    private float caretAlpha() {
        float idle = (System.nanoTime() - lastEditNanos) / 1_000_000_000f;
        if (idle < BLINK_HOLD) {
            return 1f;
        }
        double phase = (idle - BLINK_HOLD) / BLINK_PERIOD * Math.PI * 2.0;
        return (float) (0.5 + 0.5 * Math.cos(phase));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        Interaction.focus(this);
        Rect field = field();
        caret = field.contains(mouseX, mouseY)
                ? caretAt(mouseX - (field.x() + FIELD_INSET - scroll.get()))
                : setting.get().length();
        touch();
        return true;
    }

    private int caretAt(double localX) {
        String value = setting.get();
        for (int index = 0; index < value.length(); index++) {
            float before = measure(value.substring(0, index));
            float after = measure(value.substring(0, index + 1));
            if (localX < (before + after) * 0.5f) {
                return index;
            }
        }
        return value.length();
    }

    private void touch() {
        lastEditNanos = System.nanoTime();
    }

    @Override
    public boolean charTyped(String characters) {
        insert(characters);
        return true;
    }

    @Override
    public boolean keyPressed(int key) {
        String value = setting.get();
        boolean control = Interaction.isControlDown();
        touch();
        switch (key) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (caret > 0) {
                    int from = control ? wordStart(value, caret) : caret - 1;
                    setting.set(value.substring(0, from) + value.substring(caret));
                    caret = from;
                }
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (caret < value.length()) {
                    int to = control ? wordEnd(value, caret) : caret + 1;
                    setting.set(value.substring(0, caret) + value.substring(to));
                }
            }
            case GLFW.GLFW_KEY_LEFT -> caret = control ? wordStart(value, caret) : Math.max(0, caret - 1);
            case GLFW.GLFW_KEY_RIGHT -> caret = control ? wordEnd(value, caret) : Math.min(value.length(), caret + 1);
            case GLFW.GLFW_KEY_HOME -> caret = 0;
            case GLFW.GLFW_KEY_END -> caret = value.length();
            case GLFW.GLFW_KEY_V -> {
                if (control) {
                    insert(MinecraftClient.getInstance().keyboard.getClipboard());
                }
            }
            case GLFW.GLFW_KEY_C -> {
                if (control) {
                    MinecraftClient.getInstance().keyboard.setClipboard(value);
                }
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_ESCAPE -> Interaction.clearFocus();
            default -> {
                return false;
            }
        }
        return true;
    }

    private void insert(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        StringBuilder accepted = new StringBuilder(text.length());
        text.codePoints().filter(codepoint -> codepoint >= 32 && codepoint != 127).forEach(accepted::appendCodePoint);
        String value = setting.get();
        int room = setting.maxLength() - value.length();
        if (room <= 0 || accepted.isEmpty()) {
            return;
        }
        String inserted = accepted.length() > room ? accepted.substring(0, room) : accepted.toString();
        setting.set(value.substring(0, caret) + inserted + value.substring(caret));
        caret += inserted.length();
        touch();
    }

    private static int wordStart(String text, int from) {
        int index = from;
        while (index > 0 && Character.isWhitespace(text.charAt(index - 1))) {
            index--;
        }
        while (index > 0 && !Character.isWhitespace(text.charAt(index - 1))) {
            index--;
        }
        return index;
    }

    private static int wordEnd(String text, int from) {
        int index = from;
        while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        while (index < text.length() && !Character.isWhitespace(text.charAt(index))) {
            index++;
        }
        return index;
    }
}
