package cute.bytebuffer.adorance.util.render;

import com.mojang.blaze3d.systems.ProjectionType;
import com.mojang.blaze3d.systems.RenderSystem;
import cute.bytebuffer.adorance.Adorance;
import cute.bytebuffer.adorance.util.render.font.FontRenderer;
import cute.bytebuffer.adorance.util.render.font.FontWeight;
import cute.bytebuffer.adorance.util.render.font.Fonts;
import cute.bytebuffer.adorance.util.render.shader.IconRenderer;
import cute.bytebuffer.adorance.util.render.shader.VectorRenderer;
import cute.bytebuffer.adorance.util.render.shader.VectorRenderer.Shape;
import cute.bytebuffer.adorance.util.Rect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.ProjectionMatrix2;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2f;
import org.joml.Matrix4fStack;

public final class Renderer2D {

    private static final ProjectionMatrix2 PROJECTION = new ProjectionMatrix2(Adorance.MOD_ID, 1000f, 11000f, true);
    private static final Matrix3x2f IDENTITY = new Matrix3x2f();
    private static final String ELLIPSIS = "...";

    private Renderer2D() {
    }

    public static void rect(float x, float y, float width, float height, float radius, int color) {
        rect(x, y, width, height, CornerRadii.all(radius), color);
    }

    public static void rect(Rect bounds, float radius, int color) {
        rect(bounds.x(), bounds.y(), bounds.width(), bounds.height(), CornerRadii.all(radius), color);
    }

    public static void rect(float x, float y, float width, float height, CornerRadii radii, int color) {
        if (ColorUtil.alpha(color) == 0) {
            return;
        }
        VectorRenderer.begin(Shape.ROUNDED_RECT).radii(radii).solid(color).draw(x, y, width, height);
    }

    public static void rect(float x, float y, float width, float height, float radius, int fill, int border, float borderWidth) {
        VectorRenderer.begin(Shape.ROUNDED_RECT).radii(CornerRadii.all(radius)).solid(fill).border(border, borderWidth)
                .draw(x, y, width, height);
    }

    public static void outline(float x, float y, float width, float height, float radius, int color, float lineWidth) {
        if (ColorUtil.alpha(color) == 0) {
            return;
        }
        rect(x, y, width, height, radius, 0, color, lineWidth);
    }

    public static void outline(Rect bounds, float radius, int color, float lineWidth) {
        outline(bounds.x(), bounds.y(), bounds.width(), bounds.height(), radius, color, lineWidth);
    }

    public static void gradient(float x, float y, float width, float height, float radius, int from, int to,
                                float directionX, float directionY) {
        VectorRenderer.begin(Shape.ROUNDED_RECT).radii(CornerRadii.all(radius)).gradient(from, to, directionX, directionY)
                .draw(x, y, width, height);
    }

    public static void hueBar(float x, float y, float width, float height, float radius) {
        VectorRenderer.begin(Shape.ROUNDED_RECT).radii(CornerRadii.all(radius)).hue().draw(x, y, width, height);
    }

    public static void shadow(Rect bounds, float radius, int color, float softness) {
        VectorRenderer.begin(Shape.ROUNDED_RECT).radii(CornerRadii.all(radius)).shadow(color, softness)
                .draw(bounds.x(), bounds.y(), bounds.width(), bounds.height());
    }

    public static void blurredRect(Rect bounds, float radius, int tint, float blurRadius) {
        VectorRenderer renderer = VectorRenderer.begin(Shape.ROUNDED_RECT).radii(CornerRadii.all(radius)).solid(tint);
        if (blurRadius > 0f) {
            renderer.backdrop(blurRadius);
        }
        renderer.draw(bounds.x(), bounds.y(), bounds.width(), bounds.height());
    }

    public static void circle(float centerX, float centerY, float radius, int color) {
        if (ColorUtil.alpha(color) == 0) {
            return;
        }
        VectorRenderer.begin(Shape.CIRCLE).solid(color).draw(centerX - radius, centerY - radius, radius * 2f, radius * 2f);
    }

    public static void circle(float centerX, float centerY, float radius, int fill, int border, float borderWidth) {
        VectorRenderer.begin(Shape.CIRCLE).solid(fill).border(border, borderWidth)
                .draw(centerX - radius, centerY - radius, radius * 2f, radius * 2f);
    }

    public static void line(float x1, float y1, float x2, float y2, float width, int color) {
        if (ColorUtil.alpha(color) == 0) {
            return;
        }
        VectorRenderer.begin(Shape.LINE).solid(color).line(x1, y1, x2, y2, width);
    }

    public static void icon(Identifier texture, float x, float y, float size, int tint) {
        IconRenderer.draw(texture, x, y, size, size, 0f, tint);
    }

    public static void icon(Identifier texture, float x, float y, float width, float height, float rotation, int tint) {
        IconRenderer.draw(texture, x, y, width, height, rotation, tint);
    }

    public static void text(String text, float x, float y, float size, FontWeight weight, int color) {
        int argb = Opacity.apply(color);
        if (text.isEmpty() || ColorUtil.alpha(argb) == 0) {
            return;
        }
        float exactSize = RenderState.ts(size);
        float atlasSize = exactSize == size ? size : quantize(exactSize);
        FontRenderer font = Fonts.get(weight, atlasSize);
        float drawX = RenderState.tx(x);
        float drawY = RenderState.ty(y);
        float residual = exactSize / atlasSize;
        Matrix3x2f matrix = Math.abs(residual - 1f) < 0.001f
                ? IDENTITY
                : new Matrix3x2f().translate(drawX, drawY).scale(residual).translate(-drawX, -drawY);
        withGuiProjection(() -> font.draw(matrix, text, drawX, drawY, argb));
    }

    public static void textCentered(String text, float centerX, float y, float size, FontWeight weight, int color) {
        text(text, centerX - textWidth(text, size, weight) * 0.5f, y, size, weight, color);
    }

    public static float textWidth(String text, float size, FontWeight weight) {
        return Fonts.width(weight, size, text);
    }

    public static float textHeight(float size, FontWeight weight) {
        return Fonts.lineHeight(weight, size);
    }

    public static float textY(float top, float height, float size, FontWeight weight) {
        return Fonts.centeredY(weight, size, top, height);
    }

    public static String ellipsize(String text, float maxWidth, float size, FontWeight weight) {
        if (textWidth(text, size, weight) <= maxWidth) {
            return text;
        }
        float available = maxWidth - textWidth(ELLIPSIS, size, weight);
        int end = text.length();
        while (end > 0 && textWidth(text.substring(0, end), size, weight) > available) {
            end--;
        }
        return text.substring(0, end).stripTrailing() + ELLIPSIS;
    }

    public static void pushClip(Rect bounds) {
        RenderState.pushScissor(bounds.x(), bounds.y(), bounds.width(), bounds.height());
    }

    public static void popClip() {
        RenderState.popScissor();
    }

    private static float quantize(float size) {
        double step = Math.round(Math.log(size) / Math.log(2.0) * 4.0);
        return (float) Math.pow(2.0, step / 4.0);
    }

    private static void withGuiProjection(Runnable draw) {
        Window window = MinecraftClient.getInstance().getWindow();
        float width = (float) window.getFramebufferWidth() / window.getScaleFactor();
        float height = (float) window.getFramebufferHeight() / window.getScaleFactor();
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(PROJECTION.set(width, height), ProjectionType.ORTHOGRAPHIC);
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.translation(0f, 0f, -11000f);
        try {
            draw.run();
        } finally {
            modelView.popMatrix();
            RenderSystem.restoreProjectionMatrix();
        }
    }
}
