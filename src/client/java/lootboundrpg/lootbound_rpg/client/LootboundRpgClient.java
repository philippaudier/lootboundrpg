package lootboundrpg.lootbound_rpg.client;

import lootboundrpg.lootbound_rpg.registry.ModScreenHandlers;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screens.MenuScreens;

/**
 * Client-side entrypoint for Lootbound RPG.
 * Used for rendering, UI, and client-only features.
 */
public class LootboundRpgClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register tooltip handler for equipment level display
        TooltipHandler.register();

        // Register Lootbound HUD renderer
        LbHudRenderer.register();

        // Register mob health bar renderer
        LbMobHealthRenderer.register();

        // Register screen for upgrade table
        MenuScreens.register(ModScreenHandlers.UPGRADE_TABLE, UpgradeTableScreen::new);
    }
}
