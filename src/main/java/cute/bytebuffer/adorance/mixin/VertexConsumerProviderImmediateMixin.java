package cute.bytebuffer.adorance.mixin;

import cute.bytebuffer.adorance.mixin.accessor.VertexConsumerProviderImmediateAccessor;
import cute.bytebuffer.adorance.util.render.postprocess.Capture;
import cute.bytebuffer.adorance.util.render.postprocess.WrappedVertexConsumer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VertexConsumerProvider.Immediate.class)
public abstract class VertexConsumerProviderImmediateMixin {

    @ModifyVariable(method = "getBuffer", at = @At("HEAD"), argsOnly = true)
    private RenderLayer onGetBuffer(RenderLayer layer) {
        if (!Capture.isActive()) {
            return layer;
        }
        RenderLayer retargeted = Capture.retarget(layer);
        if (retargeted != layer) {
            ((VertexConsumerProviderImmediateAccessor) this).getLayerBuffers()
                    .putIfAbsent(retargeted, new BufferAllocator(retargeted.getExpectedBufferSize()));
        }
        return retargeted;
    }

    @Inject(method = "getBuffer", at = @At("RETURN"), cancellable = true)
    private void onGetBufferReturn(RenderLayer layer, CallbackInfoReturnable<VertexConsumer> cir) {
        if (Capture.isActive() && !(cir.getReturnValue() instanceof WrappedVertexConsumer)) {
            cir.setReturnValue(new WrappedVertexConsumer(cir.getReturnValue()));
        }
    }
}
