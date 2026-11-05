package cute.bytebuffer.adorance.util.render.font;

record Glyph(float minU, float maxU, float minV, float maxV,
             float bearingX, float bearingY, float width, float height, float advance) {

    static Glyph blank(float advance) {
        return new Glyph(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, advance);
    }

    boolean visible() {
        return width > 0f && height > 0f;
    }
}
