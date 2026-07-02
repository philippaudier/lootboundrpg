package lootboundrpg.lootbound_rpg.mobpack;

import lootboundrpg.lootbound_rpg.LootboundRpgMod;
import lootboundrpg.lootbound_rpg.config.LootboundConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.*;

/**
 * Handles mob pack spawning around players.
 * Respects distance, cooldown, and cap rules from config.
 */
public class MobPackSpawner {

    private static final Random RANDOM = new Random();

    // Cooldown tracking per player UUID
    private static final Map<UUID, Long> playerCooldowns = new HashMap<>();

    // Track spawned pack mobs for global cap
    private static final Set<UUID> packMobIds = new HashSet<>();

    // Tick counter for periodic checks
    private static int tickCounter = 0;
    private static final int TICK_INTERVAL = 20; // Check every second

    public static void register() {
        LootboundRpgMod.LOGGER.info("Registering Lootbound RPG mob pack spawner...");

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!LootboundConfig.get().enableMobPacks) return;

            tickCounter++;
            if (tickCounter < TICK_INTERVAL) return;
            tickCounter = 0;

            // Clean up dead pack mobs from tracking
            cleanupDeadMobs(server.overworld());

            // Try to spawn packs for each player
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.isSpectator()) {
                    continue;
                }
                if (player.isCreative()) {
                    // Allow pack spawning in creative for testing
                    // but with reduced debug spam
                }

                trySpawnPackForPlayer(player);
            }
        });
    }

    /**
     * Attempts to spawn a pack near a player if conditions are met.
     */
    private static void trySpawnPackForPlayer(ServerPlayer player) {
        LootboundConfig config = LootboundConfig.get();
        long currentTime = System.currentTimeMillis();
        UUID playerId = player.getUUID();

        // Check cooldown
        long lastSpawn = playerCooldowns.getOrDefault(playerId, 0L);
        long cooldownMs = config.mobPackSpawnIntervalSeconds * 1000L;
        long timeRemaining = (cooldownMs - (currentTime - lastSpawn)) / 1000;
        if (currentTime - lastSpawn < cooldownMs) {
            // Only log occasionally to avoid spam
            if (timeRemaining % 30 == 0 && timeRemaining > 0) {
                debugLog("Cooldown for " + player.getName().getString() + ": " + timeRemaining + "s remaining");
            }
            return;
        }

        // Check global cap
        if (packMobIds.size() >= config.mobPackGlobalCap) {
            debugLog("Global pack mob cap reached (" + packMobIds.size() + "/" + config.mobPackGlobalCap + ")");
            return;
        }

        // Check nearby hostiles
        int nearbyHostiles = countNearbyHostiles(player, config.mobPackMinDistance);
        if (nearbyHostiles >= config.mobPackMaxHostileNearby) {
            debugLog("Too many hostiles near " + player.getName().getString() + " (" + nearbyHostiles + ")");
            return;
        }

        // Get server level
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        debugLog("Attempting pack spawn for " + player.getName().getString() +
                " at pos " + player.blockPosition());

        // Find valid spawn position
        BlockPos spawnPos = findSpawnPosition(player, serverLevel);
        if (spawnPos == null) {
            debugLog("No valid spawn position found for " + player.getName().getString() +
                    " (tried 10 positions, all failed light/terrain checks)");
            // Still reset cooldown partially to avoid spam
            playerCooldowns.put(playerId, currentTime - (cooldownMs / 2));
            return;
        }

        // Select random pack type
        MobPackType packType = MobPackType.randomPack();

        debugLog("Found valid position " + spawnPos + ", spawning " + packType.getDisplayName());

        // Spawn the pack
        int spawned = spawnPack(serverLevel, spawnPos, packType);
        if (spawned > 0) {
            playerCooldowns.put(playerId, currentTime);
            debugLog("SUCCESS: Spawned " + packType.getDisplayName() + " (" + spawned + " mobs) at " + spawnPos +
                    " for " + player.getName().getString());
        } else {
            debugLog("FAILED: Could not spawn any mobs for " + packType.getDisplayName());
        }
    }

    /**
     * Spawns a specific pack at a location.
     * @return Number of mobs spawned
     */
    public static int spawnPack(ServerLevel level, BlockPos center, MobPackType packType) {
        LootboundConfig config = LootboundConfig.get();
        int totalSpawned = 0;
        boolean eliteSpawned = false;

        // Determine if this pack gets an elite
        boolean shouldSpawnElite = RANDOM.nextDouble() < config.eliteChanceInPacks;

        for (MobPackType.MobEntry entry : packType.getComposition()) {
            int count = entry.rollCount();

            for (int i = 0; i < count; i++) {
                // Find nearby valid position
                BlockPos mobPos = findNearbySpawnPos(level, center, 5);
                if (mobPos == null) continue;

                // Spawn the mob
                Mob mob = spawnMob(level, entry.getEntityType(), mobPos);
                if (mob != null) {
                    // Make elite if conditions met
                    if (shouldSpawnElite && !eliteSpawned &&
                        entry.entityId().equals(packType.getEliteTypeId())) {
                        EliteMobFactory.makeElite(mob);
                        eliteSpawned = true;
                    }

                    // Track for global cap
                    packMobIds.add(mob.getUUID());
                    totalSpawned++;
                }
            }
        }

        return totalSpawned;
    }

    /**
     * Spawns a single mob at the given position.
     */
    private static Mob spawnMob(ServerLevel level, EntityType<?> entityType, BlockPos pos) {
        try {
            Entity entity = entityType.create(level, EntitySpawnReason.EVENT);
            if (entity instanceof Mob mob) {
                mob.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                mob.setYRot(RANDOM.nextFloat() * 360);
                level.addFreshEntity(mob);
                return mob;
            }
        } catch (Exception e) {
            LootboundRpgMod.LOGGER.warn("Failed to spawn mob: " + e.getMessage());
        }
        return null;
    }

    /**
     * Finds a valid spawn position at appropriate distance from player.
     */
    private static BlockPos findSpawnPosition(ServerPlayer player, ServerLevel level) {
        LootboundConfig config = LootboundConfig.get();
        BlockPos playerPos = player.blockPosition();

        // Try multiple times to find a valid position
        for (int attempt = 0; attempt < 10; attempt++) {
            // Random angle
            double angle = RANDOM.nextDouble() * Math.PI * 2;

            // Random distance within range
            int distance = config.mobPackMinDistance +
                          RANDOM.nextInt(config.mobPackMaxDistance - config.mobPackMinDistance);

            int x = playerPos.getX() + (int)(Math.cos(angle) * distance);
            int z = playerPos.getZ() + (int)(Math.sin(angle) * distance);

            // Find ground level
            BlockPos groundPos = findGroundLevel(level, x, z, playerPos.getY());
            if (groundPos == null) continue;

            // Validate spawn conditions
            if (isValidSpawnPos(level, groundPos, config.allowDaylightPacks)) {
                return groundPos;
            }
        }

        return null;
    }

    /**
     * Finds ground level at the given X, Z coordinates.
     */
    private static BlockPos findGroundLevel(ServerLevel level, int x, int z, int referenceY) {
        // Search from reference height downward, then upward
        for (int yOffset = 0; yOffset < 30; yOffset++) {
            // Check below
            int y = referenceY - yOffset;
            if (y > level.getMinY()) {
                BlockPos pos = new BlockPos(x, y, z);
                if (isStandablePosition(level, pos)) {
                    return pos;
                }
            }

            // Check above
            y = referenceY + yOffset;
            if (y < level.getMaxY() - 2) {
                BlockPos pos = new BlockPos(x, y, z);
                if (isStandablePosition(level, pos)) {
                    return pos;
                }
            }
        }
        return null;
    }

    /**
     * Checks if a position is standable (solid below, air at feet and head).
     */
    private static boolean isStandablePosition(ServerLevel level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        BlockState atFeet = level.getBlockState(pos);
        BlockState atHead = level.getBlockState(pos.above());

        return below.isSolid() &&
               !below.liquid() &&
               atFeet.isAir() &&
               atHead.isAir();
    }

    /**
     * Validates spawn position for packs.
     */
    private static boolean isValidSpawnPos(ServerLevel level, BlockPos pos, boolean allowDaylight) {
        // Check if in water
        if (level.getBlockState(pos).liquid()) return false;

        // Check light level (unless daylight packs allowed)
        if (!allowDaylight) {
            int lightLevel = level.getMaxLocalRawBrightness(pos);
            if (lightLevel > 7) return false;
        }

        // Basic check - position should be in loaded chunk
        if (!level.isLoaded(pos)) return false;

        return true;
    }

    /**
     * Finds a nearby valid position for individual mob placement.
     */
    private static BlockPos findNearbySpawnPos(ServerLevel level, BlockPos center, int radius) {
        for (int attempt = 0; attempt < 5; attempt++) {
            int x = center.getX() + RANDOM.nextInt(radius * 2 + 1) - radius;
            int z = center.getZ() + RANDOM.nextInt(radius * 2 + 1) - radius;

            BlockPos groundPos = findGroundLevel(level, x, z, center.getY());
            if (groundPos != null && isStandablePosition(level, groundPos)) {
                return groundPos;
            }
        }
        return center; // Fallback to center
    }

    /**
     * Counts hostile mobs near the player.
     */
    private static int countNearbyHostiles(ServerPlayer player, int radius) {
        AABB area = player.getBoundingBox().inflate(radius);
        return (int) player.level().getEntitiesOfClass(Monster.class, area).stream().count();
    }

    /**
     * Removes dead mobs from tracking.
     */
    private static void cleanupDeadMobs(ServerLevel level) {
        packMobIds.removeIf(uuid -> {
            Entity entity = level.getEntity(uuid);
            return entity == null || !entity.isAlive();
        });
    }

    /**
     * Debug logging helper.
     */
    private static void debugLog(String message) {
        if (LootboundConfig.get().debugMobPackSpawning) {
            LootboundRpgMod.LOGGER.info("[MobPack] " + message);
        }
    }

    /**
     * Gets the current pack mob count for debug display.
     */
    public static int getPackMobCount() {
        return packMobIds.size();
    }

    /**
     * Clears all pack mob tracking (for debug/reset).
     */
    public static void clearTracking() {
        packMobIds.clear();
        playerCooldowns.clear();
    }
}
