package cute.bytebuffer.adorance.mixin;

import cute.bytebuffer.adorance.module.ModuleManager;
import cute.bytebuffer.adorance.module.impl.render.NoEquipAnimationModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public abstract class HeldItemRendererMixin {

    @Shadow private MinecraftClient client;
    @Shadow private float equipProgressMainHand;
    @Shadow private float lastEquipProgressMainHand;
    @Shadow private float equipProgressOffHand;
    @Shadow private float lastEquipProgressOffHand;
    @Shadow private ItemStack mainHand;
    @Shadow private ItemStack offHand;

    @Inject(method = "updateHeldItems", at = @At("HEAD"))
    private void onUpdateHeldItemsStart(CallbackInfo ci) {
        setEquipProgress(0f);
    }

    @Inject(method = "updateHeldItems", at = @At("TAIL"))
    private void onUpdateHeldItemsEnd(CallbackInfo ci) {
        setEquipProgress(1f);
    }

    @Unique
    private void setEquipProgress(float progress) {
        NoEquipAnimationModule module = ModuleManager.get().find(NoEquipAnimationModule.class)
                .filter(NoEquipAnimationModule::isEnabled)
                .orElse(null);
        if (module == null || client.player == null) {
            return;
        }
        if (!isExcluded(module, mainHand, client.player.getMainHandStack())) {
            equipProgressMainHand = lastEquipProgressMainHand = progress;
        }
        if (!isExcluded(module, offHand, client.player.getOffHandStack())) {
            equipProgressOffHand = lastEquipProgressOffHand = progress;
        }
    }

    @Unique
    private static boolean isExcluded(NoEquipAnimationModule module, ItemStack previous, ItemStack current) {
        boolean glowstone = previous.isOf(Items.GLOWSTONE) || current.isOf(Items.GLOWSTONE);
        boolean anchor = previous.isOf(Items.RESPAWN_ANCHOR) || current.isOf(Items.RESPAWN_ANCHOR);
        return module.excludesGlowstone() && glowstone || module.excludesAnchors() && anchor;
    }
}
