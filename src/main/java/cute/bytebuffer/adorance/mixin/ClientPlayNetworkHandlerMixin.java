package cute.bytebuffer.adorance.mixin;

import cute.bytebuffer.adorance.event.EventBus;
import cute.bytebuffer.adorance.event.impl.ExplosionEvent;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onExplosion", at = @At("HEAD"))
    private void onExplosion(ExplosionS2CPacket packet, CallbackInfo ci) {
        EventBus.get().post(new ExplosionEvent(packet.center(), packet.radius(), packet.explosionParticle()));
    }
}
