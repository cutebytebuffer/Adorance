package cute.bytebuffer.adorance.gui.widget;

import cute.bytebuffer.adorance.gui.animation.Smooth;
import cute.bytebuffer.adorance.gui.theme.Theme;
import cute.bytebuffer.adorance.util.render.font.FontWeight;

public abstract class Widget {

    protected static final float PADDING_X = 8f;
    protected static final float ROW_HEIGHT = 18f;
    protected static final float LABEL_SIZE = 8f;
    protected static final float VALUE_SIZE = 7.5f;
    protected static final FontWeight LABEL_WEIGHT = FontWeight.REGULAR;
    protected static final FontWeight VALUE_WEIGHT = FontWeight.MEDIUM;
    protected static final float ANIMATION_SPEED = 14f;

    protected final Smooth hover = new Smooth(ANIMATION_SPEED);
    protected float x;
    protected float y;
    protected float width;

    public final void layout(float x, float y, float width) {
        this.x = x;
        this.y = y;
        this.width = width;
    }

    public abstract float height();

    public float layoutHeight() {
        return height();
    }

    public void update(float dt) {
    }

    public float visibility() {
        return 1f;
    }

    public boolean interactive() {
        return true;
    }

    public final void render(double mouseX, double mouseY, float dt) {
        hover.update(interactive() && contains(mouseX, mouseY), dt);
        draw(mouseX, mouseY, dt);
    }

    protected abstract void draw(double mouseX, double mouseY, float dt);

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        return false;
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + layoutHeight();
    }

    protected final float right() {
        return x + width - PADDING_X;
    }

    protected static Theme theme() {
        return Theme.get();
    }
}
