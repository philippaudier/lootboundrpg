package lootboundrpg.lootbound_rpg.affix;

import net.minecraft.ChatFormatting;

/**
 * Equipment affix system for Lootbound RPG.
 *
 * Affixes provide additional stat bonuses to equipment.
 * Each affix has 3 tiers (I, II, III) with increasing power.
 *
 * Weapon affixes: SHARP, SWIFT, BONEBREAKER, HUNTER, EXECUTIONER, UNSTABLE
 * Tool affixes: MINER, DURABLE, PRECISE, PROSPECTOR, LIGHTWEIGHT, STABLE
 */
public enum EquipmentAffix {

    // ========== WEAPON AFFIXES ==========

    SHARP(
            "sharp",
            "Sharp", "Aiguisé",
            AffixCategory.WEAPON,
            AffixType.MAJOR,
            new double[]{0.08, 0.12, 0.16}, // +8% / +12% / +16% damage
            ChatFormatting.RED
    ),

    SWIFT(
            "swift",
            "Swift", "Vif",
            AffixCategory.WEAPON,
            AffixType.MINOR,
            new double[]{0.05, 0.08, 0.12}, // +5% / +8% / +12% attack speed
            ChatFormatting.YELLOW
    ),

    BONEBREAKER(
            "bonebreaker",
            "Bonebreaker", "Brise-Os",
            AffixCategory.WEAPON,
            AffixType.MAJOR,
            new double[]{0.10, 0.15, 0.20}, // +10% / +15% / +20% vs undead
            ChatFormatting.LIGHT_PURPLE
    ),

    HUNTER(
            "hunter",
            "Hunter", "Chasseur",
            AffixCategory.WEAPON,
            AffixType.MINOR,
            new double[]{0.10, 0.15, 0.20}, // +10% / +15% / +20% vs arthropods
            ChatFormatting.GREEN
    ),

    EXECUTIONER(
            "executioner",
            "Executioner", "Bourreau",
            AffixCategory.WEAPON,
            AffixType.MAJOR,
            new double[]{0.10, 0.15, 0.20}, // +10% / +15% / +20% vs <30% HP
            ChatFormatting.DARK_RED
    ),

    UNSTABLE(
            "unstable",
            "Unstable", "Instable",
            AffixCategory.WEAPON,
            AffixType.MINOR,
            new double[]{0.20, 0.20, 0.20}, // +20% damage, -10% durability (fixed)
            ChatFormatting.DARK_PURPLE
    ),

    // ========== TOOL AFFIXES ==========

    MINER(
            "miner",
            "Miner", "Mineur",
            AffixCategory.TOOL,
            AffixType.MAJOR,
            new double[]{0.08, 0.12, 0.18}, // +8% / +12% / +18% mining speed
            ChatFormatting.AQUA
    ),

    DURABLE(
            "durable",
            "Durable", "Robuste",
            AffixCategory.TOOL,
            AffixType.MINOR,
            new double[]{0.15, 0.25, 0.40}, // +15% / +25% / +40% durability
            ChatFormatting.GRAY
    ),

    PRECISE(
            "precise",
            "Precise", "Précis",
            AffixCategory.TOOL,
            AffixType.MINOR,
            new double[]{0.05, 0.08, 0.12}, // +5% / +8% / +12% no durability consume
            ChatFormatting.WHITE
    ),

    PROSPECTOR(
            "prospector",
            "Prospector", "Prospecteur",
            AffixCategory.TOOL,
            AffixType.MAJOR,
            new double[]{0.03, 0.05, 0.08}, // +3% / +5% / +8% bonus ore drop
            ChatFormatting.GOLD
    ),

    LIGHTWEIGHT(
            "lightweight",
            "Lightweight", "Léger",
            AffixCategory.TOOL,
            AffixType.MINOR,
            new double[]{0.10, 0.10, 0.10}, // +10% mining speed, -10% durability (fixed)
            ChatFormatting.WHITE
    ),

    STABLE(
            "stable",
            "Stable", "Stable",
            AffixCategory.TOOL,
            AffixType.MINOR,
            new double[]{0.05, 0.05, 0.05}, // +5% upgrade success chance (fixed)
            ChatFormatting.BLUE
    );

    private final String id;
    private final String displayNameEn;
    private final String displayNameFr;
    private final AffixCategory category;
    private final AffixType type;
    private final double[] tierValues; // Values for tier I, II, III
    private final ChatFormatting color;

    EquipmentAffix(String id, String displayNameEn, String displayNameFr,
                   AffixCategory category, AffixType type,
                   double[] tierValues, ChatFormatting color) {
        this.id = id;
        this.displayNameEn = displayNameEn;
        this.displayNameFr = displayNameFr;
        this.category = category;
        this.type = type;
        this.tierValues = tierValues;
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public String getDisplayNameEn() {
        return displayNameEn;
    }

    public String getDisplayNameFr() {
        return displayNameFr;
    }

    public AffixCategory getCategory() {
        return category;
    }

    public AffixType getType() {
        return type;
    }

    /**
     * Gets the value for a specific tier (1, 2, or 3).
     */
    public double getValue(int tier) {
        if (tier < 1) tier = 1;
        if (tier > 3) tier = 3;
        return tierValues[tier - 1];
    }

    /**
     * Gets the display percentage for a tier.
     */
    public int getDisplayPercent(int tier) {
        return (int) (getValue(tier) * 100);
    }

    public ChatFormatting getColor() {
        return color;
    }

    public String getTranslationKey() {
        return "lootbound_rpg.affix." + id;
    }

    /**
     * Get affix by ID, returns null if not found.
     */
    public static EquipmentAffix fromId(String id) {
        if (id == null) return null;
        // Handle tier suffix (e.g., "sharp_2" -> "sharp")
        String baseId = id.contains("_") ? id.substring(0, id.lastIndexOf('_')) : id;
        for (EquipmentAffix affix : values()) {
            if (affix.id.equalsIgnoreCase(baseId) || affix.id.equalsIgnoreCase(id)) {
                return affix;
            }
        }
        return null;
    }

    /**
     * Affix categories - determines which item types can have this affix.
     */
    public enum AffixCategory {
        WEAPON,  // Swords only
        TOOL     // Pickaxes only
    }

    /**
     * Affix types - determines when this affix can appear.
     * MINOR: Can appear as the minor affix slot (Uncommon+)
     * MAJOR: Can appear as the major affix slot (Rare+)
     */
    public enum AffixType {
        MINOR,
        MAJOR
    }
}
