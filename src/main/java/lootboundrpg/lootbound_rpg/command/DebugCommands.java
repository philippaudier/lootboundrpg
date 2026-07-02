package lootboundrpg.lootbound_rpg.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import lootboundrpg.lootbound_rpg.affix.AffixGenerator;
import lootboundrpg.lootbound_rpg.affix.AffixInstance;
import lootboundrpg.lootbound_rpg.affix.EquipmentAffix;
import lootboundrpg.lootbound_rpg.item.ModItems;
import lootboundrpg.lootbound_rpg.mobpack.MobPackSpawner;
import lootboundrpg.lootbound_rpg.mobpack.MobPackType;
import lootboundrpg.lootbound_rpg.upgrade.EquipmentGrade;
import lootboundrpg.lootbound_rpg.upgrade.EquipmentNaming;
import lootboundrpg.lootbound_rpg.upgrade.UpgradeSystem;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

/**
 * Debug commands for testing Lootbound RPG systems.
 *
 * Commands:
 * /lbrpg give sword <level>  - Give an iron sword with specified level
 * /lbrpg give pickaxe <level> - Give an iron pickaxe with specified level
 * /lbrpg give stones <type> <amount> - Give upgrade stones
 * /lbrpg upgrade - Attempt to upgrade held item with off-hand stone
 * /lbrpg setlevel <level> - Set level of held item directly
 * /lbrpg setgrade <grade> - Set grade of held item
 * /lbrpg rollaffixes - Roll new affixes on held item based on grade
 * /lbrpg clearaffixes - Remove all affixes from held item
 * /lbrpg info - Show info about held item
 * /lbrpg spawnpack <type> - Spawn a mob pack near the player
 * /lbrpg packinfo - Show mob pack system status
 */
