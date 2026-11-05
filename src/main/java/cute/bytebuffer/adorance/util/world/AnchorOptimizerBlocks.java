package cute.bytebuffer.adorance.util.world;

import cute.bytebuffer.adorance.Adorance;
import net.fabricmc.fabric.api.client.rendering.v1.BlockRenderLayerMap;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

public final class AnchorOptimizerBlocks {

    private static final Identifier GHOST_ANCHOR_ID = Adorance.id("ghost_anchor");
    private static final RegistryKey<Block> GHOST_ANCHOR_KEY = RegistryKey.of(RegistryKeys.BLOCK, GHOST_ANCHOR_ID);

    public static final Block GHOST_ANCHOR = Registry.register(Registries.BLOCK, GHOST_ANCHOR_ID,
            new Block(AbstractBlock.Settings.create().registryKey(GHOST_ANCHOR_KEY).nonOpaque().strength(-1.0f).dropsNothing()
                    .replaceable().sounds(BlockSoundGroup.GLASS)));

    private AnchorOptimizerBlocks() {
    }

    public static void initialize() {
        Identifier itemId = Adorance.id("ghost_anchor");
        RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, itemId);
        Registry.register(Registries.ITEM, itemId,
                new BlockItem(GHOST_ANCHOR, new Item.Settings().registryKey(itemKey)));
        BlockRenderLayerMap.putBlock(GHOST_ANCHOR, BlockRenderLayer.TRANSLUCENT);
    }
}
