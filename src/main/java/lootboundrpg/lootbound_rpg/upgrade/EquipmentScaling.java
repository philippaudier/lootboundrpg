package lootboundrpg.lootbound_rpg.upgrade;

import net.minecraft.world.item.ItemStack;

/**
 * Defines stat bonuses for each equipment level and grade.
 *
 * Scaling philosophy:
 * - Early levels (+1 to +3): Small but noticeable improvements
 * - Mid levels (+4 to +6): Significant upgrades, worth the risk
 * - Late levels (+7 to +9): Major power spikes
 * - Max level (+10): Legendary tier, very powerful
 *
 * Grade multipliers:
 * - COMMON: 1.0x (no bonus)
 * - UNCOMMON: 1.05x (+5%)
 * - RARE: 1.10x (+10%)
 * - EPIC: 1.20x (+20%)
 * - LEGENDARY: 1.35x (+35%)
 *
 * Damage bonus is additive to base weapon damage.
 * Mining speed bonus is multiplicative (1.0 = no change, 1.5 = 50% faster).
 */
public class EquipmentScaling {

    /**
     * Base attack damage bonus per level (before grade multiplier).
     */
    public static double getBaseAttackDamageBonus(int level) {
        return switch (level) {
            case 0 -> 0.0;
            case 1 -> 0.5;   // +0.5 damage
            case 2 -> 1.0;   // +1.0 damage
            case 3 -> 1.5;   // +1.5 damage
            case 4 -> 2.0;   // +2.0 damage
            case 5 -> 2.5;   // +2.5 damage
            case 6 -> 3.0;   // +3.0 damage
            case 7 -> 3.5;   // +3.5 damage
            case 8 -> 4.0;   // +4.0 damage
            case 9 -> 4.5;   // +4.5 damage
            case 10 -> 5.0;  // +5.0 damage
            default -> 0.0;
        };
    }

    /**
     * Extra attack damage per level for swords, including grade multiplier.
     * Base iron sword = 6 damage, so Legendary +10 would be 6 + (5 * 1.35) = 12.75 damage.
     */
    public static double getAttackDamageBonus(int level) {
        return getBaseAttackDamageBonus(level);
    }

    /**
     * Extra attack damage with grade multiplier applied.
     */
    public static double getAttackDamageBonus(int level, EquipmentGrade grade) {
        return getBaseAttackDamageBonus(level) * grade.getBonusMultiplier();
    }

    /**
     * Convenience method to get attack damage bonus from an ItemStack.
     */
    public static double getAttackDamageBonus(ItemStack stack) {
        int level = UpgradeSystem.getLevel(stack);
        EquipmentGrade grade = UpgradeSystem.getGrade(stack);
        return getAttackDamageBonus(level, grade);
    }

    /**
     * Base mining speed multiplier per level (before grade bonus).
     */
    public static double getBaseMiningSpeedMultiplier(int level) {
        return switch (level) {
            case 0 -> 1.0;
            case 1 -> 1.1;   // 10% faster
            case 2 -> 1.2;   // 20% faster
            case 3 -> 1.3;   // 30% faster
            case 4 -> 1.4;   // 40% faster
            case 5 -> 1.5;   // 50% faster
            case 6 -> 1.6;   // 60% faster
            case 7 -> 1.75;  // 75% faster
            case 8 -> 1.9;   // 90% faster
            case 9 -> 2.1;   // 110% faster
            case 10 -> 2.5;  // 150% faster
            default -> 1.0;
        };
    }

    /**
     * Mining speed multiplier per level for pickaxes.
     * 1.0 = normal speed, 2.0 = twice as fast.
     */
    public static double getMiningSpeedMultiplier(int level) {
        return getBaseMiningSpeedMultiplier(level);
    }

    /**
     * Mining speed multiplier with grade bonus applied.
     * Grade bonus applies to the speed increase portion only.
     * Example: Level 5 = 1.5 (50% faster), Epic = 1.20x
     * Result: 1.0 + (0.5 * 1.20) = 1.6 (60% faster)
     */
    public static double getMiningSpeedMultiplier(int level, EquipmentGrade grade) {
        double baseMultiplier = getBaseMiningSpeedMultiplier(level);
        double speedIncrease = baseMultiplier - 1.0;
        return 1.0 + (speedIncrease * grade.getBonusMultiplier());
    }

    /**
     * Convenience method to get mining speed multiplier from an ItemStack.
     */
    public static double getMiningSpeedMultiplier(ItemStack stack) {
        int level = UpgradeSystem.getLevel(stack);
        EquipmentGrade grade = UpgradeSystem.getGrade(stack);
        return getMiningSpeedMultiplier(level, grade);
    }

    /**
     * Attack speed bonus per level (for future use).
     * Slight increase to make combat feel smoother at higher levels.
     */
    public static double getAttackSpeedBonus(int level) {
        return level * 0.02; // +2% per level, max +20% at +10
    }
}
