package lootboundrpg.lootbound_rpg.threat;

import lootboundrpg.lootbound_rpg.LootboundRpgMod;
import lootboundrpg.lootbound_rpg.config.LootboundConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

/**
 * Calculates the threat tier for a given position in the world.
 *
 * Factors:
 * - Distance from world spawn (SAFE zone)
 * - Dimension (Nether/End = higher threat)
 * - Y level (underground = higher threat)
 * - Day/night cycle
 */
public class ThreatCalculator {

    // Y level thresholds
    private static final int SURFACE_Y = 63;
    private static final int CAVE_Y = 40;
    private static final int DEEP_CAVE_Y = 0;

    // Distance thresholds from spawn (in blocks)
    private static final int COMMON_DISTANCE = 100;      // 100+ blocks = COMMON
    private static final int DANGEROUS_DISTANCE = 400;   // 400+ blocks = DANGEROUS
    private static final int ELITE_DISTANCE = 1000;      // 1000+ blocks = ELITE
    private static final int NIGHTMARE_DISTANCE = 2500;  // 2500+ blocks = NIGHTMARE

    /**
     * Calculates the threat tier at the given position.
     */
    public static ThreatTier calculate(ServerLevel level, BlockPos pos) {
        LootboundConfig config = LootboundConfig.get();

        // Get world spawn (use 0,0 as fallback for non-overworld)
        BlockPos spawnPos = getWorldSpawn(level);
        double distanceFromSpawn = Math.sqrt(
            Math.pow(pos.getX() - spawnPos.getX(), 2) +
            Math.pow(pos.getZ() - spawnPos.getZ(), 2)
        );

        // Check SAFE zone
        if (distanceFromSpawn <= config.safeZoneRadiusFromWorldSpawn) {
            debugLog(config, pos, ThreatTier.SAFE, "Within safe zone radius");
            return ThreatTier.SAFE;
        }

        // Check dimension
        if (level.dimension() == Level.NETHER || level.dimension() == Level.END) {
            debugLog(config, pos, ThreatTier.DANGEROUS, "In Nether/End dimension");
            return ThreatTier.DANGEROUS;
        }

        // Calculate base tier from distance
        ThreatTier baseTier;
        if (distanceFromSpawn >= NIGHTMARE_DISTANCE) {
            baseTier = ThreatTier.NIGHTMARE;
        } else if (distanceFromSpawn >= ELITE_DISTANCE) {
            baseTier = ThreatTier.ELITE;
        } else if (distanceFromSpawn >= DANGEROUS_DISTANCE) {
            baseTier = ThreatTier.DANGEROUS;
        } else if (distanceFromSpawn >= COMMON_DISTANCE) {
            baseTier = ThreatTier.COMMON;
        } else {
            baseTier = ThreatTier.COMMON;
        }

        // Modify based on Y level (underground increases threat)
        int y = pos.getY();
        if (y < DEEP_CAVE_Y) {
            // Deep caves - maximum threat
            baseTier = upgradeTier(baseTier);
            debugLog(config, pos, baseTier, "Deep cave (Y < 0)");
        } else if (y < CAVE_Y) {
            // Regular caves - increase threat if not already high
            if (baseTier.getLevel() < ThreatTier.DANGEROUS.getLevel()) {
                baseTier = upgradeTier(baseTier);
            }
            debugLog(config, pos, baseTier, "Cave (Y < 40)");
        }

        // Night time increases threat (only on surface)
        if (y >= SURFACE_Y && isNight(level)) {
            if (baseTier == ThreatTier.COMMON) {
                baseTier = ThreatTier.DANGEROUS;
                debugLog(config, pos, baseTier, "Night time on surface");
            }
        }

        debugLog(config, pos, baseTier,
            String.format("dist=%.0f, y=%d, night=%s", distanceFromSpawn, y, isNight(level)));

        return baseTier;
    }

    /**
     * Gets the world spawn position.
     * Uses origin (0,0) as the reference spawn point.
     */
    private static BlockPos getWorldSpawn(ServerLevel level) {
        // In most worlds, spawn is near origin
        // Using 0,0 as a simple and reliable reference point
        return BlockPos.ZERO;
    }

    /**
     * Upgrades a threat tier by one level (capped at NIGHTMARE).
     */
    private static ThreatTier upgradeTier(ThreatTier tier) {
        return switch (tier) {
            case SAFE -> ThreatTier.COMMON;
            case COMMON -> ThreatTier.DANGEROUS;
            case DANGEROUS -> ThreatTier.ELITE;
            case ELITE, NIGHTMARE -> ThreatTier.NIGHTMARE;
        };
    }

    /**
     * Returns true if it's night time in the level.
     * Night is approximately from tick 13000 to 23000.
     */
    public static boolean isNight(ServerLevel level) {
        // Sky light level is low at night
        // Use a simple check based on sky darkness
        return level.getSkyDarken() > 4;
    }

    /**
     * Returns true if the position is underground (no sky access).
     */
    public static boolean isUnderground(ServerLevel level, BlockPos pos) {
        return !level.canSeeSky(pos);
    }

    /**
     * Gets the spawn rate multiplier based on conditions.
     */
    public static double getSpawnMultiplier(ServerLevel level, BlockPos pos) {
        LootboundConfig config = LootboundConfig.get();
        double multiplier = 1.0;

        // Daylight reduction
        if (!isNight(level) && level.canSeeSky(pos)) {
            multiplier *= config.daylightPackSpawnMultiplier;
        }

        // Cave bonus
        if (isUnderground(level, pos)) {
            multiplier *= config.cavePackSpawnMultiplier;
        }

        return multiplier;
    }

    /**
     * Debug logging helper.
     */
    private static void debugLog(LootboundConfig config, BlockPos pos, ThreatTier tier, String reason) {
        if (config.debugThreatTier) {
            LootboundRpgMod.LOGGER.info("[ThreatTier] {} at {} - {}",
                tier.getDisplayName(), pos.toShortString(), reason);
        }
    }
}
