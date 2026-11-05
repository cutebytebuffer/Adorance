package cute.bytebuffer.adorance.util.render.shader;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuTextureView;
import cute.bytebuffer.adorance.util.render.RenderState;
import cute.bytebuffer.adorance.util.render.gpu.ManagedFramebuffer;
import cute.bytebuffer.adorance.util.render.gpu.PassBuilder;
import cute.bytebuffer.adorance.util.render.gpu.Pipelines;
import cute.bytebuffer.adorance.util.render.gpu.UboLayout;
import cute.bytebuffer.adorance.util.render.gpu.UboPool;
import net.minecraft.client.gl.Framebuffer;

final class BackdropBlur {

    private static final int MAX_LEVELS = 6;
    private static final int UBO_SIZE = new UboLayout().vec2().float1().size();
    private static final RenderPipeline DOWNSAMPLE = Pipelines.opaque("kawase_down", "gui/kawase_down", "KawaseParams", "InSampler");
    private static final RenderPipeline UPSAMPLE = Pipelines.opaque("kawase_up", "gui/kawase_up", "KawaseParams", "InSampler");
    private static final ManagedFramebuffer[] LEVELS = new ManagedFramebuffer[MAX_LEVELS + 1];

    static {
        for (int level = 0; level <= MAX_LEVELS; level++) {
            LEVELS[level] = new ManagedFramebuffer("adorance:backdrop_blur_" + level, false);
        }
    }

    private BackdropBlur() {
    }

    static Framebuffer blur(Framebuffer main, float x, float y, float width, float height, float radiusPx) {
        GpuTextureView source = main.getColorAttachmentView();
        if (source == null || radiusPx <= 0f) {
            return null;
        }
        int framebufferWidth = main.textureWidth;
        int framebufferHeight = main.textureHeight;
        int iterations = Math.max(1, Math.min(MAX_LEVELS, (int) Math.ceil(Math.log(Math.max(radiusPx, 2f) / 1.5f) / Math.log(2))));
        float offset = Math.max(0.5f, Math.min(3f, radiusPx / (1 << iterations)));
        float margin = offset * (1 << (iterations + 2)) + 8f;

        int left = Math.max(0, (int) Math.floor(x - margin));
        int bottom = Math.max(0, (int) Math.floor(y - margin));
        int right = Math.min(framebufferWidth, (int) Math.ceil(x + width + margin));
        int top = Math.min(framebufferHeight, (int) Math.ceil(y + height + margin));
        if (right <= left || top <= bottom) {
            return null;
        }

        LEVELS[0].resize(framebufferWidth, framebufferHeight);
        for (int level = 1; level <= iterations; level++) {
            LEVELS[level].resize(Math.max(1, framebufferWidth >> level), Math.max(1, framebufferHeight >> level));
        }

        int sourceWidth = framebufferWidth;
        int sourceHeight = framebufferHeight;
        for (int level = 1; level <= iterations; level++) {
            Framebuffer destination = LEVELS[level].get();
            GpuTextureView view = level == 1 ? source : LEVELS[level - 1].get().getColorAttachmentView();
            pass("adorance:blur_down", DOWNSAMPLE, view, destination, 1f / sourceWidth, 1f / sourceHeight, offset,
                    scissor(left, bottom, right, top, level, destination));
            sourceWidth = destination.textureWidth;
            sourceHeight = destination.textureHeight;
        }
        for (int level = iterations; level >= 1; level--) {
            Framebuffer from = LEVELS[level].get();
            Framebuffer destination = LEVELS[level - 1].get();
            pass("adorance:blur_up", UPSAMPLE, from.getColorAttachmentView(), destination,
                    1f / from.textureWidth, 1f / from.textureHeight, offset, scissor(left, bottom, right, top, level - 1, destination));
        }
        return LEVELS[0].get();
    }

    private static RenderState.ScissorRect scissor(int left, int bottom, int right, int top, int level, Framebuffer destination) {
        int x0 = left >> level;
        int y0 = bottom >> level;
        int x1 = Math.min(destination.textureWidth, ((right - 1) >> level) + 2);
        int y1 = Math.min(destination.textureHeight, ((top - 1) >> level) + 2);
        return new RenderState.ScissorRect(x0, y0, Math.max(1, x1 - x0), Math.max(1, y1 - y0));
    }

    private static void pass(String label, RenderPipeline pipeline, GpuTextureView source, Framebuffer destination,
                             float texelX, float texelY, float offset, RenderState.ScissorRect scissor) {
        GpuBuffer ubo = UboPool.build("adorance:kawase", UBO_SIZE, builder -> {
            builder.putVec2(texelX, texelY);
            builder.putFloat(offset);
        });
        PassBuilder.create(label)
                .pipeline(pipeline)
                .target(destination)
                .scissor(scissor)
                .uniform("KawaseParams", ubo)
                .texture("InSampler", source, FilterMode.LINEAR)
                .draw();
    }

    static void destroy() {
        for (ManagedFramebuffer level : LEVELS) {
            level.destroy();
        }
    }
}
