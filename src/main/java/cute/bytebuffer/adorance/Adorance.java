package cute.bytebuffer.adorance;

import cute.bytebuffer.adorance.config.ConfigManager;
import cute.bytebuffer.adorance.event.EventBus;
import cute.bytebuffer.adorance.event.EventHandler;
import cute.bytebuffer.adorance.event.impl.KeyPressEvent;
import cute.bytebuffer.adorance.gui.ClickGui;
import cute.bytebuffer.adorance.gui.ClientSettings;
import cute.bytebuffer.adorance.module.ModuleManager;
import cute.bytebuffer.adorance.module.impl.optimizer.HerosAnchorOptimizerModule;
import cute.bytebuffer.adorance.module.impl.render.NoExplosionParticleModule;
import cute.bytebuffer.adorance.module.impl.render.SmallPopParticlesModule;
import cute.bytebuffer.adorance.module.impl.render.NoEquipAnimationModule;
import cute.bytebuffer.adorance.util.world.AnchorOptimizerBlocks;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;

public final class Adorance implements ClientModInitializer {
    public static final String MOD_ID = "adorance";
    public static final String NAME = "Adorance";

    private static ClickGui clickGui;

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public static ClickGui clickGui() {
        if (clickGui == null) {
            clickGui = new ClickGui();
        }
        return clickGui;
    }

    @Override
    public void onInitializeClient() {
        AnchorOptimizerBlocks.initialize();
        ModuleManager modules = ModuleManager.get();
        modules.register(
                new HerosAnchorOptimizerModule(),
                new NoExplosionParticleModule(),
                new SmallPopParticlesModule(),
                new NoEquipAnimationModule()
        );
        EventBus.get().subscribe(modules);
        EventBus.get().subscribe(this);
        ConfigManager.load();
    }

    @EventHandler
    private void onKeyPress(KeyPressEvent event) {
        if (ClientSettings.get().clickGuiKey().matches(event.key())) {
            MinecraftClient.getInstance().setScreen(clickGui());
            event.cancel();
        }
    }
}
