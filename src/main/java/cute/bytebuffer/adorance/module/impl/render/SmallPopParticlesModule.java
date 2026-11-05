package cute.bytebuffer.adorance.module.impl.render;

import cute.bytebuffer.adorance.module.Category;
import cute.bytebuffer.adorance.module.Module;
import cute.bytebuffer.adorance.module.setting.NumberSetting;

public final class SmallPopParticlesModule extends Module {

    private final NumberSetting size = register(new NumberSetting("Size", 0.45, 0.1, 1.0, 0.05));

    public SmallPopParticlesModule() {
        super("Small Pop Particles", "Scales down the totem of undying particle effect.", Category.RENDER);
    }

    public float size() {
        return size.floatValue();
    }
}
