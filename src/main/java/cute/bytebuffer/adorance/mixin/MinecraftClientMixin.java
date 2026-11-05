package cute.bytebuffer.adorance.mixin;

import cute.bytebuffer.adorance.config.ConfigManager;
import cute.bytebuffer.adorance.event.EventBus;
import cute.bytebuffer.adorance.event.impl.TickEvent;
import cute.bytebuffer.adorance.util.render.font.Fonts;
import cute.bytebuffer.adorance.util.render.gpu.UboPool;
import cute.bytebuffer.adorance.util.render.postprocess.PostProcessing;
import cute.bytebuffer.adorance.util.render.shader.VectorRenderer;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "tick", at = @At("HEAD"))
    private void onPreTick(CallbackInfo ci) {
        EventBus.get().post(TickEvent.Pre.get());
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void onPostTick(CallbackInfo ci) {
        EventBus.get().post(TickEvent.Post.get());
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(boolean tick, CallbackInfo ci) {
        UboPool.nextFrame();
        ConfigManager.flush(false);
    }

    @Inject(method = "onResolutionChanged", at = @At("RETURN"))
    private void onResolutionChanged(CallbackInfo ci) {
        PostProcessing.resize();
    }

    @Inject(method = "close", at = @At("HEAD"))
    private void onClose(CallbackInfo ci) {
        ConfigManager.save();
    }

    @Inject(method = "close", at = @At("RETURN"))
    private void onClosed(CallbackInfo ci) {
        PostProcessing.destroy();
        VectorRenderer.destroy();
        Fonts.close();
        UboPool.clear();
    }
}
