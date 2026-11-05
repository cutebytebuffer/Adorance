package cute.bytebuffer.adorance.module.impl.render;

import cute.bytebuffer.adorance.event.EventHandler;
import cute.bytebuffer.adorance.event.impl.ExplosionEvent;
import cute.bytebuffer.adorance.event.impl.ParticleSpawnEvent;
import cute.bytebuffer.adorance.event.impl.TickEvent;
import cute.bytebuffer.adorance.module.Category;
import cute.bytebuffer.adorance.module.Module;
import cute.bytebuffer.adorance.module.setting.BooleanSetting;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;

import java.util.HashMap;
import java.util.Map;

public final class NoExplosionParticleModule extends Module {

    private static final int EXPLOSION_WINDOW_TICKS = 8;

    private final BooleanSetting explosion = register(new BooleanSetting("Explosion", true));
    private final BooleanSetting emitter = register(new BooleanSetting("Explosion Emitter", true));
    private final Map<ParticleType<?>, Long> recentExplosions = new HashMap<>();
    private long tick;

    public NoExplosionParticleModule() {
        super("No Explosion Particles", "Suppresses clientside explosion particles.", Category.RENDER);
    }

    @Override
    protected void onDisable() {
        recentExplosions.clear();
    }

    @EventHandler
    private void onExplosion(ExplosionEvent event) {
        recentExplosions.put(event.particle().getType(), tick);
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        tick++;
        recentExplosions.values().removeIf(seenAt -> tick - seenAt > EXPLOSION_WINDOW_TICKS);
    }

    @EventHandler
    private void onParticleSpawn(ParticleSpawnEvent event) {
        ParticleType<?> type = event.particle().getType();
        if (emitter.get() && type == ParticleTypes.EXPLOSION_EMITTER) {
            event.cancel();
        } else if (explosion.get() && (type == ParticleTypes.EXPLOSION || recentExplosions.containsKey(type))) {
            event.cancel();
        }
    }
}
