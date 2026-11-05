package cute.bytebuffer.adorance.gui.widget;

import cute.bytebuffer.adorance.gui.animation.Spring;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.util.render.ColorUtil;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.module.setting.ModeSetting;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class ModeWidget extends SettingWidget<ModeSetting<?>> {

    private static final float LABEL_ROW = 16f;
    private static final float BOTTOM_PADDING = 3f;
    private static final float LEADING_STIFFNESS = 520f;
    private static final float TRAILING_STIFFNESS = 210f;
    private static final float CHIP_RADIUS = 5f;
    private static final float TRACK_ALPHA = 0.55f;

    private final Spring pillLeft = new Spring(LEADING_STIFFNESS, 0.82f);
    private final Spring pillRight = new Spring(LEADING_STIFFNESS, 0.82f);
    private final Spring pillTop = new Spring(360f, 0.8f);

    public ModeWidget(ModeSetting<?> setting) {
        super(setting);
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
        ChipLayout layout = chips();
        float originX = x + PADDING_X;
        float originY = y + LABEL_ROW;

        int rowStart = 0;
        List<ChipLayout.Chip> all = layout.chips();
        for (int i = 1; i <= all.size(); i++) {
            if (i == all.size() || all.get(i).row() != all.get(rowStart).row()) {
                ChipLayout.Chip first = all.get(rowStart);
                ChipLayout.Chip last = all.get(i - 1);
                Renderer2D.rect(originX + first.x(), originY + ChipLayout.rowY(first.row()), last.right() - first.x(),
                        ChipLayout.CHIP_HEIGHT, CHIP_RADIUS, ColorUtil.multiplyAlpha(theme.control(), TRACK_ALPHA));
                rowStart = i;
            }
        }

        ChipLayout.Chip selected = layout.chip(setting.index());
        float currentLeft = pillLeft.get();
        boolean movingRight = selected.x() > currentLeft + 0.01f;
        boolean movingLeft = selected.x() < currentLeft - 0.01f;
        float left = pillLeft.update(selected.x(), dt, movingLeft ? LEADING_STIFFNESS : movingRight ? TRAILING_STIFFNESS : LEADING_STIFFNESS);
        float right = pillRight.update(selected.right(), dt, movingRight ? LEADING_STIFFNESS : movingLeft ? TRAILING_STIFFNESS : LEADING_STIFFNESS);
        float top = pillTop.update(ChipLayout.rowY(selected.row()), dt);
        float pillWidth = Math.max(2f, right - left);
        Renderer2D.rect(originX + left, originY + top, pillWidth, ChipLayout.CHIP_HEIGHT, CHIP_RADIUS, theme.accent());

        List<String> labels = setting.labels();
        for (ChipLayout.Chip chip : all) {
            float chipTop = ChipLayout.rowY(chip.row());
            float overlap = Math.max(0f, Math.min(chip.right(), left + pillWidth) - Math.max(chip.x(), left)) / chip.width();
            float verticalOverlap = Math.max(0f, 1f - Math.abs(chipTop - top) / ChipLayout.CHIP_HEIGHT);
            int color = ColorUtil.lerp(theme.text(), theme.onAccent(), overlap * verticalOverlap);
            String label = labels.get(chip.index());
            float textY = Renderer2D.textY(originY + chipTop, ChipLayout.CHIP_HEIGHT, ChipLayout.TEXT_SIZE, ChipLayout.TEXT_WEIGHT);
            Renderer2D.textCentered(label, originX + chip.x() + chip.width() * 0.5f, textY,
                    ChipLayout.TEXT_SIZE, ChipLayout.TEXT_WEIGHT, color);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return false;
        }
        int index = chips().indexAt(mouseX - x - PADDING_X, mouseY - y - LABEL_ROW);
        if (index >= 0) {
            setting.select(index);
        }
        return true;
    }
}
