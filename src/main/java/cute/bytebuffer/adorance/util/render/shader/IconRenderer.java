package cute.bytebuffer.adorance.util.render.shader;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.FilterMode;
import cute.bytebuffer.adorance.util.render.ColorUtil;
import cute.bytebuffer.adorance.util.render.Opacity;
import cute.bytebuffer.adorance.util.render.RenderState;
import cute.bytebuffer.adorance.util.render.gpu.PassBuilder;
import cute.bytebuffer.adorance.util.render.gpu.Pipelines;
import cute.bytebuffer.adorance.util.render.gpu.UboLayout;
import cute.bytebuffer.adorance.util.render.gpu.UboPool;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.util.Identifier;

public final class IconRenderer {

    private static final int UBO_SIZE = new UboLayout().vec4().vec2().float1().float1().vec4().size();
    private static final RenderPipeline PIPELINE = Pipelines.translucent("icon", "gui/icon", "IconParams", "IconSampler");
    private static final float EDGE_SMOOTHING = 1f;

    private IconRenderer() {
    }

    public static void draw(Identifier texture, float x, float y, float width, float height, float rotationDegrees, int tint) {
        int color = Opacity.apply(tint);
        if (ColorUtil.alpha(color) == 0 || width <= 0f || height <= 0f) {
            return;
        }
        MinecraftClient client = MinecraftClient.getInstance();
        Framebuffer main = client.getFramebuffer();
        AbstractTexture icon = client.getTextureManager().getTexture(texture);
        float factor = RenderState.scaleFactor();
        float scale = RenderState.transformScale();
        float pixelX = RenderState.tx(x) * factor;
        float pixelY = RenderState.ty(y) * factor;
        float pixelWidth = width * scale * factor;
        float pixelHeight = height * scale * factor;
        float framebufferHeight = main.textureHeight;
        float[] rgba = ColorUtil.toVec4(color);

        GpuBuffer ubo = UboPool.build("adorance:icon", UBO_SIZE, builder -> {
            builder.putVec4(pixelX, framebufferHeight - pixelY - pixelHeight, pixelWidth, pixelHeight);
            builder.putVec2(main.textureWidth, framebufferHeight);
            builder.putFloat((float) Math.toRadians(rotationDegrees));
            builder.putFloat(EDGE_SMOOTHING * factor);
            builder.putVec4(rgba[0], rgba[1], rgba[2], rgba[3]);
        });

        PassBuilder.create("adorance:icon")
                .pipeline(PIPELINE)
                .target(main)
                .scissor(RenderState.activeScissor())
                .texture("IconSampler", icon.getGlTextureView(), FilterMode.LINEAR)
                .uniform("IconParams", ubo)
                .draw();
    }
}
