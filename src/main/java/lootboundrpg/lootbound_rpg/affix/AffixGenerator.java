package lootboundrpg.lootbound_rpg.affix;

import lootboundrpg.lootbound_rpg.affix.EquipmentAffix.AffixCategory;
import lootboundrpg.lootbound_rpg.affix.EquipmentAffix.AffixType;
import lootboundrpg.lootbound_rpg.component.ModDataComponents;
import lootboundrpg.lootbound_rpg.upgrade.EquipmentGrade;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Generates random affixes for equipment based on grade.
 *
 * Affix count and tier by grade:
 * - COMMON: 0 affixes
 * - UNCOMMON: 1 minor (tier I)
 * - RARE: 1 major (tier II)
 * - EPIC: 1 major (tier II) + 1 minor (tier II)
 * - LEGENDARY: 2 majors (tier III) + 1 minor (tier III)
 */
public class AffixGenerator {

    private static final Random RANDOM = new Random();

    /**
     * Generates and applies affixes to an equipment item based on its grade.
     */
    public static void rollAndApplyAffixes(ItemStack stack, EquipmentGrade grade) {
        if (stack.isEmpty()) return;

        AffixCategory category = getCategoryForItem(stack);
        if (category == null) return;

        List<AffixInstance> affixes = rollAffixesForGrade(grade, category);
        if (affixes.isEmpty()) {
            stack.remove(ModDataComponents.EQUIPMENT_AFFIXES);
        } else {
            stack.set(ModDataComponents.EQUIPMENT_AFFIXES, AffixData.fromInstances(affixes));
        }
    }

    /**
     * Gets the affix category for an item based on its type.
     */
    public static AffixCategory getCategoryForItem(ItemStack stack) {
        if (stack.is(ItemTags.SWORDS)) {
            return AffixCategory.WEAPON;
        } else if (stack.is(ItemTags.PICKAXES)) {
            return AffixCategory.TOOL;
        }
        return null;
    }

    /**
     * Rolls affixes for a given grade and category.
     *
     * @param grade The equipment grade
     * @param category The affix category (WEAPON or TOOL)
     * @return List of rolled affix instances (never null, may be empty)
     */
    public static List<AffixInstance> rollAffixesForGrade(EquipmentGrade grade, AffixCategory category) {
        int majorCount = getMajorAffixCount(grade);
        int minorCount = getMinorAffixCount(grade);
        int tier = getAffixTier(grade);

        if (majorCount == 0 && minorCount == 0) {
            return Collections.emptyList();
        }

        List<AffixInstance> result = new ArrayList<>();
        List<EquipmentAffix> availableMajor = getAffixesOfType(category, AffixType.MAJOR);
        List<EquipmentAffix> availableMinor = getAffixesOfType(category, AffixType.MINOR);

        // Roll major affixes first
        Collections.shuffle(availableMajor, RANDOM);
        for (int i = 0; i < majorCount && i < availableMajor.size(); i++) {
            result.add(AffixInstance.of(availableMajor.get(i), tier));
        }

        // Roll minor affixes
        Collections.shuffle(availableMinor, RANDOM);
        for (int i = 0; i < minorCount && i < availableMinor.size(); i++) {
            result.add(AffixInstance.of(availableMinor.get(i), tier));
        }

        return result;
    }

    /**
     * Gets the number of major affixes for a grade.
     */
    private static int getMajorAffixCount(EquipmentGrade grade) {
        return switch (grade) {
            case COMMON, UNCOMMON -> 0;
            case RARE, EPIC -> 1;
            case LEGENDARY -> 2;
        };
    }

    /**
     * Gets the number of minor affixes for a grade.
     */
    private static int getMinorAffixCount(EquipmentGrade grade) {
        return switch (grade) {
            case COMMON -> 0;
            case UNCOMMON, RARE, EPIC, LEGENDARY -> 1;
        };
    }

    /**
     * Gets the affix tier for a grade.
     * Higher grades get higher tier affixes.
     */
    private static int getAffixTier(EquipmentGrade grade) {
        return switch (grade) {
            case COMMON -> 1;
            case UNCOMMON -> 1;  // Tier I
            case RARE -> 2;      // Tier II
            case EPIC -> 2;      // Tier II
            case LEGENDARY -> 3; // Tier III
        };
    }

    /**
     * Gets all affixes of a specific category and type.
     */
    private static List<EquipmentAffix> getAffixesOfType(AffixCategory category, AffixType type) {
        List<EquipmentAffix> result = new ArrayList<>();
        for (EquipmentAffix affix : EquipmentAffix.values()) {
            if (affix.getCategory() == category && affix.getType() == type) {
                result.add(affix);
            }
        }
        return result;
    }

    /**
     * Gets the affix instances currently on an item.
     */
    public static List<AffixInstance> getAffixes(ItemStack stack) {
        AffixData data = stack.get(ModDataComponents.EQUIPMENT_AFFIXES);
        if (data == null || data.isEmpty()) {
            return Collections.emptyList();
        }
        return data.affixes();
    }

    /**
     * Checks if an item has any affixes.
     */
    public static boolean hasAffixes(ItemStack stack) {
        AffixData data = stack.get(ModDataComponents.EQUIPMENT_AFFIXES);
        return data != null && !data.isEmpty();
    }

    /**
     * Clears all affixes from an item.
     */
    public static void clearAffixes(ItemStack stack) {
        stack.remove(ModDataComponents.EQUIPMENT_AFFIXES);
    }

    /**
     * Sets specific affixes on an item.
     */
    public static void setAffixes(ItemStack stack, List<AffixInstance> affixes) {
        if (affixes.isEmpty()) {
            stack.remove(ModDataComponents.EQUIPMENT_AFFIXES);
        } else {
            stack.set(ModDataComponents.EQUIPMENT_AFFIXES, AffixData.fromInstances(affixes));
        }
    }
}
