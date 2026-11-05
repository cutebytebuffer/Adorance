package cute.bytebuffer.adorance.event.impl;

import cute.bytebuffer.adorance.event.CancellableEvent;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;

public final class ParticleSpawnEvent extends CancellableEvent {

    private final ParticleEffect particle;
    private final Vec3d position;

    public ParticleSpawnEvent(ParticleEffect particle, Vec3d position) {
        this.particle = particle;
        this.position = position;
    }

    public ParticleEffect particle() {
        return particle;
    }

    public Vec3d position() {
        return position;
    }
}
