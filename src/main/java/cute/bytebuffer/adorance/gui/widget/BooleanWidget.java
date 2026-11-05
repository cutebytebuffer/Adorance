package cute.bytebuffer.adorance.gui.widget;

import cute.bytebuffer.adorance.gui.animation.Smooth;
import cute.bytebuffer.adorance.gui.animation.Spring;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.util.render.ColorUtil;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.module.setting.BooleanSetting;
import org.lwjgl.glfw.GLFW;

public final class BooleanWidget extends SettingWidget<BooleanSetting> {

    private static final float TRACK_WIDTH = 20f;
    private static final float TRACK_HEIGHT = 11f;
    private static final float KNOB_INSET = 2f;
    private static final float KNOB_RADIUS = TRACK_HEIGHT * 0.5f - KNOB_INSET;
    private static final float HOVER_GROWTH = 0.5f;

    private final Spring knob = new Spring(420f, 0.62f);
    private final Smooth fill = new Smooth(ANIMATION_SPEED);

    public BooleanWidget(BooleanSetting setting) {
        super(setting);
    }

    @Override
    public float height() {
        return ROW_HEIGHT;
    }

    @Override
    protected void draw(double mouseX, double mouseY, float dt) {
        Theme theme = theme();
        drawLabel();
        float position = knob.update(setting.get() ? 1f : 0f, dt);
        float active = fill.update(setting.get(), dt);
        float trackX = right() - TRACK_WIDTH;
        float trackY = y + (ROW_HEIGHT - TRACK_HEIGHT) * 0.5f;
        Renderer2D.rect(trackX, trackY, TRACK_WIDTH, TRACK_HEIGHT, TRACK_HEIGHT * 0.5f,
                ColorUtil.lerp(theme.control(), theme.accent(), active));
        float travel = TRACK_WIDTH - (KNOB_INSET + KNOB_RADIUS) * 2f;
        float knobX = trackX + KNOB_INSET + KNOB_RADIUS + travel * position;
        Renderer2D.circle(knobX, trackY + TRACK_HEIGHT * 0.5f, KNOB_RADIUS + HOVER_GROWTH * hover.get(),
                ColorUtil.lerp(theme.mutedText(), theme.onAccent(), active));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        setting.toggle();
        return true;
    }
}
