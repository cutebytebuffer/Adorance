package cute.bytebuffer.adorance.mixin;

import cute.bytebuffer.adorance.event.EventBus;
import cute.bytebuffer.adorance.event.impl.Render2DEvent;
import cute.bytebuffer.adorance.gui.ClickGui;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.render.RenderTickCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin {

    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRenderStart(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        if (MinecraftClient.getInstance().currentScreen instanceof ClickGui) {
            postRender2D(context, tickCounter);
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRender(DrawContext context, RenderTickCounter tickCounter, CallbackInfo ci) {
        postRender2D(context, tickCounter);
    }

    @Unique
    private static void postRender2D(DrawContext context, RenderTickCounter tickCounter) {
        EventBus.get().post(new Render2DEvent(context, tickCounter.getTickProgress(false)));
    }
}
