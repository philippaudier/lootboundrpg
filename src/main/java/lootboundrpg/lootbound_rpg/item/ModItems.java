package lootboundrpg.lootbound_rpg.item;

import lootboundrpg.lootbound_rpg.LootboundRpgMod;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

import java.util.function.Function;

/**
 * Registers all items for Lootbound RPG.
 *
 * Upgrade stones are used to enhance equipment from +0 to +10:
 * - Crude: +1 to +3 (100% success)
 * - Refined: +4 to +6 (80-60% success)
 * - Rare: +7 to +9 (45-25% success)
 * - Perfect: +10 (15% success)
 * - Protection/Stability: placeholders for future mechanics
 */
public class ModItems {

    // Upgrade stones - core progression items
    public static final Item CRUDE_UPGRADE_STONE = register("crude_upgrade_stone",
            Item::new, new Item.Properties());

    public static final Item REFINED_UPGRADE_STONE = register("refined_upgrade_stone",
            Item::new, new Item.Properties());

    public static final Item RARE_UPGRADE_STONE = register("rare_upgrade_stone",
            Item::new, new Item.Properties());

    public static final Item PERFECT_UPGRADE_STONE = register("perfect_upgrade_stone",
            Item::new, new Item.Properties());

    // Utility stones - placeholders for V1, will add mechanics later
    public static final Item PROTECTION_STONE = register("protection_stone",
            Item::new, new Item.Properties());

    public static final Item STABILITY_STONE = register("stability_stone",
            Item::new, new Item.Properties());

    /**
     * Registers an item with the game registry using MC 26.2 API.
     * @param name The item name (used for identifier)
     * @param factory Function to create the item from properties
     * @param settings Item properties
     * @return The registered item
     */
    private static <T extends Item> T register(String name, Function<Item.Properties, T> factory, Item.Properties settings) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, LootboundRpgMod.id(name));
        T item = factory.apply(settings.setId(itemKey));
        return Registry.register(BuiltInRegistries.ITEM, itemKey, item);
    }

    /**
     * Called from mod initializer to trigger static initialization
     * and add items to creative tabs.
     */
    public static void register() {
        LootboundRpgMod.LOGGER.info("Registering Lootbound RPG items...");

        // Add all upgrade stones to the Ingredients creative tab
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS).register(output -> {
            output.accept(CRUDE_UPGRADE_STONE);
            output.accept(REFINED_UPGRADE_STONE);
            output.accept(RARE_UPGRADE_STONE);
            output.accept(PERFECT_UPGRADE_STONE);
            output.accept(PROTECTION_STONE);
            output.accept(STABILITY_STONE);
        });
    }
}
