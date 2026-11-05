package cute.bytebuffer.adorance.module.impl.render;

import cute.bytebuffer.adorance.module.Category;
import cute.bytebuffer.adorance.module.Module;
import cute.bytebuffer.adorance.module.setting.BooleanSetting;

public final class NoEquipAnimationModule extends Module {
    private final BooleanSetting excludeGlowstone = register(new BooleanSetting("Exclude Glowstone", false));
    private final BooleanSetting excludeAnchors = register(new BooleanSetting("Exclude Anchors", false));

    public NoEquipAnimationModule() {
        super("No Equip Animation", "Removes the first-person held-item switch animation.", Category.RENDER);
    }

    public boolean excludesGlowstone() {
        return excludeGlowstone.get();
    }

    public boolean excludesAnchors() {
        return excludeAnchors.get();
    }
}
