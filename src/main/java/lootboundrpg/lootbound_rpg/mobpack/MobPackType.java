package lootboundrpg.lootbound_rpg.mobpack;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.util.List;
import java.util.Random;

/**
 * Defines the different types of mob packs that can spawn.
 * Each pack has a composition and tier.
 */
public enum MobPackType {
    ZOMBIE_PACK(
            "Zombie Pack",
            MobPackTier.COMMON,
            List.of(
                    new MobEntry("zombie", 3, 5)
            ),
            "zombie"  // Elite type
    ),

    SKELETON_PATROL(
            "Skeleton Patrol",
            MobPackTier.DANGEROUS,
            List.of(
                    new MobEntry("skeleton", 2, 3),
                    new MobEntry("zombie", 0, 1)  // Chance of a tank zombie
            ),
            "skeleton"  // Elite type
    ),

    SPIDER_NEST(
            "Spider Nest",
            MobPackTier.DANGEROUS,
            List.of(
                    new MobEntry("spider", 3, 3)
            ),
            "spider"  // Elite type
    ),

    MIXED_AMBUSH(
            "Mixed Ambush",
            MobPackTier.ELITE,
            List.of(
                    new MobEntry("zombie", 2, 2),
                    new MobEntry("skeleton", 1, 1),
                    new MobEntry("spider", 1, 1)
            ),
            "zombie"  // Elite type (random selection)
    );

    private static final Random RANDOM = new Random();

    private final String displayName;
    private final MobPackTier tier;
    private final List<MobEntry> composition;
    private final String eliteTypeId;

    MobPackType(String displayName, MobPackTier tier, List<MobEntry> composition, String eliteTypeId) {
        this.displayName = displayName;
        this.tier = tier;
        this.composition = composition;
        this.eliteTypeId = eliteTypeId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public MobPackTier getTier() {
        return tier;
    }

    public List<MobEntry> getComposition() {
        return composition;
    }

    public String getEliteTypeId() {
        return eliteTypeId;
    }

    /**
     * Selects a random pack type for spawning.
     */
    public static MobPackType randomPack() {
        MobPackType[] values = values();
        return values[RANDOM.nextInt(values.length)];
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
