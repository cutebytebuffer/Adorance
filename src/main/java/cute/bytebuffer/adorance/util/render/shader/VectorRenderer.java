package cute.bytebuffer.adorance.util.render.shader;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import cute.bytebuffer.adorance.util.render.ColorUtil;
import cute.bytebuffer.adorance.util.render.CornerRadii;
import cute.bytebuffer.adorance.util.render.Opacity;
import cute.bytebuffer.adorance.util.render.RenderState;
import cute.bytebuffer.adorance.util.render.gpu.PassBuilder;
import cute.bytebuffer.adorance.util.render.gpu.Pipelines;
import cute.bytebuffer.adorance.util.render.gpu.UboLayout;
import cute.bytebuffer.adorance.util.render.gpu.UboPool;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

public final class VectorRenderer {

    public enum Shape {
        ROUNDED_RECT(0f),
        CIRCLE(1f),
        LINE(2f);

        private final float id;

        Shape(float id) {
            this.id = id;
        }
    }

    private enum Fill {
        SOLID(0f),
        GRADIENT(1f),
        HUE(2f),
        SHADOW(3f);

        private final float id;

        Fill(float id) {
            this.id = id;
        }
    }

    private static final int UBO_SIZE = new UboLayout()
            .vec4().vec2().vec4().vec4().vec4().vec4().vec2().float1()
            .vec4().float1().float1().float1().float1()
            .vec4().float1()
            .size();

    private static final RenderPipeline PIPELINE = Pipelines.translucent("vector", "gui/vector", "VectorParams", "InSampler");
    private static final VectorRenderer INSTANCE = new VectorRenderer();

    private Shape shape;
    private Fill fill;
    private CornerRadii radii;
    private int fillColor;
    private int gradientFrom;
    private int gradientTo;
    private float gradientX;
    private float gradientY;
    private int borderColor;
    private float borderWidth;
    private float softness;
    private float backdrop;
    private final float[] line = new float[4];

    private VectorRenderer() {
    }

    public static VectorRenderer begin(Shape shape) {
        return INSTANCE.reset(shape);
    }

    private VectorRenderer reset(Shape shape) {
        this.shape = shape;
        this.fill = Fill.SOLID;
        this.radii = CornerRadii.NONE;
        this.fillColor = 0;
        this.borderColor = 0;
        this.borderWidth = 0f;
        this.softness = 0f;
        this.backdrop = 0f;
        return this;
    }

    public VectorRenderer radii(CornerRadii radii) {
        this.radii = radii;
        return this;
    }

    public VectorRenderer solid(int argb) {
        this.fill = Fill.SOLID;
        this.fillColor = argb;
        return this;
    }

    public VectorRenderer gradient(int from, int to, float directionX, float directionY) {
        this.fill = Fill.GRADIENT;
        this.gradientFrom = from;
        this.gradientTo = to;
        this.gradientX = directionX;
        this.gradientY = -directionY;
        return this;
    }

    public VectorRenderer hue() {
        this.fill = Fill.HUE;
        return this;
    }

    public VectorRenderer shadow(int argb, float softness) {
        this.fill = Fill.SHADOW;
        this.fillColor = argb;
        this.softness = softness;
        return this;
    }

    public VectorRenderer border(int argb, float width) {
        this.borderColor = argb;
        this.borderWidth = width;
        return this;
    }

    public VectorRenderer backdrop(float radius) {
        this.backdrop = radius;
        return this;
    }

    public void line(float x1, float y1, float x2, float y2, float width) {
        line[0] = x1;
        line[1] = y1;
        line[2] = x2;
        line[3] = y2;
        borderWidth = width;
        float pad = width * 0.5f + 1f;
        float left = Math.min(x1, x2) - pad;
        float top = Math.min(y1, y2) - pad;
        draw(left, top, Math.abs(x2 - x1) + pad * 2f, Math.abs(y2 - y1) + pad * 2f);
    }

    public void draw(float x, float y, float width, float height) {
        if (width <= 0f || height <= 0f || Opacity.isInvisible()) {
            return;
        }
        Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
        float factor = RenderState.scaleFactor();
        float scale = RenderState.transformScale();
        float framebufferHeight = main.textureHeight;
        float pixelX = RenderState.tx(x) * factor;
        float pixelY = RenderState.ty(y) * factor;
        float pixelWidth = width * scale * factor;
        float pixelHeight = height * scale * factor;
        float regionY = framebufferHeight - pixelY - pixelHeight;

        Framebuffer blurred = backdrop > 0f
                ? BackdropBlur.blur(main, pixelX, regionY, pixelWidth, pixelHeight, backdrop * scale * factor)
                : null;
        boolean useBackdrop = blurred != null;
        float globalAlpha = Opacity.current();
        int fillArgb = useBackdrop ? fillColor : Opacity.apply(fillColor);
        int borderArgb = Opacity.apply(borderColor);
        float pixelScale = scale * factor;

        GpuBuffer ubo = UboPool.build("adorance:vector", UBO_SIZE, builder -> {
            builder.putVec4(pixelX, regionY, pixelWidth, pixelHeight);
            builder.putVec2(main.textureWidth, framebufferHeight);
            builder.putVec4(radii.topLeft() * pixelScale, radii.topRight() * pixelScale,
                    radii.bottomRight() * pixelScale, radii.bottomLeft() * pixelScale);
            putColor(builder, fillArgb);
            putColor(builder, gradientFrom);
            putColor(builder, gradientTo);
            builder.putVec2(gradientX, gradientY);
            builder.putFloat(borderWidth * pixelScale);
            putColor(builder, borderArgb);
            builder.putFloat(shape.id);
            builder.putFloat(fill.id);
            builder.putFloat(softness * pixelScale);
            builder.putFloat(useBackdrop ? 1f : 0f);
            builder.putVec4(
                    RenderState.tx(line[0]) * factor, framebufferHeight - RenderState.ty(line[1]) * factor,
                    RenderState.tx(line[2]) * factor, framebufferHeight - RenderState.ty(line[3]) * factor);
            builder.putFloat(globalAlpha);
        });

        PassBuilder pass = PassBuilder.create("adorance:vector")
                .pipeline(PIPELINE)
                .target(main)
                .scissor(RenderState.activeScissor())
                .uniform("VectorParams", ubo);
        if (useBackdrop) {
            pass.sampler("InSampler", blurred);
        }
        pass.draw();
    }

    private static void putColor(Std140Builder builder, int argb) {
        float[] color = ColorUtil.toVec4(argb);
        builder.putVec4(color[0], color[1], color[2], color[3]);
    }

    public static void destroy() {
        BackdropBlur.destroy();
    }
}
