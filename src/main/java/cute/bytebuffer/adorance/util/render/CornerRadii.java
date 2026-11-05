package cute.bytebuffer.adorance.util.render;

public record CornerRadii(float topLeft, float topRight, float bottomRight, float bottomLeft) {

    public static final CornerRadii NONE = all(0f);

    public static CornerRadii all(float radius) {
        return new CornerRadii(radius, radius, radius, radius);
    }

    public static CornerRadii of(float topLeft, float topRight, float bottomRight, float bottomLeft) {
        return new CornerRadii(topLeft, topRight, bottomRight, bottomLeft);
    }

    public CornerRadii scaled(float factor) {
        return new CornerRadii(topLeft * factor, topRight * factor, bottomRight * factor, bottomLeft * factor);
    }
}
