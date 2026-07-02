package lootboundrpg.lootbound_rpg.loot;

import lootboundrpg.lootbound_rpg.LootboundRpgMod;
import lootboundrpg.lootbound_rpg.config.LootboundConfig;
import lootboundrpg.lootbound_rpg.item.ModItems;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * Modifies vanilla mob loot tables to drop upgrade stones.
 *
 * Drop rates (V1):
 * - Common mobs (zombie, skeleton, spider): 5% crude stone
 * - Dangerous mobs (enderman, witch, etc.): 8% crude, 3% refined
 * - Boss-tier (wither skeleton, etc.): 10% crude, 5% refined, 1% rare
 */
public class ModLootTables {

    public static void register() {
        LootboundRpgMod.LOGGER.info("Registering Lootbound RPG loot table modifications...");

        LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
            // Check if stone drops are enabled
            if (!LootboundConfig.get().enableMobStoneDrops) {
                return;
            }

            // Only modify vanilla loot tables
            if (!source.isBuiltin()) {
                return;
            }

            String path = key.identifier().getPath();

            // Common overworld mobs - crude stones only
            if (isEntityLoot(path, "zombie") ||
                isEntityLoot(path, "skeleton") ||
                isEntityLoot(path, "spider")) {
                addStoneDrop(tableBuilder, ModItems.CRUDE_UPGRADE_STONE, 0.05f); // 5%
            }

            // Cave spider - slightly higher chance
            if (isEntityLoot(path, "cave_spider")) {
                addStoneDrop(tableBuilder, ModItems.CRUDE_UPGRADE_STONE, 0.07f); // 7%
            }

            // Creeper - crude stones
            if (isEntityLoot(path, "creeper")) {
                addStoneDrop(tableBuilder, ModItems.CRUDE_UPGRADE_STONE, 0.06f); // 6%
            }

            // Dangerous mobs - crude + chance for refined
            if (isEntityLoot(path, "enderman")) {
                addStoneDrop(tableBuilder, ModItems.CRUDE_UPGRADE_STONE, 0.10f);   // 10%
                addStoneDrop(tableBuilder, ModItems.REFINED_UPGRADE_STONE, 0.04f); // 4%
            }

            if (isEntityLoot(path, "witch")) {
                addStoneDrop(tableBuilder, ModItems.CRUDE_UPGRADE_STONE, 0.08f);   // 8%
                addStoneDrop(tableBuilder, ModItems.REFINED_UPGRADE_STONE, 0.03f); // 3%
            }

            // Nether mobs - higher tier stones
            if (isEntityLoot(path, "blaze")) {
                addStoneDrop(tableBuilder, ModItems.REFINED_UPGRADE_STONE, 0.08f); // 8%
                addStoneDrop(tableBuilder, ModItems.RARE_UPGRADE_STONE, 0.02f);    // 2%
            }

            if (isEntityLoot(path, "wither_skeleton")) {
                addStoneDrop(tableBuilder, ModItems.REFINED_UPGRADE_STONE, 0.10f); // 10%
                addStoneDrop(tableBuilder, ModItems.RARE_UPGRADE_STONE, 0.03f);    // 3%
            }

            if (isEntityLoot(path, "piglin_brute")) {
                addStoneDrop(tableBuilder, ModItems.REFINED_UPGRADE_STONE, 0.12f); // 12%
                addStoneDrop(tableBuilder, ModItems.RARE_UPGRADE_STONE, 0.04f);    // 4%
            }
        });
    }

    /**
     * Checks if the loot table path matches an entity loot table.
     * Entity loot tables follow the pattern "entities/{entity_name}".
     */
    private static boolean isEntityLoot(String path, String entityName) {
        return path.equals("entities/" + entityName);
    }

    /**
     * Adds a stone drop with the specified chance to the loot table.
     * Applies the global drop rate multiplier from config.
     */
    private static void addStoneDrop(LootTable.Builder tableBuilder, Item stone, float chance) {
        // Apply config multiplier
        float adjustedChance = LootboundConfig.get().applyDropMultiplier(chance);

        LootPool.Builder pool = LootPool.lootPool()
                .setRolls(ConstantValue.exactly(1))
                .when(LootItemRandomChanceCondition.randomChance(adjustedChance))
                .add(LootItem.lootTableItem(stone));

        tableBuilder.withPool(pool);
    }
}
