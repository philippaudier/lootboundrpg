package lootboundrpg.lootbound_rpg.mobpack;

import lootboundrpg.lootbound_rpg.LootboundRpgMod;
import lootboundrpg.lootbound_rpg.config.LootboundConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Monster;

import java.util.Set;

/**
 * Blocks vanilla hostile mob spawning when pack system is enabled.
 * This prevents the world from being overrun with vanilla mobs
 * while the pack system handles controlled spawning.
 */
public class VanillaSpawnBlocker {

    // Mobs that should be blocked from natural spawning
    private static final Set<String> BLOCKED_MOBS = Set.of(
            "zombie", "skeleton", "spider", "creeper",
            "husk", "stray", "drowned", "cave_spider",
            "witch", "phantom", "slime"
    );

    // Mobs that should NEVER be blocked (bosses, special mobs)
    private static final Set<String> NEVER_BLOCK = Set.of(
            "wither", "ender_dragon", "elder_guardian", "warden",
            "ravager", "evoker", "vindicator", "pillager",
            "wither_skeleton", "piglin_brute", "piglin", "hoglin",
            "blaze", "ghast", "magma_cube", "enderman", "endermite",
            "shulker", "guardian"
    );

    public static void register() {
        LootboundRpgMod.LOGGER.info("Registering Lootbound RPG vanilla spawn blocker...");

        // Hook into entity load event to remove naturally spawned hostiles
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            LootboundConfig config = LootboundConfig.get();

            // Only block if both packs and vanilla blocking are enabled
            if (!config.enableMobPacks || !config.disableVanillaHostileSpawns) {
                return;
            }

            // Only affect monsters
            if (!(entity instanceof Monster)) {
                return;
            }

            // Get entity ID
            String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();

            // Never block special mobs
            if (NEVER_BLOCK.contains(entityId)) {
                return;
            }

            // Check if this is a pack mob (has custom name with ELITE or was spawned by us)
            if (entity.hasCustomName()) {
                // Pack mobs have custom names, don't block them
                return;
            }

            // Check if this mob was spawned by a spawner or spawn egg (allow those)
            // Natural spawns don't have persistence required
            if (entity instanceof net.minecraft.world.entity.Mob mob) {
                // If persistence is required, it was likely spawned deliberately
                if (mob.isPersistenceRequired()) {
                    return;
                }
            }

            // Only block common hostile mobs
            if (BLOCKED_MOBS.contains(entityId)) {
                // Remove the entity before it fully spawns
                entity.discard();

                if (config.debugMobPackSpawning) {
                    LootboundRpgMod.LOGGER.info("[SpawnBlocker] Blocked vanilla spawn: " + entityId);
                }
            }
        });
    }
}
