package cute.bytebuffer.adorance.mixin.accessor;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.BufferAllocator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.SequencedMap;

@Mixin(VertexConsumerProvider.Immediate.class)
public interface VertexConsumerProviderImmediateAccessor {

    @Accessor
    SequencedMap<RenderLayer, BufferAllocator> getLayerBuffers();
}
