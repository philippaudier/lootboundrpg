package lootboundrpg.lootbound_rpg.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lootboundrpg.lootbound_rpg.LootboundRpgMod;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Configuration system for Lootbound RPG.
 *
 * Config file: config/lootbound-rpg.json
 * Loaded at startup with default values if absent.
 */
public class LootboundConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_FILE = "lootbound-rpg.json";

    private static LootboundConfig INSTANCE;

    // === Feature Toggles ===

    /** Enable upgrade stone drops from mobs */
    public boolean enableMobStoneDrops = true;

    /** Enable graded equipment drops from mobs */
    public boolean enableEquipmentDrops = true;

    /** Upgrades consume XP (false = free upgrades) */
    public boolean upgradeConsumesXp = true;

    // === Drop Rate Multipliers ===

    /** Global multiplier for all drop rates (1.0 = normal, 2.0 = double) */
    public double globalDropRateMultiplier = 1.0;

    /** Multiplier for Legendary grade drop chance */
    public double legendaryDropRateMultiplier = 1.0;

    // === Upgrade Chance Multipliers ===

    /** Global multiplier for upgrade success chances (1.0 = normal) */
    public double globalUpgradeChanceMultiplier = 1.0;

    // === Grade Drop Weights (Common mobs) ===
    public int commonMobGradeCommon = 70;
    public int commonMobGradeUncommon = 25;
    public int commonMobGradeRare = 4;
    public int commonMobGradeEpic = 1;
    public int commonMobGradeLegendary = 0;

    // === Grade Drop Weights (Dangerous mobs) ===
    public int dangerousMobGradeCommon = 30;
    public int dangerousMobGradeUncommon = 40;
    public int dangerousMobGradeRare = 20;
    public int dangerousMobGradeEpic = 8;
    public int dangerousMobGradeLegendary = 2;

    // === Grade Drop Weights (Elite mobs) ===
    public int eliteMobGradeCommon = 10;
    public int eliteMobGradeUncommon = 25;
    public int eliteMobGradeRare = 35;
    public int eliteMobGradeEpic = 20;
    public int eliteMobGradeLegendary = 10;

    // === Mob Packs ===

    /** Enable mob pack spawning system */
    public boolean enableMobPacks = true;

    /** Interval between pack spawn attempts per player (seconds) */
    public int mobPackSpawnIntervalSeconds = 90;

    /** Minimum distance from player for pack spawns */
    public int mobPackMinDistance = 32;

    /** Maximum distance from player for pack spawns */
    public int mobPackMaxDistance = 96;

    /** Maximum hostile mobs nearby before blocking pack spawns */
    public int mobPackMaxHostileNearby = 12;

    /** Global cap on total pack mobs in the world */
    public int mobPackGlobalCap = 40;

    /** Allow packs to spawn during daytime in lit areas */
    public boolean allowDaylightPacks = true;

    /** Chance for elite mobs to spawn in packs (0.0 to 1.0) */
    public double eliteChanceInPacks = 0.08;

    /** Debug logging for mob pack spawning */
    public boolean debugMobPackSpawning = false;

    /** Disable vanilla hostile mob spawning (zombies, skeletons, etc.) when packs are enabled */
    public boolean disableVanillaHostileSpawns = false;

    // === Threat Zones ===

    /** Radius around world spawn where no hostile packs spawn (SAFE zone) */
    public int safeZoneRadiusFromWorldSpawn = 64;

    /** Pack spawn rate multiplier during daylight (0.0 = no daylight spawns, 1.0 = normal) */
    public double daylightPackSpawnMultiplier = 0.5;

    /** Pack spawn rate multiplier in caves/underground (1.0 = normal, 2.0 = double) */
    public double cavePackSpawnMultiplier = 1.5;

    /** Maximum concurrent packs per player */
    public int maxPacksPerPlayer = 3;

    /** Distance at which pack mobs despawn from their spawn point */
    public int packDespawnDistance = 128;

    /** Maximum distance pack mobs can wander from leader (leash radius) */
    public int packLeashRadius = 10;

    // === Debug ===

    /** Log threat tier calculations */
    public boolean debugThreatTier = false;

    /** Enable debug logging for drops and upgrades */
    public boolean debugLogging = false;

    // === Static Access ===

    public static LootboundConfig get() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static void reload() {
        INSTANCE = load();
        LootboundRpgMod.LOGGER.info("Lootbound RPG config reloaded");
    }

    private static LootboundConfig load() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve(CONFIG_FILE);

        if (Files.exists(configFile)) {
            try (Reader reader = Files.newBufferedReader(configFile)) {
                LootboundConfig config = GSON.fromJson(reader, LootboundConfig.class);
                if (config != null) {
                    LootboundRpgMod.LOGGER.info("Loaded Lootbound RPG config from {}", configFile);
                    // Re-save to add any new fields
                    config.save();
                    return config;
                }
            } catch (IOException e) {
                LootboundRpgMod.LOGGER.error("Failed to load config, using defaults", e);
            }
        }

        // Create default config
        LootboundConfig config = new LootboundConfig();
        config.save();
        LootboundRpgMod.LOGGER.info("Created default Lootbound RPG config at {}", configFile);
        return config;
    }

    public void save() {
        Path configDir = FabricLoader.getInstance().getConfigDir();
        Path configFile = configDir.resolve(CONFIG_FILE);

        try {
            Files.createDirectories(configDir);
            try (Writer writer = Files.newBufferedWriter(configFile)) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            LootboundRpgMod.LOGGER.error("Failed to save config", e);
        }
    }

    // === Helper Methods ===

    /**
     * Applies drop rate multipliers to a base chance.
     */
    public float applyDropMultiplier(float baseChance) {
        return (float) (baseChance * globalDropRateMultiplier);
    }

    /**
     * Applies upgrade chance multipliers to a base chance.
     * Capped at 100%.
     */
    public double applyUpgradeMultiplier(double baseChance) {
        return Math.min(1.0, baseChance * globalUpgradeChanceMultiplier);
    }

    /**
     * Debug log helper - only logs if debugLogging is enabled.
     */
    public void debug(String message, Object... args) {
        if (debugLogging) {
            LootboundRpgMod.LOGGER.info("[DEBUG] " + message, args);
        }
    }
}
