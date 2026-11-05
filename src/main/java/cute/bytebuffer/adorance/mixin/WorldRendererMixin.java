package cute.bytebuffer.adorance.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderSystem;
import cute.bytebuffer.adorance.event.EventBus;
import cute.bytebuffer.adorance.event.impl.Render3DEvent;
import cute.bytebuffer.adorance.util.render.postprocess.PostProcessing;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.memory.ObjectAllocator;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderStart(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline,
                                              Camera camera, Matrix4f positionMatrix, Matrix4f basicProjectionMatrix,
                                              Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor,
                                              boolean renderSky, CallbackInfo ci) {
        PostProcessing.beginFrame();
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void onRender(ObjectAllocator allocator, RenderTickCounter tickCounter, boolean renderBlockOutline,
                                      Camera camera, Matrix4f positionMatrix, Matrix4f basicProjectionMatrix,
                                      Matrix4f projectionMatrix, GpuBufferSlice fogBuffer, Vector4f fogColor,
                                      boolean renderSky, CallbackInfo ci) {
        PostProcessing.composite();
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.mul(positionMatrix);
        try {
            EventBus.get().post(new Render3DEvent(positionMatrix, projectionMatrix, tickCounter.getTickProgress(false)));
        } finally {
            modelView.popMatrix();
        }
    }
}
