package lootboundrpg.lootbound_rpg.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import lootboundrpg.lootbound_rpg.config.LootboundConfig;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * In-game guide command for Lootbound RPG.
 *
 * Usage: /lbrpg guide [topic]
 * Topics: stones, upgrades, grades, table, drops, packs
 */
public class GuideCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
        });
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lbrpg")
                .then(Commands.literal("guide")
                        .executes(GuideCommand::showOverview)
                        .then(Commands.literal("stones")
                                .executes(GuideCommand::showStones))
                        .then(Commands.literal("upgrades")
                                .executes(GuideCommand::showUpgrades))
                        .then(Commands.literal("grades")
                                .executes(GuideCommand::showGrades))
                        .then(Commands.literal("table")
                                .executes(GuideCommand::showTable))
                        .then(Commands.literal("drops")
                                .executes(GuideCommand::showDrops))
                        .then(Commands.literal("packs")
                                .executes(GuideCommand::showPacks)))
                .then(Commands.literal("reload")
                        .executes(GuideCommand::reloadConfig)));
    }

    private static int showOverview(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        sendHeader(source, "Lootbound RPG Guide");
        sendLine(source, "Transform your gear from +0 to +10!");
        sendLine(source, "");
        sendLine(source, "Topics (use /lbrpg guide <topic>):");
        sendBullet(source, "stones", "Upgrade stone types & sources");
        sendBullet(source, "upgrades", "How the +0 to +10 system works");
        sendBullet(source, "grades", "Equipment rarity grades");
        sendBullet(source, "table", "Using the Upgrade Table");
        sendBullet(source, "drops", "Mob equipment drops");
        sendBullet(source, "packs", "Mob pack encounters");

        return 1;
    }

    private static int showStones(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        sendHeader(source, "Upgrade Stones");
        sendLine(source, "Four tiers of stones for different upgrade levels:");
        sendLine(source, "");
        sendStone(source, "Crude Upgrade Stone", "+1 to +3", "Common mobs (zombie, skeleton...)");
        sendStone(source, "Refined Upgrade Stone", "+4 to +6", "Dangerous mobs (enderman, blaze...)");
        sendStone(source, "Rare Upgrade Stone", "+7 to +9", "Elite mobs (wither skeleton...)");
        sendStone(source, "Perfect Upgrade Stone", "+10", "Crafted from Rare stones + Nether Star");

        return 1;
    }

    private static int showUpgrades(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        sendHeader(source, "Upgrade System (+0 to +10)");
        sendLine(source, "Each level increases equipment stats:");
        sendLine(source, "");
        sendUpgradeInfo(source, "+1 to +3", "100%", "1-3 XP", "Crude Stone");
        sendUpgradeInfo(source, "+4 to +6", "80-60%", "5-9 XP", "Refined Stone");
        sendUpgradeInfo(source, "+7 to +9", "45-25%", "12-20 XP", "Rare Stone");
        sendUpgradeInfo(source, "+10", "15%", "30 XP", "Perfect Stone");
        sendLine(source, "");
        sendLine(source, ChatFormatting.YELLOW + "Failure consumes the stone but keeps your level!");

        return 1;
    }

    private static int showGrades(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        sendHeader(source, "Equipment Grades");
        sendLine(source, "Grades multiply your upgrade bonuses:");
        sendLine(source, "");
        sendGrade(source, "Common", ChatFormatting.WHITE, "1.00x", "No bonus");
        sendGrade(source, "Uncommon", ChatFormatting.GREEN, "1.05x", "+5% bonus");
        sendGrade(source, "Rare", ChatFormatting.BLUE, "1.10x", "+10% bonus");
        sendGrade(source, "Epic", ChatFormatting.LIGHT_PURPLE, "1.20x", "+20% bonus");
        sendGrade(source, "Legendary", ChatFormatting.GOLD, "1.35x", "+35% bonus");
        sendLine(source, "");
        sendLine(source, "Higher tier mobs drop better grades!");

        return 1;
    }

    private static int showTable(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        sendHeader(source, "Upgrade Table");
        sendLine(source, "Craft: Obsidian + Lapis + Iron + Diamond");
        sendLine(source, "");
        sendLine(source, "How to use:");
        sendBullet(source, "1", "Place equipment in left slot");
        sendBullet(source, "2", "Place upgrade stone in right slot");
        sendBullet(source, "3", "Check chance, XP cost, and stone tier");
        sendBullet(source, "4", "Click Upgrade!");
        sendLine(source, "");
        sendLine(source, ChatFormatting.GREEN + "Success: Level increases!");
        sendLine(source, ChatFormatting.RED + "Failure: Stone consumed, level stays.");

        return 1;
    }

    private static int showDrops(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        sendHeader(source, "Mob Drops");
        sendLine(source, "Kill mobs for stones and graded equipment:");
        sendLine(source, "");
        sendMobTier(source, "Common", "Zombie, Skeleton, Spider", "Crude stones, basic gear");
        sendMobTier(source, "Dangerous", "Enderman, Blaze, Witch", "Refined stones, iron/diamond gear");
        sendMobTier(source, "Elite", "Wither Skeleton, Evoker", "Rare stones, diamond/netherite gear");
        sendLine(source, "");
        sendLine(source, "Elite mobs can drop pre-upgraded equipment (+1 to +4)!");

        return 1;
    }

    private static int showPacks(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        sendHeader(source, "Mob Packs");
        sendLine(source, "Mob packs spawn periodically around players:");
        sendLine(source, "");
        sendPackInfo(source, "Zombie Pack", "3-5 zombies", "Common");
        sendPackInfo(source, "Skeleton Patrol", "2-3 skeletons + zombie", "Dangerous");
        sendPackInfo(source, "Spider Nest", "3 spiders", "Dangerous");
        sendPackInfo(source, "Mixed Ambush", "2 zombies + skeleton + spider", "Elite");
        sendLine(source, "");
        sendLine(source, ChatFormatting.RED + "Elite mobs" + ChatFormatting.WHITE + " can spawn in packs!");
        sendLine(source, "Elites have boosted stats and guaranteed drops.");
        sendLine(source, "");
        sendLine(source, "Debug: /lbrpg spawnpack <type>");

        return 1;
    }

    private static int reloadConfig(CommandContext<CommandSourceStack> context) {
        LootboundConfig.reload();
        context.getSource().sendSuccess(
                () -> Component.literal("Lootbound RPG config reloaded!").withStyle(ChatFormatting.GREEN),
                true);
        return 1;
    }

    // === Helper Methods ===

    private static void sendHeader(CommandSourceStack source, String title) {
        source.sendSuccess(() -> Component.literal("=== " + title + " ===")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
    }

    private static void sendLine(CommandSourceStack source, String text) {
        source.sendSuccess(() -> Component.literal(text), false);
    }

    private static void sendBullet(CommandSourceStack source, String key, String value) {
        MutableComponent msg = Component.literal("  • ")
                .withStyle(ChatFormatting.GRAY)
                .append(Component.literal(key).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" - " + value).withStyle(ChatFormatting.WHITE));
        source.sendSuccess(() -> msg, false);
    }

    private static void sendStone(CommandSourceStack source, String name, String levels, String drop) {
        MutableComponent msg = Component.literal("  ")
                .append(Component.literal(name).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" [" + levels + "]").withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" - " + drop).withStyle(ChatFormatting.GRAY));
        source.sendSuccess(() -> msg, false);
    }

    private static void sendUpgradeInfo(CommandSourceStack source, String level, String chance, String xp, String stone) {
        MutableComponent msg = Component.literal("  ")
                .append(Component.literal(level).withStyle(ChatFormatting.GREEN))
                .append(Component.literal(" | " + chance).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" | " + xp).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" | " + stone).withStyle(ChatFormatting.GRAY));
        source.sendSuccess(() -> msg, false);
    }

    private static void sendGrade(CommandSourceStack source, String name, ChatFormatting color, String mult, String desc) {
        MutableComponent msg = Component.literal("  ")
                .append(Component.literal(name).withStyle(color))
                .append(Component.literal(" " + mult).withStyle(ChatFormatting.YELLOW))
                .append(Component.literal(" (" + desc + ")").withStyle(ChatFormatting.GRAY));
        source.sendSuccess(() -> msg, false);
    }

    private static void sendMobTier(CommandSourceStack source, String tier, String mobs, String drops) {
        MutableComponent msg = Component.literal("  ")
                .append(Component.literal(tier + ": ").withStyle(ChatFormatting.GOLD))
                .append(Component.literal(mobs).withStyle(ChatFormatting.WHITE));
        source.sendSuccess(() -> msg, false);
        source.sendSuccess(() -> Component.literal("    → " + drops).withStyle(ChatFormatting.GRAY), false);
    }

    private static void sendPackInfo(CommandSourceStack source, String name, String composition, String tier) {
        ChatFormatting tierColor = switch (tier) {
            case "Common" -> ChatFormatting.WHITE;
            case "Dangerous" -> ChatFormatting.YELLOW;
            case "Elite" -> ChatFormatting.RED;
            default -> ChatFormatting.GRAY;
        };

        MutableComponent msg = Component.literal("  ")
                .append(Component.literal(name).withStyle(ChatFormatting.AQUA))
                .append(Component.literal(" [" + tier + "]").withStyle(tierColor))
                .append(Component.literal(" - " + composition).withStyle(ChatFormatting.GRAY));
        source.sendSuccess(() -> msg, false);
    }
}
