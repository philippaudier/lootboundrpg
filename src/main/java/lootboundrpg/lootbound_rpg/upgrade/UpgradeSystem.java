package lootboundrpg.lootbound_rpg.upgrade;

import lootboundrpg.lootbound_rpg.component.ModDataComponents;
import lootboundrpg.lootbound_rpg.config.LootboundConfig;
import lootboundrpg.lootbound_rpg.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Core upgrade system logic for Lootbound RPG.
 *
 * Handles:
 * - Checking if items are upgradeable
 * - Determining required stones for each level
 * - Calculating success chances
 * - Performing upgrades
 * - Managing equipment grades
 *
 * MC 26.2 Note: Uses item tags instead of class instanceof checks,
 * since tool classes (SwordItem, PickaxeItem) no longer exist.
 */
public class UpgradeSystem {

    public static final int MAX_LEVEL = 10;

    // Item tags for identifying upgradeable equipment
    private static final TagKey<Item> SWORDS_TAG = ItemTags.SWORDS;
    private static final TagKey<Item> PICKAXES_TAG = ItemTags.PICKAXES;

    /**
     * Checks if an item can be upgraded (swords and pickaxes only in V1).
     * Uses item tags instead of instanceof checks for MC 26.2 compatibility.
     */
    public static boolean isUpgradeable(ItemStack stack) {
        if (stack.isEmpty()) return false;

        return stack.is(SWORDS_TAG) || stack.is(PICKAXES_TAG);
    }

    /**
     * Gets the current equipment level of an item.
     * Returns 0 if the item has no level component.
     */
    public static int getLevel(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.EQUIPMENT_LEVEL, 0);
    }

    /**
     * Sets the equipment level on an item.
     */
    public static void setLevel(ItemStack stack, int level) {
        if (level < 0) level = 0;
        if (level > MAX_LEVEL) level = MAX_LEVEL;

        stack.set(ModDataComponents.EQUIPMENT_LEVEL, level);
    }

    /**
     * Gets the equipment grade of an item.
     * Returns COMMON if no grade is set.
     */
    public static EquipmentGrade getGrade(ItemStack stack) {
        int gradeId = stack.getOrDefault(ModDataComponents.EQUIPMENT_GRADE, 0);
        return EquipmentGrade.fromId(gradeId);
    }

    /**
     * Sets the equipment grade on an item.
     */
    public static void setGrade(ItemStack stack, EquipmentGrade grade) {
        stack.set(ModDataComponents.EQUIPMENT_GRADE, grade.getId());
    }

    /**
     * Gets the XP level cost for upgrading to a target level.
     */
    public static int getXpCost(int targetLevel) {
        return switch (targetLevel) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 3;
            case 4 -> 5;
            case 5 -> 7;
            case 6 -> 9;
            case 7 -> 12;
            case 8 -> 16;
            case 9 -> 20;
            case 10 -> 30;
            default -> 0;
        };
    }

    /**
     * Determines which upgrade stone is required for the target level.
     *
     * +1 to +3: Crude Upgrade Stone
     * +4 to +6: Refined Upgrade Stone
     * +7 to +9: Rare Upgrade Stone
     * +10: Perfect Upgrade Stone
     */
    public static Item getRequiredStone(int targetLevel) {
        return switch (targetLevel) {
            case 1, 2, 3 -> ModItems.CRUDE_UPGRADE_STONE;
            case 4, 5, 6 -> ModItems.REFINED_UPGRADE_STONE;
            case 7, 8, 9 -> ModItems.RARE_UPGRADE_STONE;
            case 10 -> ModItems.PERFECT_UPGRADE_STONE;
            default -> null;
        };
    }

    /**
     * Gets the base success chance for upgrading to a target level.
     *
     * +1 to +3: 100%
     * +4: 80%, +5: 70%, +6: 60%
     * +7: 45%, +8: 35%, +9: 25%
     * +10: 15%
     */
    public static double getBaseSuccessChance(int targetLevel) {
        return switch (targetLevel) {
            case 1, 2, 3 -> 1.0;     // 100%
            case 4 -> 0.80;          // 80%
            case 5 -> 0.70;          // 70%
            case 6 -> 0.60;          // 60%
            case 7 -> 0.45;          // 45%
            case 8 -> 0.35;          // 35%
            case 9 -> 0.25;          // 25%
            case 10 -> 0.15;         // 15%
            default -> 0.0;
        };
    }

    /**
     * Gets the success chance with config multiplier applied.
     * Capped at 100%.
     */
    public static double getSuccessChance(int targetLevel) {
        double baseChance = getBaseSuccessChance(targetLevel);
        return LootboundConfig.get().applyUpgradeMultiplier(baseChance);
    }

    /**
     * Checks if an upgrade stone is valid for the target level.
     */
    public static boolean isValidStone(ItemStack stoneStack, int targetLevel) {
        if (stoneStack.isEmpty()) return false;

        Item required = getRequiredStone(targetLevel);
        return required != null && stoneStack.getItem() == required;
    }

    /**
     * Result of an upgrade attempt.
     */
    public enum UpgradeResult {
        SUCCESS,           // Upgrade succeeded, level increased
        FAILURE,           // Upgrade failed, stone consumed but level unchanged
        INVALID_ITEM,      // Item cannot be upgraded
        INVALID_STONE,     // Wrong stone type for this level
        MAX_LEVEL,         // Item is already at max level
        NO_STONE           // No stone provided
    }

    /**
     * Attempts to upgrade an equipment item.
     *
     * @param equipment The item to upgrade
     * @param stone The upgrade stone to consume
     * @return The result of the upgrade attempt
     */
    public static UpgradeResult attemptUpgrade(ItemStack equipment, ItemStack stone) {
        // Validate equipment
        if (!isUpgradeable(equipment)) {
            return UpgradeResult.INVALID_ITEM;
        }

        int currentLevel = getLevel(equipment);
        int targetLevel = currentLevel + 1;

        // Check max level
        if (currentLevel >= MAX_LEVEL) {
            return UpgradeResult.MAX_LEVEL;
        }

        // Validate stone
        if (stone.isEmpty()) {
            return UpgradeResult.NO_STONE;
        }

        if (!isValidStone(stone, targetLevel)) {
            return UpgradeResult.INVALID_STONE;
        }

        // Consume stone
        stone.shrink(1);

        // Roll for success
        double chance = getSuccessChance(targetLevel);
        boolean success = Math.random() < chance;

        if (success) {
            setLevel(equipment, targetLevel);
            return UpgradeResult.SUCCESS;
        } else {
            // V1: Failure only consumes stone, no level loss
            return UpgradeResult.FAILURE;
        }
    }
}
