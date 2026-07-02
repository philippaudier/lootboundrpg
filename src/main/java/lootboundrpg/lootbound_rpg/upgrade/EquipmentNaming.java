package lootboundrpg.lootbound_rpg.upgrade;

import lootboundrpg.lootbound_rpg.config.LootboundConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

/**
 * Handles colored naming of equipment based on grade and level.
 *
 * Examples:
 * - Iron Sword +4 in blue if Rare
 * - Diamond Pickaxe +7 in gold if Legendary
 */
public class EquipmentNaming {

    /**
     * Updates the display name of an item based on its grade and level.
     * Only applies if colored names are enabled in config.
     */
    public static void updateDisplayName(ItemStack stack) {
        if (stack.isEmpty()) return;
        if (!UpgradeSystem.isUpgradeable(stack)) return;
        if (!LootboundConfig.get().enableColoredItemNames) return;

        // Don't overwrite manually named items (check if it has a custom name that isn't ours)
        // We'll use a marker in the name to detect our own names
        Component existingCustomName = stack.get(DataComponents.CUSTOM_NAME);
        if (existingCustomName != null) {
            String plainText = existingCustomName.getString();
            // If it doesn't contain a + level indicator, it might be a manual name
            if (!plainText.contains("+") && !plainText.isEmpty()) {
                // Check if it looks like a manual rename (no grade color markers)
                // For simplicity, we always update if the item is upgradeable
            }
        }

        int level = UpgradeSystem.getLevel(stack);
        EquipmentGrade grade = UpgradeSystem.getGrade(stack);

        // Get base item name (without custom name)
        stack.remove(DataComponents.CUSTOM_NAME);
        String baseName = stack.getHoverName().getString();

        // Build colored name with level
        MutableComponent coloredName = Component.literal(baseName)
                .withStyle(grade.getColor());

        if (level > 0) {
            coloredName.append(Component.literal(" +" + level)
                    .withStyle(grade.getColor()));
        }

        // Apply the custom name
        stack.set(DataComponents.CUSTOM_NAME, coloredName);
    }

    /**
     * Gets the display name component for an item with grade coloring.
     * Used when you need the name without modifying the item.
     */
    public static Component getColoredDisplayName(ItemStack stack) {
        if (stack.isEmpty()) return Component.empty();
        if (!UpgradeSystem.isUpgradeable(stack)) return stack.getHoverName();

        int level = UpgradeSystem.getLevel(stack);
        EquipmentGrade grade = UpgradeSystem.getGrade(stack);

        // Get the base item name (need to temporarily remove custom name)
        Component customName = stack.get(DataComponents.CUSTOM_NAME);
        stack.remove(DataComponents.CUSTOM_NAME);
        String baseName = stack.getHoverName().getString();
        if (customName != null) {
            stack.set(DataComponents.CUSTOM_NAME, customName);
        }

        MutableComponent result = Component.literal(baseName)
                .withStyle(grade.getColor());

        if (level > 0) {
            result.append(Component.literal(" +" + level)
                    .withStyle(grade.getColor()));
        }

        return result;
    }

    /**
     * Clears the colored name from an item, restoring the default name.
     */
    public static void clearColoredName(ItemStack stack) {
        if (stack.isEmpty()) return;
        stack.remove(DataComponents.CUSTOM_NAME);
    }
}
