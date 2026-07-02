package lootboundrpg.lootbound_rpg.registry;

import lootboundrpg.lootbound_rpg.LootboundRpgMod;
import lootboundrpg.lootbound_rpg.block.UpgradeTableBlock;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

/**
 * Registers all blocks for Lootbound RPG.
 */
public class ModBlocks {

    public static final Block UPGRADE_TABLE = registerWithItem(
            "upgrade_table",
            props -> new UpgradeTableBlock(props
                    .strength(2.5f)
                    .requiresCorrectToolForDrops())
    );

    private static Block registerWithItem(String name, Function<BlockBehaviour.Properties, Block> blockFactory) {
        // Create the resource keys first
        ResourceKey<Block> blockKey = ResourceKey.create(
                BuiltInRegistries.BLOCK.key(),
                LootboundRpgMod.id(name)
        );
        ResourceKey<Item> itemKey = ResourceKey.create(
                BuiltInRegistries.ITEM.key(),
                LootboundRpgMod.id(name)
        );

        // Create properties with the ID set
        BlockBehaviour.Properties props = BlockBehaviour.Properties.of().setId(blockKey);

        // Create the block using the factory
        Block block = blockFactory.apply(props);

        // Register the block
        Registry.register(BuiltInRegistries.BLOCK, blockKey, block);

        // Register the block item
        BlockItem blockItem = new BlockItem(block, new Item.Properties().setId(itemKey));
        Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem);

        return block;
    }

    public static void register() {
        LootboundRpgMod.LOGGER.info("Registering Lootbound RPG blocks...");

        // Add to creative tab
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.FUNCTIONAL_BLOCKS).register(entries -> {
            entries.accept(UPGRADE_TABLE);
        });
    }
}
