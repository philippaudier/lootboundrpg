package lootboundrpg.lootbound_rpg.upgrade;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * Equipment grade/rarity system for Lootbound RPG.
 *
 * Grades add a multiplier to the base upgrade bonuses:
 * - COMMON: 0% bonus (1.0x multiplier)
 * - UNCOMMON: +5% bonus (1.05x multiplier)
 * - RARE: +10% bonus (1.10x multiplier)
 * - EPIC: +20% bonus (1.20x multiplier)
 * - LEGENDARY: +35% bonus (1.35x multiplier)
 */
public enum EquipmentGrade {
    COMMON(0, "common", "Common", "Commun", ChatFormatting.WHITE, 1.0),
    UNCOMMON(1, "uncommon", "Uncommon", "Peu commun", ChatFormatting.GREEN, 1.05),
    RARE(2, "rare", "Rare", "Rare", ChatFormatting.BLUE, 1.10),
    EPIC(3, "epic", "Epic", "Épique", ChatFormatting.LIGHT_PURPLE, 1.20),
    LEGENDARY(4, "legendary", "Legendary", "Légendaire", ChatFormatting.GOLD, 1.35);

    private final int id;
    private final String name;
    private final String displayNameEn;
    private final String displayNameFr;
    private final ChatFormatting color;
    private final double bonusMultiplier;

    EquipmentGrade(int id, String name, String displayNameEn, String displayNameFr,
                   ChatFormatting color, double bonusMultiplier) {
        this.id = id;
        this.name = name;
        this.displayNameEn = displayNameEn;
        this.displayNameFr = displayNameFr;
        this.color = color;
        this.bonusMultiplier = bonusMultiplier;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ChatFormatting getColor() {
        return color;
    }

    /**
     * Returns the multiplier applied to upgrade bonuses.
     * A Legendary +10 sword would have: baseDamageBonus * 1.35
     */
    public double getBonusMultiplier() {
        return bonusMultiplier;
    }

    /**
     * Returns the bonus percentage for display (e.g., "+5%" for UNCOMMON).
     */
    public int getBonusPercent() {
        return (int) ((bonusMultiplier - 1.0) * 100);
    }

    /**
     * Returns the translation key for this grade.
     */
    public String getTranslationKey() {
        return "lootbound_rpg.grade." + name;
    }

    /**
     * Returns a colored component for display.
     */
    public MutableComponent getDisplayComponent() {
        return Component.translatable(getTranslationKey()).withStyle(color);
    }

    /**
     * Get grade by ID, defaults to COMMON if invalid.
     */
    public static EquipmentGrade fromId(int id) {
        for (EquipmentGrade grade : values()) {
            if (grade.id == id) {
                return grade;
            }
        }
        return COMMON;
    }

    /**
     * Get grade by name, defaults to COMMON if invalid.
     */
    public static EquipmentGrade fromName(String name) {
        for (EquipmentGrade grade : values()) {
            if (grade.name.equalsIgnoreCase(name)) {
                return grade;
            }
        }
        return COMMON;
    }
}
