package cute.bytebuffer.adorance.gui.widget;

import cute.bytebuffer.adorance.gui.animation.Smooth;
import cute.bytebuffer.adorance.gui.animation.Spring;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.util.render.ColorUtil;
import cute.bytebuffer.adorance.util.render.RenderState;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.module.setting.MultiModeSetting;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class MultiModeWidget extends SettingWidget<MultiModeSetting<?>> {

    private static final float LABEL_ROW = 16f;
    private static final float BOTTOM_PADDING = 3f;
    private static final float CHIP_RADIUS = 5f;
    private static final float IDLE_ALPHA = 0.55f;
    private static final float POP_IMPULSE = -5f;

    private final Smooth[] selection;
    private final Spring[] pop;

    public MultiModeWidget(MultiModeSetting<?> setting) {
        super(setting);
        int count = setting.labels().size();
        this.selection = new Smooth[count];
        this.pop = new Spring[count];
        for (int index = 0; index < count; index++) {
            selection[index] = new Smooth(ANIMATION_SPEED, setting.isEnabled(index) ? 1f : 0f);
            pop[index] = new Spring(380f, 0.42f);
        }
    }

    private ChipLayout chips() {
        return ChipLayout.of(setting.labels(), Math.max(1f, width - PADDING_X * 2f));
    }

    @Override
    public float height() {
        return LABEL_ROW + chips().height() + BOTTOM_PADDING;
    }

    @Override
    protected void draw(double mouseX, double mouseY, float dt) {
        Theme theme = theme();
        drawLabel(y, LABEL_ROW);
        float originX = x + PADDING_X;
        float originY = y + LABEL_ROW;
        List<String> labels = setting.labels();
        for (ChipLayout.Chip chip : chips().chips()) {
            int index = chip.index();
            float active = selection[index].update(setting.isEnabled(index), dt);
            float scale = pop[index].update(1f, dt);
            float chipX = originX + chip.x();
            float chipY = originY + ChipLayout.rowY(chip.row());
            RenderState.pushTransform(chipX + chip.width() * 0.5f, chipY + ChipLayout.CHIP_HEIGHT * 0.5f, scale);
            try {
                int fill = ColorUtil.lerp(ColorUtil.multiplyAlpha(theme.control(), IDLE_ALPHA), theme.accent(), active);
                Renderer2D.rect(chipX, chipY, chip.width(), ChipLayout.CHIP_HEIGHT, CHIP_RADIUS, fill);
                float textY = Renderer2D.textY(chipY, ChipLayout.CHIP_HEIGHT, ChipLayout.TEXT_SIZE, ChipLayout.TEXT_WEIGHT);
                Renderer2D.textCentered(labels.get(index), chipX + chip.width() * 0.5f, textY, ChipLayout.TEXT_SIZE,
                        ChipLayout.TEXT_WEIGHT, ColorUtil.lerp(theme.text(), theme.onAccent(), active));
            } finally {
                RenderState.popTransform();
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        int index = chips().indexAt(mouseX - x - PADDING_X, mouseY - y - LABEL_ROW);
        if (index >= 0) {
            setting.toggle(index);
            pop[index].impulse(POP_IMPULSE);
        }
        return true;
    }
}
