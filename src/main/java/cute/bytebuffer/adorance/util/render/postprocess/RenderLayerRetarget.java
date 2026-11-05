package cute.bytebuffer.adorance.util.render.postprocess;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import cute.bytebuffer.adorance.mixin.accessor.RenderLayerAccessor;
import cute.bytebuffer.adorance.mixin.accessor.RenderSetupAccessor;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.OutputTarget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderSetup;

import java.util.function.Supplier;

public final class RenderLayerRetarget {

    private RenderLayerRetarget() {
    }

    public static RenderLayer copy(RenderLayer base, String targetName, Supplier<Framebuffer> framebuffer) {
        RenderSetupAccessor setup = setupOf(base);
        return copy(base, targetName, framebuffer, setup.getPipeline());
    }

    public static RenderLayer copy(RenderLayer base, String targetName, Supplier<Framebuffer> framebuffer, RenderPipeline pipeline) {
        RenderSetupAccessor setup = setupOf(base);
        RenderSetup copy = RenderSetupAccessor.create(
                pipeline,
                setup.getTextures(),
                setup.isUseLightmap(),
                setup.isUseOverlay(),
                setup.getLayeringTransform(),
                new OutputTarget(targetName, framebuffer),
                setup.getTextureTransform(),
                setup.getOutlineMode(),
                setup.isHasCrumbling(),
                setup.isTranslucent(),
                setup.getExpectedBufferSize()
        );
        return RenderLayer.of(((RenderLayerAccessor) base).getName() + "_" + targetName, copy);
    }

    private static RenderSetupAccessor setupOf(RenderLayer layer) {
        return (RenderSetupAccessor) (Object) ((RenderLayerAccessor) layer).getRenderSetup();
    }
}
