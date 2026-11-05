package cute.bytebuffer.adorance.gui.widget;

import cute.bytebuffer.adorance.gui.animation.Smooth;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.module.setting.Setting;

public abstract class SettingWidget<S extends Setting<?>> extends Widget {

    private static final float VISIBILITY_SPEED = 12f;
    private static final float INTERACTIVE_THRESHOLD = 0.5f;

    protected final S setting;
    private final Smooth shown;

    protected SettingWidget(S setting) {
        this.setting = setting;
        this.shown = new Smooth(VISIBILITY_SPEED, setting.isVisible() ? 1f : 0f);
    }

    @Override
    public void update(float dt) {
        shown.update(setting.isVisible(), dt);
    }

    @Override
    public float layoutHeight() {
        return height() * shown.get();
    }

    @Override
    public float visibility() {
        return shown.get();
    }

    @Override
    public boolean interactive() {
        return setting.isVisible() && shown.get() > INTERACTIVE_THRESHOLD;
    }

    protected final void drawLabel() {
        drawLabel(y, ROW_HEIGHT);
    }

    protected final void drawLabel(float top, float rowHeight) {
        float textY = Renderer2D.textY(top, rowHeight, LABEL_SIZE, LABEL_WEIGHT);
        Renderer2D.text(setting.name(), x + PADDING_X, textY, LABEL_SIZE, LABEL_WEIGHT, theme().text());
    }
}
