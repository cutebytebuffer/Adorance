package cute.bytebuffer.adorance.util.render.gpu;

import com.mojang.blaze3d.systems.RenderSystem;
import cute.bytebuffer.adorance.util.render.postprocess.RenderLayerRetarget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.SimpleFramebuffer;
import net.minecraft.client.render.RenderLayer;

import java.util.IdentityHashMap;
import java.util.Map;

public final class ManagedFramebuffer {

    private final String name;
    private final boolean depth;
    private final Map<RenderLayer, RenderLayer> retargetedLayers = new IdentityHashMap<>();
    private SimpleFramebuffer framebuffer;

    public ManagedFramebuffer(String name, boolean depth) {
        this.name = name;
        this.depth = depth;
    }

    public String name() {
        return name;
    }

    public SimpleFramebuffer get() {
        if (framebuffer == null) {
            Framebuffer main = MinecraftClient.getInstance().getFramebuffer();
            framebuffer = new SimpleFramebuffer(name, main.textureWidth, main.textureHeight, depth);
        }
        return framebuffer;
    }

    public RenderLayer retarget(RenderLayer layer) {
        return retargetedLayers.computeIfAbsent(layer, base -> RenderLayerRetarget.copy(base, name, this::get));
    }

    public void resize(int width, int height) {
        if (framebuffer != null && framebuffer.textureWidth == width && framebuffer.textureHeight == height) {
            return;
        }
        destroy();
        framebuffer = new SimpleFramebuffer(name, width, height, depth);
    }

    public void clear() {
        try {
            SimpleFramebuffer target = get();
            if (target.getColorAttachment() == null || depth && target.getDepthAttachment() == null) {
                return;
            }
            var encoder = RenderSystem.getDevice().createCommandEncoder();
            if (depth) {
                encoder.clearColorAndDepthTextures(target.getColorAttachment(), 0, target.getDepthAttachment(), 1.0);
            } else {
                encoder.clearColorTexture(target.getColorAttachment(), 0);
            }
        } catch (RuntimeException exception) {
            RenderErrors.report("clear " + name, exception);
        }
    }

    public void destroy() {
        if (framebuffer != null) {
            framebuffer.delete();
            framebuffer = null;
        }
    }
}
