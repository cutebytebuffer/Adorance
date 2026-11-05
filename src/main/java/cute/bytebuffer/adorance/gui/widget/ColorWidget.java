package cute.bytebuffer.adorance.gui.widget;

import cute.bytebuffer.adorance.gui.animation.Easing;
import cute.bytebuffer.adorance.gui.animation.Spring;
import cute.bytebuffer.adorance.gui.input.Draggable;
import cute.bytebuffer.adorance.gui.input.Interaction;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.util.render.ColorUtil;
import cute.bytebuffer.adorance.util.render.Opacity;
import cute.bytebuffer.adorance.util.render.RenderState;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.module.setting.ColorSetting;
import cute.bytebuffer.adorance.util.Rect;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

public final class ColorWidget extends SettingWidget<ColorSetting> {

    private static final float SWATCH_WIDTH = 18f;
    private static final float SWATCH_HEIGHT = 10f;
    private static final float FIELD_HEIGHT = 46f;
    private static final float BAR_HEIGHT = 7f;
    private static final float SECTION_GAP = 5f;
    private static final float PICKER_TOP = 2f;
    private static final float PICKER_HEIGHT = PICKER_TOP + FIELD_HEIGHT + SECTION_GAP + BAR_HEIGHT + SECTION_GAP + BAR_HEIGHT + 6f;
    private static final float CURSOR_RADIUS = 3.5f;
    private static final int OPAQUE = 0xFF;

    private final Spring expansion = new Spring(260f, 0.86f);
    private final Draggable fieldDrag = this::dragField;
    private final Draggable hueDrag = this::dragHue;
    private final Draggable alphaDrag = this::dragAlpha;
    private boolean open;
    private float hue;
    private float saturation;
    private float brightness;
    private int alpha;
    private int synced;

    public ColorWidget(ColorSetting setting) {
        super(setting);
        this.synced = ~setting.argb();
        sync();
    }

    @Override
    public float height() {
        return ROW_HEIGHT + (open ? PICKER_HEIGHT : 0f);
    }

    @Override
    public float layoutHeight() {
        return (ROW_HEIGHT + PICKER_HEIGHT * Easing.clamp(expansion.get())) * visibility();
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        expansion.update(open ? 1f : 0f, dt);
        sync();
    }

    private void sync() {
        int argb = setting.argb();
        if (argb == synced) {
            return;
        }
        float[] hsv = ColorUtil.toHsv(argb);
        if (hsv[1] > 0.001f && hsv[2] > 0.001f) {
            hue = hsv[0];
        }
        saturation = hsv[1];
        brightness = hsv[2];
        alpha = ColorUtil.alpha(argb);
        synced = argb;
    }

    private void apply() {
        setting.set(ColorUtil.fromHsv(hue, saturation, brightness, alpha));
        synced = setting.argb();
    }

    private Rect field() {
        return new Rect(x + PADDING_X, y + ROW_HEIGHT + PICKER_TOP, width - PADDING_X * 2f, FIELD_HEIGHT);
    }

    private Rect hueBar() {
        Rect field = field();
        return new Rect(field.x(), field.bottom() + SECTION_GAP, field.width(), BAR_HEIGHT);
    }

    private Rect alphaBar() {
        Rect hueBar = hueBar();
        return new Rect(hueBar.x(), hueBar.bottom() + SECTION_GAP, hueBar.width(), BAR_HEIGHT);
    }

