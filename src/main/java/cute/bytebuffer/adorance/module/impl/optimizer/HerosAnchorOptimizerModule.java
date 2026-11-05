package cute.bytebuffer.adorance.module.impl.optimizer;

import cute.bytebuffer.adorance.module.Category;
import cute.bytebuffer.adorance.module.Module;
import cute.bytebuffer.adorance.module.setting.NumberSetting;
import cute.bytebuffer.adorance.util.world.AnchorOptimizerBlocks;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.util.ActionResult;
import net.minecraft.world.attribute.EnvironmentAttributes;

public final class HerosAnchorOptimizerModule extends Module {

    private final NumberSetting minimumCharge = register(new NumberSetting("Minimum Charge", 1, 1, 4, 1));

    public HerosAnchorOptimizerModule() {
        super("Hero's Anchor Optimizer",
                "Locally replaces an exploding respawn anchor with a replaceable ghost anchor.",
                Category.OPTIMIZERS);
        UseBlockCallback.EVENT.register((player, world, hand, hit) -> {
            if (!isEnabled() || !world.isClient() || MinecraftClient.getInstance().isInSingleplayer()
                    || player.isSneaking()) {
                return ActionResult.PASS;
            }

            var pos = hit.getBlockPos();
            var state = world.getBlockState(pos);
            if (!state.isOf(Blocks.RESPAWN_ANCHOR)
                    || state.get(net.minecraft.block.RespawnAnchorBlock.CHARGES) < minimumCharge.intValue()
                    || Boolean.TRUE.equals(world.getEnvironmentAttributes().getAttributeValue(
                            EnvironmentAttributes.RESPAWN_ANCHOR_WORKS_GAMEPLAY, pos))
                    || player.getStackInHand(hand).isOf(Items.GLOWSTONE)
                    || player.getOffHandStack().isOf(Items.GLOWSTONE)) {
                return ActionResult.PASS;
            }

            world.setBlockState(pos, AnchorOptimizerBlocks.GHOST_ANCHOR.getDefaultState());
            return ActionResult.SUCCESS;
        });
    }
}
