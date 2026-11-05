package cute.bytebuffer.adorance.mixin;

import cute.bytebuffer.adorance.event.EventBus;
import cute.bytebuffer.adorance.event.impl.ParticleSpawnEvent;
import cute.bytebuffer.adorance.module.ModuleManager;
import cute.bytebuffer.adorance.module.impl.render.SmallPopParticlesModule;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("HEAD"), cancellable = true)
    private void onAddParticle(ParticleEffect particle, double x, double y, double z,
                                          double velocityX, double velocityY, double velocityZ,
                                          CallbackInfoReturnable<Particle> cir) {
        if (EventBus.get().post(new ParticleSpawnEvent(particle, new Vec3d(x, y, z))).isCancelled()) {
            cir.setReturnValue(null);
        }
    }

    @Inject(method = "addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)Lnet/minecraft/client/particle/Particle;",
            at = @At("RETURN"))
    private void onParticleAdded(ParticleEffect particle, double x, double y, double z,
                                      double velocityX, double velocityY, double velocityZ,
                                      CallbackInfoReturnable<Particle> cir) {
        Particle spawned = cir.getReturnValue();
        if (spawned == null || particle.getType() != ParticleTypes.TOTEM_OF_UNDYING) {
            return;
        }
        ModuleManager.get().find(SmallPopParticlesModule.class)
                .filter(SmallPopParticlesModule::isEnabled)
                .ifPresent(module -> spawned.scale(module.size()));
    }

    @Inject(method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;)V",
            at = @At("HEAD"), cancellable = true)
    private void onAddEmitter(Entity entity, ParticleEffect particle, CallbackInfo ci) {
        if (EventBus.get().post(new ParticleSpawnEvent(particle, entity.getEntityPos())).isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "addEmitter(Lnet/minecraft/entity/Entity;Lnet/minecraft/particle/ParticleEffect;I)V",
            at = @At("HEAD"), cancellable = true)
    private void onAddEmitter(Entity entity, ParticleEffect particle, int maxAge, CallbackInfo ci) {
        if (EventBus.get().post(new ParticleSpawnEvent(particle, entity.getEntityPos())).isCancelled()) {
            ci.cancel();
        }
    }
}
