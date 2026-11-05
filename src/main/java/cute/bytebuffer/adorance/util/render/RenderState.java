package cute.bytebuffer.adorance.util.render;

import net.minecraft.client.MinecraftClient;

import java.util.ArrayDeque;
import java.util.Deque;

public final class RenderState {

    public record ScissorRect(int x, int y, int width, int height) {

        static final ScissorRect EMPTY = new ScissorRect(0, 0, 0, 0);

        ScissorRect intersect(ScissorRect other) {
            int left = Math.max(x, other.x);
            int bottom = Math.max(y, other.y);
            int right = Math.min(x + width, other.x + other.width);
            int top = Math.min(y + height, other.y + other.height);
            return right <= left || top <= bottom ? EMPTY : new ScissorRect(left, bottom, right - left, top - bottom);
        }
    }

    private record Transform(float offsetX, float offsetY, float scale) {

        static final Transform IDENTITY = new Transform(0f, 0f, 1f);

        Transform scaledAround(float originX, float originY, float factor) {
            float nextX = offsetX + originX * (1f - factor) * scale;
            float nextY = offsetY + originY * (1f - factor) * scale;
            return new Transform(nextX, nextY, scale * factor);
        }
    }

    private static final Deque<Transform> TRANSFORMS = new ArrayDeque<>();
    private static final Deque<ScissorRect> SCISSORS = new ArrayDeque<>();

    private RenderState() {
    }

    private static Transform current() {
        Transform transform = TRANSFORMS.peek();
        return transform == null ? Transform.IDENTITY : transform;
    }

    public static void pushTransform(float originX, float originY, float scale) {
        TRANSFORMS.push(current().scaledAround(originX, originY, scale));
    }

    public static void popTransform() {
        TRANSFORMS.poll();
    }

    public static float transformScale() {
        return current().scale;
    }

    public static float tx(float x) {
        Transform transform = current();
        return transform.offsetX + x * transform.scale;
    }

    public static float ty(float y) {
        Transform transform = current();
        return transform.offsetY + y * transform.scale;
    }

    public static float ts(float size) {
        return size * transformScale();
    }

    public static void pushScissor(float x, float y, float width, float height) {
        int framebufferHeight = MinecraftClient.getInstance().getFramebuffer().textureHeight;
        float factor = scaleFactor();
        int pixelX = Math.round(tx(x) * factor);
        int pixelWidth = Math.round(ts(width) * factor);
        int pixelHeight = Math.round(ts(height) * factor);
        int pixelY = framebufferHeight - Math.round(ty(y) * factor) - pixelHeight;
        ScissorRect rect = new ScissorRect(pixelX, pixelY, Math.max(0, pixelWidth), Math.max(0, pixelHeight));
        ScissorRect parent = SCISSORS.peek();
        SCISSORS.push(parent == null ? rect : rect.intersect(parent));
    }

    public static void popScissor() {
        SCISSORS.poll();
    }

    public static ScissorRect activeScissor() {
        return SCISSORS.peek();
    }

    public static float scaleFactor() {
        return (float) MinecraftClient.getInstance().getWindow().getScaleFactor();
    }

    public static void reset() {
        TRANSFORMS.clear();
        SCISSORS.clear();
    }
}
