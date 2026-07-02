package lootboundrpg.lootbound_rpg;

import lootboundrpg.lootbound_rpg.command.DebugCommands;
import lootboundrpg.lootbound_rpg.command.GuideCommand;
import lootboundrpg.lootbound_rpg.component.ModDataComponents;
import lootboundrpg.lootbound_rpg.config.LootboundConfig;
import lootboundrpg.lootbound_rpg.item.ModItems;
import lootboundrpg.lootbound_rpg.loot.MobEquipmentDrops;
import lootboundrpg.lootbound_rpg.loot.ModLootTables;
import lootboundrpg.lootbound_rpg.mobpack.MobPackSpawner;
import lootboundrpg.lootbound_rpg.mobpack.VanillaSpawnBlocker;
import lootboundrpg.lootbound_rpg.registry.ModBlockEntities;
import lootboundrpg.lootbound_rpg.registry.ModBlocks;
import lootboundrpg.lootbound_rpg.registry.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main entrypoint for Lootbound RPG mod.
 *
 * This mod transforms Minecraft progression into a loot-based RPG
 * where players upgrade equipment from +0 to +10 using upgrade stones.
 */
public class LootboundRpgMod implements ModInitializer {
    public static final String MOD_ID = "lootbound_rpg";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Lootbound RPG...");

        // Load config first
        LootboundConfig.get();

        // Register data components first (needed for items)
        ModDataComponents.register();

        // Register all mod items (upgrade stones)
        ModItems.register();

        // Register blocks and block entities
        ModBlocks.register();
        ModBlockEntities.register();
        ModScreenHandlers.register();

        // Register commands
        DebugCommands.register();
        GuideCommand.register();

        // Register loot table modifications (stone drops)
        ModLootTables.register();

        // Register mob equipment drops (graded equipment)
        MobEquipmentDrops.register();

        // Register mob pack spawner (V1.2)
        MobPackSpawner.register();

        // Register vanilla spawn blocker (V1.2)
        VanillaSpawnBlocker.register();

        LOGGER.info("Lootbound RPG initialized!");
    }

    /**
     * Creates an Identifier for this mod.
     * @param path The resource path
     * @return Identifier with mod namespace
     */
    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
