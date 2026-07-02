package lootboundrpg.lootbound_rpg.mobpack;

import lootboundrpg.lootbound_rpg.threat.ThreatTier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Defines the different types of mob packs that can spawn.
 * Organized by threat tier for progressive difficulty.
 *
 * COMMON (100+ blocks): Basic zombies, skeletons
 * DANGEROUS (400+ blocks): Spiders, creepers, witches
 * ELITE (1000+ blocks): Pillagers, cave spiders, mixed groups
 * NIGHTMARE (2500+ blocks): Endermen, ravagers, wardens
 */
public enum MobPackType {

    // ========== COMMON TIER (Early game, basic grinding) ==========

    ZOMBIE_HORDE(
            "Zombie Horde",
            MobPackTier.COMMON,
            ThreatTier.COMMON,
            List.of(
                    new MobEntry("zombie", 4, 6)
            ),
            "zombie"
    ),

    SKELETON_PATROL(
            "Skeleton Patrol",
            MobPackTier.COMMON,
            ThreatTier.COMMON,
            List.of(
                    new MobEntry("skeleton", 3, 5)
            ),
            "skeleton"
    ),

    HUSK_WANDERERS(
            "Husk Wanderers",
            MobPackTier.COMMON,
            ThreatTier.COMMON,
            List.of(
                    new MobEntry("husk", 3, 5)
            ),
            "husk"
    ),

    STRAY_HUNTERS(
            "Stray Hunters",
            MobPackTier.COMMON,
            ThreatTier.COMMON,
            List.of(
                    new MobEntry("stray", 3, 4)
            ),
            "stray"
    ),

    // ========== DANGEROUS TIER (Mid game, challenging) ==========

    SPIDER_NEST(
            "Spider Nest",
            MobPackTier.DANGEROUS,
            ThreatTier.DANGEROUS,
            List.of(
                    new MobEntry("spider", 4, 6)
            ),
            "spider"
    ),

    CREEPER_GANG(
            "Creeper Gang",
            MobPackTier.DANGEROUS,
            ThreatTier.DANGEROUS,
            List.of(
                    new MobEntry("creeper", 2, 4)
            ),
            "creeper"
    ),

    DROWNED_TIDE(
            "Drowned Tide",
            MobPackTier.DANGEROUS,
            ThreatTier.DANGEROUS,
            List.of(
                    new MobEntry("drowned", 4, 6)
            ),
            "drowned"
    ),

    WITCH_COVEN(
            "Witch Coven",
            MobPackTier.DANGEROUS,
            ThreatTier.DANGEROUS,
            List.of(
                    new MobEntry("witch", 2, 3),
                    new MobEntry("zombie", 1, 2)
            ),
            "witch"
    ),

    SLIME_SWARM(
            "Slime Swarm",
            MobPackTier.DANGEROUS,
            ThreatTier.DANGEROUS,
            List.of(
                    new MobEntry("slime", 3, 5)
            ),
            "slime"
    ),

    UNDEAD_LEGION(
            "Undead Legion",
            MobPackTier.DANGEROUS,
            ThreatTier.DANGEROUS,
            List.of(
                    new MobEntry("zombie", 2, 3),
                    new MobEntry("skeleton", 2, 3),
                    new MobEntry("zombie_villager", 0, 1)
            ),
            "zombie"
    ),

    // ========== ELITE TIER (Late game, high reward) ==========

    CAVE_SPIDER_SWARM(
            "Cave Spider Swarm",
            MobPackTier.ELITE,
            ThreatTier.ELITE,
            List.of(
                    new MobEntry("cave_spider", 5, 8)
            ),
            "cave_spider"
    ),

    PILLAGER_SQUAD(
            "Pillager Squad",
            MobPackTier.ELITE,
            ThreatTier.ELITE,
            List.of(
                    new MobEntry("pillager", 3, 5),
                    new MobEntry("vindicator", 1, 2)
            ),
            "pillager"
    ),

    PHANTOM_FLOCK(
            "Phantom Flock",
            MobPackTier.ELITE,
            ThreatTier.ELITE,
            List.of(
                    new MobEntry("phantom", 3, 5)
            ),
            "phantom"
    ),

    ELITE_AMBUSH(
            "Elite Ambush",
            MobPackTier.ELITE,
            ThreatTier.ELITE,
            List.of(
                    new MobEntry("zombie", 2, 3),
                    new MobEntry("skeleton", 2, 3),
                    new MobEntry("spider", 1, 2),
                    new MobEntry("creeper", 1, 1)
            ),
            "zombie"
    ),