public class DebugCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerCommands(dispatcher);
        });
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("lbrpg")
                // Debug commands - no permission check for testing
                // TODO: Add proper permission check for release

                // /lbrpg give sword <level>
                .then(Commands.literal("give")
                        .then(Commands.literal("sword")
                                .then(Commands.argument("level", IntegerArgumentType.integer(0, 10))
                                        .executes(ctx -> {
                                            int level = IntegerArgumentType.getInteger(ctx, "level");
                                            return giveEquipment(ctx.getSource(), Items.IRON_SWORD, level);
                                        })))

                        // /lbrpg give pickaxe <level>
                        .then(Commands.literal("pickaxe")
                                .then(Commands.argument("level", IntegerArgumentType.integer(0, 10))
                                        .executes(ctx -> {
                                            int level = IntegerArgumentType.getInteger(ctx, "level");
                                            return giveEquipment(ctx.getSource(), Items.IRON_PICKAXE, level);
                                        })))

                        // /lbrpg give stones crude/refined/rare/perfect <amount>
                        .then(Commands.literal("stones")
                                .then(Commands.literal("crude")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> giveStones(ctx.getSource(), "crude",
                                                        IntegerArgumentType.getInteger(ctx, "amount")))))
                                .then(Commands.literal("refined")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> giveStones(ctx.getSource(), "refined",
                                                        IntegerArgumentType.getInteger(ctx, "amount")))))
                                .then(Commands.literal("rare")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> giveStones(ctx.getSource(), "rare",
                                                        IntegerArgumentType.getInteger(ctx, "amount")))))
                                .then(Commands.literal("perfect")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> giveStones(ctx.getSource(), "perfect",
                                                        IntegerArgumentType.getInteger(ctx, "amount")))))))

                // /lbrpg upgrade - use off-hand stone on main-hand equipment
                .then(Commands.literal("upgrade")
                        .executes(ctx -> tryUpgrade(ctx.getSource())))

                // /lbrpg setlevel <level> - directly set level of held item
                .then(Commands.literal("setlevel")
                        .then(Commands.argument("level", IntegerArgumentType.integer(0, 10))
                                .executes(ctx -> {
                                    int level = IntegerArgumentType.getInteger(ctx, "level");
                                    return setLevel(ctx.getSource(), level);
                                })))

                // /lbrpg setgrade <grade> - set grade of held item
                .then(Commands.literal("setgrade")
                        .then(Commands.literal("common")
                                .executes(ctx -> setGrade(ctx.getSource(), EquipmentGrade.COMMON)))
                        .then(Commands.literal("uncommon")
                                .executes(ctx -> setGrade(ctx.getSource(), EquipmentGrade.UNCOMMON)))
                        .then(Commands.literal("rare")
                                .executes(ctx -> setGrade(ctx.getSource(), EquipmentGrade.RARE)))
                        .then(Commands.literal("epic")
                                .executes(ctx -> setGrade(ctx.getSource(), EquipmentGrade.EPIC)))
                        .then(Commands.literal("legendary")
                                .executes(ctx -> setGrade(ctx.getSource(), EquipmentGrade.LEGENDARY))))

                // /lbrpg rollaffixes - roll new affixes based on grade
                .then(Commands.literal("rollaffixes")
                        .executes(ctx -> rollAffixes(ctx.getSource())))

                // /lbrpg clearaffixes - remove all affixes
                .then(Commands.literal("clearaffixes")
                        .executes(ctx -> clearAffixes(ctx.getSource())))

                // /lbrpg info - show info about held item
                .then(Commands.literal("info")
                        .executes(ctx -> showInfo(ctx.getSource())))

                // /lbrpg spawnpack <type> - spawn a mob pack
                .then(Commands.literal("spawnpack")
                        .then(Commands.literal("zombie_horde")
                                .executes(ctx -> spawnPack(ctx.getSource(), MobPackType.ZOMBIE_HORDE)))
                        .then(Commands.literal("skeleton_patrol")
                                .executes(ctx -> spawnPack(ctx.getSource(), MobPackType.SKELETON_PATROL)))
                        .then(Commands.literal("spider_nest")
                                .executes(ctx -> spawnPack(ctx.getSource(), MobPackType.SPIDER_NEST)))
                        .then(Commands.literal("elite_ambush")
                                .executes(ctx -> spawnPack(ctx.getSource(), MobPackType.ELITE_AMBUSH)))
                        .then(Commands.literal("random")
                                .executes(ctx -> spawnPack(ctx.getSource(), MobPackType.randomPack()))))

                // /lbrpg packinfo - show pack system status
                .then(Commands.literal("packinfo")
                        .executes(ctx -> showPackInfo(ctx.getSource())))
        );
    }

    private static int giveEquipment(CommandSourceStack source, Item item, int level) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Command must be run by a player"));
            return 0;
        }

        ItemStack stack = new ItemStack(item);
        if (level > 0) {
            UpgradeSystem.setLevel(stack, level);
        }

        player.getInventory().add(stack);

        String itemName = stack.getHoverName().getString();
        source.sendSuccess(() -> Component.literal("Gave " + itemName + " to " + player.getName().getString()), true);
        return 1;
    }

    private static int giveStones(CommandSourceStack source, String type, int amount) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Command must be run by a player"));
            return 0;
        }

        Item stoneItem = switch (type) {
            case "crude" -> ModItems.CRUDE_UPGRADE_STONE;
            case "refined" -> ModItems.REFINED_UPGRADE_STONE;
            case "rare" -> ModItems.RARE_UPGRADE_STONE;
            case "perfect" -> ModItems.PERFECT_UPGRADE_STONE;
            default -> null;
        };

        if (stoneItem == null) {
            source.sendFailure(Component.literal("Unknown stone type: " + type));
            return 0;
        }

        ItemStack stack = new ItemStack(stoneItem, amount);
        player.getInventory().add(stack);

        String stoneName = stack.getHoverName().getString();
        source.sendSuccess(() -> Component.literal("Gave " + amount + "x " + stoneName +
                " to " + player.getName().getString()), true);
        return 1;
    }

    private static int tryUpgrade(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Command must be run by a player"));
            return 0;
        }

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        UpgradeSystem.UpgradeResult result = UpgradeSystem.attemptUpgrade(mainHand, offHand);

        String message = switch (result) {
            case SUCCESS -> "Upgrade successful! New level: +" + UpgradeSystem.getLevel(mainHand);
            case FAILURE -> "Upgrade failed! Stone consumed, level unchanged.";
            case DOWNGRADE -> "Upgrade failed! DOWNGRADE to +" + UpgradeSystem.getLevel(mainHand);
            case INVALID_ITEM -> "Main hand item cannot be upgraded (needs sword or pickaxe)";
            case INVALID_STONE -> "Wrong stone type for this upgrade level";
            case MAX_LEVEL -> "Item is already at maximum level (+10)";
            case NO_STONE -> "No upgrade stone in off-hand";
        };

        if (result == UpgradeSystem.UpgradeResult.SUCCESS) {
            source.sendSuccess(() -> Component.literal(message), false);
        } else if (result == UpgradeSystem.UpgradeResult.FAILURE ||
                   result == UpgradeSystem.UpgradeResult.DOWNGRADE) {
            source.sendSuccess(() -> Component.literal(message), false);
        } else {
            source.sendFailure(Component.literal(message));
        }

        return result == UpgradeSystem.UpgradeResult.SUCCESS ? 1 : 0;
    }

    private static int setLevel(CommandSourceStack source, int level) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Command must be run by a player"));
            return 0;
        }

        ItemStack mainHand = player.getMainHandItem();

        if (!UpgradeSystem.isUpgradeable(mainHand)) {
            source.sendFailure(Component.literal("Main hand item cannot have equipment level"));
            return 0;
        }

        UpgradeSystem.setLevel(mainHand, level);
        EquipmentNaming.updateDisplayName(mainHand);
        source.sendSuccess(() -> Component.literal("Set equipment level to +" + level), true);
        return 1;
    }

    private static int setGrade(CommandSourceStack source, EquipmentGrade grade) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Command must be run by a player"));
            return 0;
        }

        ItemStack mainHand = player.getMainHandItem();

        if (!UpgradeSystem.isUpgradeable(mainHand)) {
            source.sendFailure(Component.literal("Main hand item cannot have equipment grade"));
            return 0;
        }

        UpgradeSystem.setGrade(mainHand, grade);
        EquipmentNaming.updateDisplayName(mainHand);
        source.sendSuccess(() -> Component.literal("Set equipment grade to " + grade.getName().toUpperCase()), true);
        return 1;
    }

    private static int showInfo(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Command must be run by a player"));
            return 0;
        }

        ItemStack mainHand = player.getMainHandItem();

        if (mainHand.isEmpty()) {
            source.sendFailure(Component.literal("No item in main hand"));
            return 0;
        }

        boolean upgradeable = UpgradeSystem.isUpgradeable(mainHand);
        int level = UpgradeSystem.getLevel(mainHand);
        EquipmentGrade grade = UpgradeSystem.getGrade(mainHand);

        source.sendSuccess(() -> Component.literal("Item: " + mainHand.getHoverName().getString()), false);
        source.sendSuccess(() -> Component.literal("Upgradeable: " + (upgradeable ? "Yes" : "No")), false);

        if (upgradeable) {
            source.sendSuccess(() -> Component.literal("Grade: " + grade.getName().toUpperCase() +
                    " (+" + grade.getBonusPercent() + "% bonus)"), false);

            // Show affixes
            List<AffixInstance> affixes = AffixGenerator.getAffixes(mainHand);
            if (!affixes.isEmpty()) {
                source.sendSuccess(() -> Component.literal("Affixes:"), false);
                for (AffixInstance instance : affixes) {
                    EquipmentAffix affix = instance.getAffix();
                    if (affix != null) {
                        source.sendSuccess(() -> Component.literal("  - " + affix.getDisplayNameEn() +
                                " " + instance.getTierRoman() + " (+" + instance.getDisplayPercent() + "%)"), false);
                    }
                }
            } else {
                source.sendSuccess(() -> Component.literal("Affixes: None"), false);
            }

            source.sendSuccess(() -> Component.literal("Current Level: +" + level), false);
            if (level < UpgradeSystem.MAX_LEVEL) {
                int nextLevel = level + 1;
                double chance = UpgradeSystem.getSuccessChance(nextLevel);
                double downgradeChance = UpgradeSystem.getDowngradeChance(nextLevel);
                int xpCost = UpgradeSystem.getXpCost(nextLevel);
                Item reqStone = UpgradeSystem.getRequiredStone(nextLevel);
                ItemStack reqStoneStack = new ItemStack(reqStone);
                source.sendSuccess(() -> Component.literal("Next upgrade: +" + nextLevel +
                        " (" + (int)(chance * 100) + "% chance, " + xpCost + " XP, needs " +
                        reqStoneStack.getHoverName().getString() + ")"), false);
                if (downgradeChance > 0) {
                    source.sendSuccess(() -> Component.literal("")
                            .append(Component.literal("Downgrade Risk: ").withStyle(ChatFormatting.DARK_RED))
                            .append(Component.literal((int)(downgradeChance * 100) + "%").withStyle(ChatFormatting.RED)), false);
                }
            } else {
                source.sendSuccess(() -> Component.literal("Max level reached!"), false);
            }
        }

        return 1;
    }

    private static int rollAffixes(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Command must be run by a player"));
            return 0;
        }

        ItemStack mainHand = player.getMainHandItem();

        if (!UpgradeSystem.isUpgradeable(mainHand)) {
            source.sendFailure(Component.literal("Main hand item cannot have affixes"));
            return 0;
        }

        EquipmentGrade grade = UpgradeSystem.getGrade(mainHand);
        AffixGenerator.rollAndApplyAffixes(mainHand, grade);

        List<AffixInstance> affixes = AffixGenerator.getAffixes(mainHand);
        if (affixes.isEmpty()) {
            source.sendSuccess(() -> Component.literal("No affixes rolled (grade too low)"), true);
        } else {
            StringBuilder affixStr = new StringBuilder("Rolled affixes: ");
            for (int i = 0; i < affixes.size(); i++) {
                if (i > 0) affixStr.append(", ");
                AffixInstance instance = affixes.get(i);
                EquipmentAffix affix = instance.getAffix();
                if (affix != null) {
                    affixStr.append(affix.getDisplayNameEn()).append(" ").append(instance.getTierRoman());
                }
            }
            source.sendSuccess(() -> Component.literal(affixStr.toString()), true);
        }
        return 1;
    }

    private static int clearAffixes(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Command must be run by a player"));
            return 0;
        }

        ItemStack mainHand = player.getMainHandItem();

        if (!UpgradeSystem.isUpgradeable(mainHand)) {
            source.sendFailure(Component.literal("Main hand item cannot have affixes"));
            return 0;
        }

        AffixGenerator.clearAffixes(mainHand);
        source.sendSuccess(() -> Component.literal("Cleared all affixes from item"), true);
        return 1;
    }

    private static int spawnPack(CommandSourceStack source, MobPackType packType) {
        ServerPlayer player = source.getPlayer();
        if (player == null) {
            source.sendFailure(Component.literal("Command must be run by a player"));
            return 0;
        }

        // Find spawn position near player (15-25 blocks away)
        BlockPos playerPos = player.blockPosition();
        double angle = Math.random() * Math.PI * 2;
        int distance = 15 + (int)(Math.random() * 10);
        int x = playerPos.getX() + (int)(Math.cos(angle) * distance);
        int z = playerPos.getZ() + (int)(Math.sin(angle) * distance);

        // Find ground level
        BlockPos spawnPos = new BlockPos(x, playerPos.getY(), z);
        for (int y = playerPos.getY() + 10; y > playerPos.getY() - 10; y--) {
            BlockPos checkPos = new BlockPos(x, y, z);
            if (player.level().getBlockState(checkPos.below()).isSolid() &&
                player.level().getBlockState(checkPos).isAir()) {
                spawnPos = checkPos;
                break;
            }
        }

        // Get server level
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            source.sendFailure(Component.literal("Not on server side"));
            return 0;
        }

        // Spawn the pack
        int spawned = MobPackSpawner.spawnPack(serverLevel, spawnPos, packType);

        if (spawned > 0) {
            final BlockPos finalPos = spawnPos;
            source.sendSuccess(() -> Component.literal("")
                    .append(Component.literal("[Pack] ").withStyle(ChatFormatting.GOLD))
                    .append(Component.literal("Spawned ").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(packType.getDisplayName()).withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(" (" + spawned + " mobs) at ").withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(finalPos.toShortString()).withStyle(ChatFormatting.AQUA)), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("Failed to spawn pack (no valid positions)"));
            return 0;
        }
    }

    private static int showPackInfo(CommandSourceStack source) {
        int packMobCount = MobPackSpawner.getPackMobCount();

        source.sendSuccess(() -> Component.literal("")
                .append(Component.literal("=== Mob Pack Info ===").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)), false);

        source.sendSuccess(() -> Component.literal("Active pack mobs: ")
                .append(Component.literal(String.valueOf(packMobCount)).withStyle(ChatFormatting.YELLOW)), false);

        source.sendSuccess(() -> Component.literal("Available packs:").withStyle(ChatFormatting.WHITE), false);
        for (MobPackType type : MobPackType.values()) {
            source.sendSuccess(() -> Component.literal("  - ")
                    .append(Component.literal(type.name().toLowerCase()).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(" (" + type.getTier() + ")").withStyle(ChatFormatting.GRAY)), false);
        }

        return 1;
    }
}
