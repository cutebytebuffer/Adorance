package cute.bytebuffer.adorance.util;

public record Rect(float x, float y, float width, float height) {

    public float right() {
        return x + width;
    }

    public float bottom() {
        return y + height;
    }

    public float centerX() {
        return x + width * 0.5f;
    }

    public float centerY() {
        return y + height * 0.5f;
    }

    public boolean contains(double px, double py) {
        return px >= x && px <= x + width && py >= y && py <= y + height;
    }

    public Rect inset(float amount) {
        return new Rect(x + amount, y + amount, Math.max(0f, width - amount * 2f), Math.max(0f, height - amount * 2f));
    }

    public Rect inset(float horizontal, float vertical) {
        return new Rect(x + horizontal, y + vertical, Math.max(0f, width - horizontal * 2f), Math.max(0f, height - vertical * 2f));
    }

    public Rect offset(float dx, float dy) {
        return new Rect(x + dx, y + dy, width, height);
    }

    public Rect withWidth(float newWidth) {
        return new Rect(x, y, newWidth, height);
    }

    public Rect withHeight(float newHeight) {
        return new Rect(x, y, width, newHeight);
    }

    public Rect sliceTop(float amount) {
        return new Rect(x, y, width, Math.min(height, amount));
    }

    public Rect belowTop(float amount) {
        float consumed = Math.min(height, amount);
        return new Rect(x, y + consumed, width, height - consumed);
    }
}
