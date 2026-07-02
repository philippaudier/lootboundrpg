package lootboundrpg.lootbound_rpg.registry;

import lootboundrpg.lootbound_rpg.LootboundRpgMod;
import lootboundrpg.lootbound_rpg.block.entity.UpgradeTableBlockEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Set;

/**
 * Registers all block entities for Lootbound RPG.
 */
public class ModBlockEntities {

    public static final BlockEntityType<UpgradeTableBlockEntity> UPGRADE_TABLE =
            Registry.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE,
                    LootboundRpgMod.id("upgrade_table"),
                    new BlockEntityType<>(
                            UpgradeTableBlockEntity::new,
                            Set.of(ModBlocks.UPGRADE_TABLE)
                    )
            );

    public static void register() {
        LootboundRpgMod.LOGGER.info("Registering Lootbound RPG block entities...");
    }
}
