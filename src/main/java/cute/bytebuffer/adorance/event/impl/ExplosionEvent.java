package cute.bytebuffer.adorance.event.impl;

import cute.bytebuffer.adorance.event.Event;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.math.Vec3d;

public final class ExplosionEvent extends Event {

    private final Vec3d center;
    private final float radius;
    private final ParticleEffect particle;

    public ExplosionEvent(Vec3d center, float radius, ParticleEffect particle) {
        this.center = center;
        this.radius = radius;
        this.particle = particle;
    }

    public Vec3d center() {
        return center;
    }

    public float radius() {
        return radius;
    }

    public ParticleEffect particle() {
        return particle;
    }
}
