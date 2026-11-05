package cute.bytebuffer.adorance.gui.widget;

import cute.bytebuffer.adorance.gui.animation.Smooth;
import cute.bytebuffer.adorance.gui.input.Draggable;
import cute.bytebuffer.adorance.gui.input.Interaction;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.util.render.ColorUtil;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.module.setting.NumberSetting;
import org.lwjgl.glfw.GLFW;

public final class SliderWidget extends SettingWidget<NumberSetting> implements Draggable {

    private static final float LABEL_ROW = 15f;
    private static final float TRACK_OFFSET = 18f;
    private static final float TRACK_HEIGHT = 3f;
    private static final float HEIGHT = 26f;
    private static final float KNOB_RADIUS = 3.5f;
    private static final float KNOB_ACTIVE_GROWTH = 1.2f;
    private static final float VALUE_SPEED = 18f;

    private final Smooth progress;
    private final Smooth active = new Smooth(ANIMATION_SPEED);

    public SliderWidget(NumberSetting setting) {
        super(setting);
        this.progress = new Smooth(VALUE_SPEED, (float) setting.progress());
    }

    @Override
    public float height() {
        return HEIGHT;
    }

    private float trackX() {
        return x + PADDING_X;
    }

    private float trackWidth() {
        return Math.max(1f, width - PADDING_X * 2f);
    }

    @Override
    protected void draw(double mouseX, double mouseY, float dt) {
        Theme theme = theme();
        boolean dragging = Interaction.isDragging(this);
        float shown = progress.update((float) setting.progress(), dt);
        float emphasis = active.update(Math.max(hover.get(), dragging ? 1f : 0f), dt);
        drawLabel(y, LABEL_ROW);

        double displayed = setting.min() + shown * (setting.max() - setting.min());
        String value = setting.format(dragging ? setting.get() : displayed);
        float valueWidth = Renderer2D.textWidth(value, VALUE_SIZE, VALUE_WEIGHT);
        float valueY = Renderer2D.textY(y, LABEL_ROW, VALUE_SIZE, VALUE_WEIGHT);
        Renderer2D.text(value, right() - valueWidth, valueY, VALUE_SIZE, VALUE_WEIGHT,
                ColorUtil.lerp(theme.mutedText(), theme.text(), emphasis));

        float trackY = y + TRACK_OFFSET;
        float trackWidth = trackWidth();
        Renderer2D.rect(trackX(), trackY, trackWidth, TRACK_HEIGHT, TRACK_HEIGHT * 0.5f, theme.control());
        float filled = trackWidth * shown;
        if (filled > 0.5f) {
            Renderer2D.rect(trackX(), trackY, filled, TRACK_HEIGHT, TRACK_HEIGHT * 0.5f, theme.accent());
        }
        float knobRadius = KNOB_RADIUS + KNOB_ACTIVE_GROWTH * emphasis;
        Renderer2D.circle(trackX() + filled, trackY + TRACK_HEIGHT * 0.5f, knobRadius, theme.text(),
                ColorUtil.multiplyAlpha(theme.accent(), emphasis), 1.2f);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        Interaction.beginDrag(this, mouseX, mouseY);
        return true;
    }

    @Override
    public void drag(double mouseX, double mouseY) {
        setting.setProgress((mouseX - trackX()) / trackWidth());
    }
}
