package cute.bytebuffer.adorance.gui.widget;

import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.util.render.Renderer2D;
import cute.bytebuffer.adorance.util.render.font.FontWeight;

import java.util.Locale;

public final class SectionWidget extends Widget {

    private static final float HEIGHT = 17f;
    private static final float TEXT_SIZE = 6.5f;
    private static final FontWeight TEXT_WEIGHT = FontWeight.SEMIBOLD;
    private static final float RULE_GAP = 6f;

    private final String title;

    public SectionWidget(String title) {
        this.title = title.toUpperCase(Locale.ROOT);
    }

    @Override
    public float height() {
        return HEIGHT;
    }

    @Override
    public boolean interactive() {
        return false;
    }

    @Override
    protected void draw(double mouseX, double mouseY, float dt) {
        Theme theme = theme();
        float textY = Renderer2D.textY(y + 3f, HEIGHT - 3f, TEXT_SIZE, TEXT_WEIGHT);
        Renderer2D.text(title, x + PADDING_X, textY, TEXT_SIZE, TEXT_WEIGHT, theme.mutedText());
        float ruleX = x + PADDING_X + Renderer2D.textWidth(title, TEXT_SIZE, TEXT_WEIGHT) + RULE_GAP;
        float ruleY = y + 3f + (HEIGHT - 3f) * 0.5f;
        if (right() > ruleX) {
            Renderer2D.rect(ruleX, ruleY - 0.5f, right() - ruleX, 1f, 0.5f, theme.outline());
        }
    }
}
