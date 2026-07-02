package lootboundrpg.lootbound_rpg.client;

import lootboundrpg.lootbound_rpg.upgrade.EquipmentGrade;
import lootboundrpg.lootbound_rpg.upgrade.EquipmentScaling;
import lootboundrpg.lootbound_rpg.upgrade.UpgradeSystem;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Adds equipment level and grade information to item tooltips.
 *
 * Shows the current level, grade, and stat bonuses for upgraded equipment.
 */
public class TooltipHandler {

    public static void register() {
        ItemTooltipCallback.EVENT.register(TooltipHandler::addEquipmentTooltip);
    }

    private static void addEquipmentTooltip(ItemStack stack, Item.TooltipContext context,
                                            TooltipFlag flag, List<Component> lines) {
        if (!UpgradeSystem.isUpgradeable(stack)) {
            return;
        }

        int level = UpgradeSystem.getLevel(stack);
        EquipmentGrade grade = UpgradeSystem.getGrade(stack);

        // Add blank line before our info
        lines.add(Component.empty());

        // Show grade
        lines.add(Component.translatable("lootbound_rpg.tooltip.grade")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(": "))
                .append(grade.getDisplayComponent()));

        // Show grade bonus if not COMMON
        if (grade != EquipmentGrade.COMMON) {
            lines.add(Component.literal(" +")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(grade.getBonusPercent() + "%")
                            .withStyle(grade.getColor()))
                    .append(Component.translatable("lootbound_rpg.tooltip.grade_bonus")
                            .withStyle(ChatFormatting.DARK_GRAY)));
        }

        // Show equipment level
        if (level > 0) {
            lines.add(Component.translatable("lootbound_rpg.tooltip.level")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(": "))
                    .append(Component.literal("+" + level + "/" + UpgradeSystem.MAX_LEVEL)
                            .withStyle(getLevelColor(level))));

            // Show bonuses based on item type (with grade multiplier)
            if (stack.is(ItemTags.SWORDS)) {
                double damageBonus = EquipmentScaling.getAttackDamageBonus(level, grade);
                lines.add(Component.literal(" +")
                        .withStyle(ChatFormatting.DARK_GREEN)
                        .append(Component.literal(String.format("%.1f", damageBonus))
                                .withStyle(ChatFormatting.GREEN))
                        .append(Component.translatable("lootbound_rpg.tooltip.attack_damage")
                                .withStyle(ChatFormatting.DARK_GREEN)));
            } else if (stack.is(ItemTags.PICKAXES)) {
                double speedMultiplier = EquipmentScaling.getMiningSpeedMultiplier(level, grade);
                int speedPercent = (int) ((speedMultiplier - 1.0) * 100);
                lines.add(Component.literal(" +")
                        .withStyle(ChatFormatting.DARK_AQUA)
                        .append(Component.literal(speedPercent + "%")
                                .withStyle(ChatFormatting.AQUA))
                        .append(Component.translatable("lootbound_rpg.tooltip.mining_speed")
                                .withStyle(ChatFormatting.DARK_AQUA)));
            }
        } else {
            lines.add(Component.translatable("lootbound_rpg.tooltip.level")
                    .withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(": "))
                    .append(Component.literal("+0/" + UpgradeSystem.MAX_LEVEL)
                            .withStyle(ChatFormatting.DARK_GRAY)));
            lines.add(Component.translatable("lootbound_rpg.tooltip.can_upgrade")
                    .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
        }

        // Show next upgrade info
        if (level < UpgradeSystem.MAX_LEVEL) {
            int nextLevel = level + 1;
            double chance = UpgradeSystem.getSuccessChance(nextLevel);
            int xpCost = UpgradeSystem.getXpCost(nextLevel);
            lines.add(Component.translatable("lootbound_rpg.tooltip.next_level", nextLevel)
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(" ("))
                    .append(Component.literal((int)(chance * 100) + "%")
                            .withStyle(chance >= 0.5 ? ChatFormatting.GREEN : ChatFormatting.YELLOW))
                    .append(Component.literal(", "))
                    .append(Component.literal(xpCost + " XP")
                            .withStyle(ChatFormatting.GREEN))
                    .append(Component.literal(")")));
        } else {
            lines.add(Component.translatable("lootbound_rpg.tooltip.max_level")
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        }
    }

    /**
     * Returns a color based on the equipment level.
     */
    private static ChatFormatting getLevelColor(int level) {
        return switch (level) {
            case 1, 2, 3 -> ChatFormatting.WHITE;
            case 4, 5, 6 -> ChatFormatting.BLUE;
            case 7, 8, 9 -> ChatFormatting.DARK_PURPLE;
            case 10 -> ChatFormatting.GOLD;
            default -> ChatFormatting.GRAY;
        };
    }
}
