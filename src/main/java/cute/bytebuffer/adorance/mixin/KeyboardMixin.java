package cute.bytebuffer.adorance.mixin;

import cute.bytebuffer.adorance.event.EventBus;
import cute.bytebuffer.adorance.event.impl.KeyPressEvent;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public abstract class KeyboardMixin {

    @Inject(method = "onKey", at = @At("HEAD"), cancellable = true)
    private void onKey(long window, int action, KeyInput input, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (action != GLFW.GLFW_PRESS || client.currentScreen != null || window != client.getWindow().getHandle()) {
            return;
        }
        if (EventBus.get().post(new KeyPressEvent(input.key())).isCancelled()) {
            ci.cancel();
        }
    }
}
