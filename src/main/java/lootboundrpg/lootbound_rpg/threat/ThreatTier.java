package lootboundrpg.lootbound_rpg.threat;

import net.minecraft.ChatFormatting;

/**
 * Threat tiers for different areas of the world.
 * Affects pack types, spawn rates, and loot quality.
 */
public enum ThreatTier {
    SAFE(0, "Safe", ChatFormatting.GREEN),
    COMMON(1, "Common", ChatFormatting.WHITE),
    DANGEROUS(2, "Dangerous", ChatFormatting.YELLOW),
    ELITE(3, "Elite", ChatFormatting.RED),
    NIGHTMARE(4, "Nightmare", ChatFormatting.DARK_PURPLE);

    private final int level;
    private final String displayName;
    private final ChatFormatting color;

    ThreatTier(int level, String displayName, ChatFormatting color) {
        this.level = level;
        this.displayName = displayName;
        this.color = color;
    }

    public int getLevel() {
        return level;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ChatFormatting getColor() {
        return color;
    }

    /**
     * Returns true if this tier is at least as dangerous as the other.
     */
    public boolean isAtLeast(ThreatTier other) {
        return this.level >= other.level;
    }
}
