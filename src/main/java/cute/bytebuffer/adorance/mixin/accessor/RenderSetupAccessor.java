package cute.bytebuffer.adorance.mixin.accessor;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.render.LayeringTransform;
import net.minecraft.client.render.OutputTarget;
import net.minecraft.client.render.RenderSetup;
import net.minecraft.client.render.TextureTransform;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(RenderSetup.class)
public interface RenderSetupAccessor {

    @Accessor
    RenderPipeline getPipeline();

    @Accessor
    @SuppressWarnings("rawtypes")
    Map getTextures();

    @Accessor
    TextureTransform getTextureTransform();

    @Accessor
    OutputTarget getOutputTarget();

    @Accessor
    RenderSetup.OutlineMode getOutlineMode();

    @Accessor
    boolean isUseLightmap();

    @Accessor
    boolean isUseOverlay();

    @Accessor
    boolean isHasCrumbling();

    @Accessor
    boolean isTranslucent();

    @Accessor
    int getExpectedBufferSize();

    @Accessor
    LayeringTransform getLayeringTransform();

    @Invoker("<init>")
    @SuppressWarnings("rawtypes")
    static RenderSetup create(RenderPipeline pipeline, Map textures, boolean useLightmap, boolean useOverlay,
                              LayeringTransform layeringTransform, OutputTarget outputTarget,
                              TextureTransform textureTransform, RenderSetup.OutlineMode outlineMode,
                              boolean hasCrumbling, boolean translucent, int expectedBufferSize) {
        throw new AssertionError();
    }
}
