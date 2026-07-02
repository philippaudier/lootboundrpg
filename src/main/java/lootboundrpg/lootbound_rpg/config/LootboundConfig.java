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

    // === Debug ===

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
