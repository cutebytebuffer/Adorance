package cute.bytebuffer.adorance.mixin;

import cute.bytebuffer.adorance.util.render.postprocess.Capture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.BatchingRenderCommandQueue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(BatchingRenderCommandQueue.class)
public abstract class BatchingRenderCommandQueueMixin {

    @ModifyVariable(method = "submitModel", at = @At("HEAD"), argsOnly = true)
    private RenderLayer onSubmitModel(RenderLayer layer) {
        return Capture.retarget(layer);
    }

    @ModifyVariable(method = "submitModelPart", at = @At("HEAD"), argsOnly = true)
    private RenderLayer onSubmitModelPart(RenderLayer layer) {
        return Capture.retarget(layer);
    }

    @ModifyVariable(method = "submitItem", at = @At("HEAD"), argsOnly = true)
    private RenderLayer onSubmitItem(RenderLayer layer) {
        return Capture.retarget(layer);
    }
}