    EVOKER_CIRCLE(
            "Evoker Circle",
            MobPackTier.ELITE,
            ThreatTier.ELITE,
            List.of(
                    new MobEntry("evoker", 1, 2),
                    new MobEntry("vindicator", 2, 3)
            ),
            "evoker"
    ),

    // ========== NIGHTMARE TIER (End game, extreme challenge) ==========

    ENDERMAN_CONVERGENCE(
            "Enderman Convergence",
            MobPackTier.ELITE,
            ThreatTier.NIGHTMARE,
            List.of(
                    new MobEntry("enderman", 3, 5)
            ),
            "enderman"
    ),

    RAVAGER_ASSAULT(
            "Ravager Assault",
            MobPackTier.ELITE,
            ThreatTier.NIGHTMARE,
            List.of(
                    new MobEntry("ravager", 1, 2),
                    new MobEntry("pillager", 2, 4)
            ),
            "ravager"
    ),

    NIGHTMARE_HORDE(
            "Nightmare Horde",
            MobPackTier.ELITE,
            ThreatTier.NIGHTMARE,
            List.of(
                    new MobEntry("vindicator", 2, 3),
                    new MobEntry("evoker", 1, 2),
                    new MobEntry("witch", 1, 2),
                    new MobEntry("pillager", 2, 3)
            ),
            "vindicator"
    ),

    WARDEN_DOMAIN(
            "Warden's Domain",
            MobPackTier.ELITE,
            ThreatTier.NIGHTMARE,
            List.of(
                    new MobEntry("warden", 1, 1)
            ),
            "warden"
    );

    private static final Random RANDOM = new Random();

    private final String displayName;
    private final MobPackTier tier;
    private final ThreatTier minThreatTier;
    private final List<MobEntry> composition;
    private final String eliteTypeId;

    MobPackType(String displayName, MobPackTier tier, ThreatTier minThreatTier,
                List<MobEntry> composition, String eliteTypeId) {
        this.displayName = displayName;
        this.tier = tier;
        this.minThreatTier = minThreatTier;
        this.composition = composition;
        this.eliteTypeId = eliteTypeId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MobPackTier getTier() {
        return tier;
    }

    public ThreatTier getMinThreatTier() {
        return minThreatTier;
    }

    public List<MobEntry> getComposition() {
        return composition;
    }

    public String getEliteTypeId() {
        return eliteTypeId;
    }

    /**
     * Checks if this pack can spawn in the given threat tier.
     */
    public boolean canSpawnInThreat(ThreatTier threat) {
        return threat.isAtLeast(minThreatTier);
    }

    /**
     * Selects a random pack type for spawning (ignores threat tier).
     */
    public static MobPackType randomPack() {
        MobPackType[] values = values();
        return values[RANDOM.nextInt(values.length)];
    }

    /**
     * Selects a random pack type that can spawn in the given threat tier.
     * Prefers packs matching the current tier over lower tier packs.
     * Returns null if no packs can spawn.
     */
    public static MobPackType randomPackForThreat(ThreatTier threat) {
        List<MobPackType> matchingTier = new ArrayList<>();
        List<MobPackType> lowerTier = new ArrayList<>();

        for (MobPackType type : values()) {
            if (type.minThreatTier == threat) {
                matchingTier.add(type);
            } else if (type.canSpawnInThreat(threat) && type.minThreatTier.getLevel() < threat.getLevel()) {
                lowerTier.add(type);
            }
        }

        // 70% chance to spawn matching tier, 30% lower tier
        if (!matchingTier.isEmpty() && (lowerTier.isEmpty() || RANDOM.nextDouble() < 0.7)) {
            return matchingTier.get(RANDOM.nextInt(matchingTier.size()));
        } else if (!lowerTier.isEmpty()) {
            return lowerTier.get(RANDOM.nextInt(lowerTier.size()));
        } else if (!matchingTier.isEmpty()) {
            return matchingTier.get(RANDOM.nextInt(matchingTier.size()));
        }

        return null;
    }

    /**
     * Gets a pack type by name (case insensitive).
     */
    public static MobPackType fromName(String name) {
        for (MobPackType type : values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Represents a mob entry in a pack with min/max count.
     * Uses string IDs since MC 26.2 doesn't have EntityType constants.
     */
    public record MobEntry(String entityId, int minCount, int maxCount) {
        private static final Random RANDOM = new Random();

        public int rollCount() {
            if (minCount >= maxCount) return minCount;
            return minCount + RANDOM.nextInt(maxCount - minCount + 1);
        }

        /**
         * Gets the EntityType from the registry.
         */
        @SuppressWarnings("unchecked")
        public EntityType<?> getEntityType() {
            return BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.fromNamespaceAndPath("minecraft", entityId));
        }
    }
}
