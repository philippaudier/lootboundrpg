package lootboundrpg.lootbound_rpg.registry;

import lootboundrpg.lootbound_rpg.LootboundRpgMod;
import lootboundrpg.lootbound_rpg.screen.UpgradeTableScreenHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;

/**
 * Registers all screen handlers (menus) for Lootbound RPG.
 */
public class ModScreenHandlers {

    public static final MenuType<UpgradeTableScreenHandler> UPGRADE_TABLE =
            Registry.register(
                    BuiltInRegistries.MENU,
                    LootboundRpgMod.id("upgrade_table"),
                    new MenuType<>(UpgradeTableScreenHandler::new, FeatureFlags.DEFAULT_FLAGS)
            );

    public static void register() {
        LootboundRpgMod.LOGGER.info("Registering Lootbound RPG screen handlers...");
    }
}