    @Override
    protected void draw(double mouseX, double mouseY, float dt) {
        Theme theme = theme();
        drawLabel();
        int argb = setting.argb();
        float swatchX = right() - SWATCH_WIDTH;
        float swatchY = y + (ROW_HEIGHT - SWATCH_HEIGHT) * 0.5f;
        Renderer2D.rect(swatchX, swatchY, SWATCH_WIDTH, SWATCH_HEIGHT, 3f, theme.control());
        Renderer2D.rect(swatchX, swatchY, SWATCH_WIDTH, SWATCH_HEIGHT, 3f, argb, theme.outline(), 1f);

        String hex = String.format(Locale.ROOT, "#%06X", argb & 0x00FFFFFF);
        float hexWidth = Renderer2D.textWidth(hex, VALUE_SIZE, VALUE_WEIGHT);
        Renderer2D.text(hex, swatchX - 5f - hexWidth, Renderer2D.textY(y, ROW_HEIGHT, VALUE_SIZE, VALUE_WEIGHT),
                VALUE_SIZE, VALUE_WEIGHT, ColorUtil.lerp(theme.mutedText(), theme.text(), hover.get()));

        float reveal = Easing.clamp(expansion.get());
        if (reveal <= 0.01f) {
            return;
        }
        RenderState.pushScissor(x, y + ROW_HEIGHT, width, PICKER_HEIGHT * reveal);
        Opacity.push(reveal);
        try {
            drawPicker(theme);
        } finally {
            Opacity.pop();
            RenderState.popScissor();
        }
    }

    private void drawPicker(Theme theme) {
        Rect field = field();
        int pureHue = ColorUtil.fromHsv(hue, 1f, 1f, OPAQUE);
        Renderer2D.gradient(field.x(), field.y(), field.width(), field.height(), 5f, 0xFFFFFFFF, pureHue, 1f, 0f);
        Renderer2D.gradient(field.x(), field.y(), field.width(), field.height(), 5f, 0x00000000, 0xFF000000, 0f, 1f);
        Renderer2D.outline(field, 5f, theme.outline(), 1f);
        float cursorX = field.x() + saturation * field.width();
        float cursorY = field.y() + (1f - brightness) * field.height();
        Renderer2D.circle(cursorX, cursorY, CURSOR_RADIUS, ColorUtil.withAlpha(setting.argb(), OPAQUE), 0xFFFFFFFF, 1.2f);

        Rect hueBar = hueBar();
        Renderer2D.hueBar(hueBar.x(), hueBar.y(), hueBar.width(), hueBar.height(), BAR_HEIGHT * 0.5f);
        drawBarCursor(hueBar, hue, pureHue);

        Rect alphaBar = alphaBar();
        int rgb = setting.rgb();
        Renderer2D.rect(alphaBar, BAR_HEIGHT * 0.5f, theme.control());
        Renderer2D.gradient(alphaBar.x(), alphaBar.y(), alphaBar.width(), alphaBar.height(), BAR_HEIGHT * 0.5f,
                rgb, 0xFF000000 | rgb, 1f, 0f);
        drawBarCursor(alphaBar, alpha / 255f, ColorUtil.withAlpha(rgb, OPAQUE));
    }

    private static void drawBarCursor(Rect bar, float position, int color) {
        float centerX = bar.x() + position * bar.width();
        Renderer2D.circle(centerX, bar.centerY(), bar.height() * 0.5f + 1.5f, color, 0xFFFFFFFF, 1.2f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (mouseY <= y + ROW_HEIGHT) {
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                open = !open;
            }
            return true;
        }
        if (!open || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return true;
        }
        if (field().inset(-2f).contains(mouseX, mouseY)) {
            Interaction.beginDrag(fieldDrag, mouseX, mouseY);
        } else if (hueBar().inset(-2f).contains(mouseX, mouseY)) {
            Interaction.beginDrag(hueDrag, mouseX, mouseY);
        } else if (alphaBar().inset(-2f).contains(mouseX, mouseY)) {
            Interaction.beginDrag(alphaDrag, mouseX, mouseY);
        }
        return true;
    }

    private void dragField(double mouseX, double mouseY) {
        Rect field = field();
        saturation = Easing.clamp((float) ((mouseX - field.x()) / field.width()));
        brightness = 1f - Easing.clamp((float) ((mouseY - field.y()) / field.height()));
        apply();
    }

    private void dragHue(double mouseX, double mouseY) {
        Rect bar = hueBar();
        hue = Math.min(0.9999f, Easing.clamp((float) ((mouseX - bar.x()) / bar.width())));
        apply();
    }

    private void dragAlpha(double mouseX, double mouseY) {
        Rect bar = alphaBar();
        alpha = Math.round(Easing.clamp((float) ((mouseX - bar.x()) / bar.width())) * 255f);
        apply();
    }
}
