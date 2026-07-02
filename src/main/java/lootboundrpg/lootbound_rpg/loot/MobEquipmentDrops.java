package lootboundrpg.lootbound_rpg.loot;

import lootboundrpg.lootbound_rpg.LootboundRpgMod;
import lootboundrpg.lootbound_rpg.config.LootboundConfig;
import lootboundrpg.lootbound_rpg.mobpack.EliteMobFactory;
import lootboundrpg.lootbound_rpg.upgrade.EquipmentGrade;
import lootboundrpg.lootbound_rpg.upgrade.UpgradeSystem;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Random;
import java.util.Set;

/**
 * Handles equipment drops from mobs with random grades and levels.
 *
 * Drop chances and grade weights vary by mob tier:
 * - Common mobs (zombie, skeleton): Low drop chance, mostly Common/Uncommon
 * - Dangerous mobs (enderman, witch): Medium drop chance, can drop Rare
 * - Elite mobs (wither skeleton, piglin brute): High drop chance, can drop Epic
 */
public class MobEquipmentDrops {

    private static final Random RANDOM = new Random();

    // Drop chance percentages
    private static final float COMMON_MOB_DROP_CHANCE = 0.03f;      // 3%
    private static final float DANGEROUS_MOB_DROP_CHANCE = 0.06f;   // 6%
    private static final float ELITE_MOB_DROP_CHANCE = 0.10f;       // 10%

    // Mob categories by entity ID
    private static final Set<String> COMMON_MOBS = Set.of(
            "zombie", "skeleton", "spider", "creeper"
    );

    private static final Set<String> COMMON_PLUS_MOBS = Set.of(
            "husk", "stray", "drowned", "cave_spider"
    );

    private static final Set<String> DANGEROUS_MOBS = Set.of(
            "enderman", "witch", "vindicator", "pillager", "blaze", "piglin"
    );

    private static final Set<String> ELITE_MOBS = Set.of(
            "wither_skeleton", "piglin_brute", "evoker", "ravager"
    );

    public static void register() {
        LootboundRpgMod.LOGGER.info("Registering Lootbound RPG mob equipment drops...");

        ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
            // Check if equipment drops are enabled
            if (!LootboundConfig.get().enableEquipmentDrops) return;

            // Only process on server side
            if (entity.level().isClientSide()) return;

            // Only drop when killed by a player
            if (!isKilledByPlayer(damageSource)) return;

            // Check mob type and handle drops
            handleMobDeath(entity);
        });
    }

    private static boolean isKilledByPlayer(DamageSource source) {
        return source.getEntity() instanceof Player;
    }

    private static void handleMobDeath(LivingEntity entity) {
        // Check if this is a Lootbound elite mob (from pack spawner)
        boolean isLootboundElite = EliteMobFactory.isElite(entity);

        // Lootbound elites always get elite tier drops with guaranteed drop
        if (isLootboundElite) {
            tryDropEquipment(entity, 1.0f, MobTier.ELITE); // 100% drop chance
            return;
        }

        // Get entity type ID (e.g., "minecraft:zombie" -> "zombie")
        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).getPath();

        if (COMMON_MOBS.contains(entityId)) {
            tryDropEquipment(entity, COMMON_MOB_DROP_CHANCE, MobTier.COMMON);
        } else if (COMMON_PLUS_MOBS.contains(entityId)) {
            tryDropEquipment(entity, COMMON_MOB_DROP_CHANCE + 0.01f, MobTier.COMMON);
        } else if (DANGEROUS_MOBS.contains(entityId)) {
            tryDropEquipment(entity, DANGEROUS_MOB_DROP_CHANCE, MobTier.DANGEROUS);
        } else if (ELITE_MOBS.contains(entityId)) {
            tryDropEquipment(entity, ELITE_MOB_DROP_CHANCE, MobTier.ELITE);
        }
    }

    private static void tryDropEquipment(LivingEntity entity, float dropChance, MobTier tier) {
        // Apply config multiplier
        float adjustedChance = LootboundConfig.get().applyDropMultiplier(dropChance);
        if (RANDOM.nextFloat() > adjustedChance) return;

        // Choose random equipment type
        ItemStack equipment = createRandomEquipment(tier);
        if (equipment.isEmpty()) return;

        // Apply random grade based on mob tier
        EquipmentGrade grade = rollGrade(tier);
        UpgradeSystem.setGrade(equipment, grade);

        // Small chance for pre-upgraded equipment from elite+ mobs
        if (tier.ordinal() >= MobTier.DANGEROUS.ordinal()) {
            int preLevel = rollPreLevel(tier);
            if (preLevel > 0) {
                UpgradeSystem.setLevel(equipment, preLevel);
            }
        }

        // Drop the item
        dropItem(entity, equipment);

        LootboundRpgMod.LOGGER.debug("Dropped {} {} from {}",
                grade.getName(), equipment.getItem().toString(), entity.getType().toString());
    }

    private static ItemStack createRandomEquipment(MobTier tier) {
        int roll = RANDOM.nextInt(100);

        return switch (tier) {
            case COMMON -> {
                // Mostly wooden/stone, some iron
                if (roll < 40) yield new ItemStack(Items.WOODEN_SWORD);
                else if (roll < 70) yield new ItemStack(Items.STONE_SWORD);
                else if (roll < 85) yield new ItemStack(Items.WOODEN_PICKAXE);
                else yield new ItemStack(Items.STONE_PICKAXE);
            }
            case DANGEROUS -> {
                // Iron equipment, rare gold/diamond
                if (roll < 35) yield new ItemStack(Items.IRON_SWORD);
                else if (roll < 60) yield new ItemStack(Items.IRON_PICKAXE);
                else if (roll < 75) yield new ItemStack(Items.STONE_SWORD);
                else if (roll < 90) yield new ItemStack(Items.GOLDEN_SWORD);
                else yield new ItemStack(Items.DIAMOND_SWORD);
            }
            case ELITE -> {
                // Diamond equipment, rare netherite
                if (roll < 30) yield new ItemStack(Items.DIAMOND_SWORD);
                else if (roll < 55) yield new ItemStack(Items.DIAMOND_PICKAXE);
                else if (roll < 75) yield new ItemStack(Items.IRON_SWORD);
                else if (roll < 90) yield new ItemStack(Items.IRON_PICKAXE);
                else yield new ItemStack(Items.NETHERITE_SWORD);
            }
        };
    }

    private static EquipmentGrade rollGrade(MobTier tier) {
        LootboundConfig config = LootboundConfig.get();
        int roll = RANDOM.nextInt(100);

        // Get weights from config based on tier
        int common, uncommon, rare, epic, legendary;
        switch (tier) {
            case COMMON -> {
                common = config.commonMobGradeCommon;
                uncommon = config.commonMobGradeUncommon;
                rare = config.commonMobGradeRare;
                epic = config.commonMobGradeEpic;
                legendary = config.commonMobGradeLegendary;
            }
            case DANGEROUS -> {
                common = config.dangerousMobGradeCommon;
                uncommon = config.dangerousMobGradeUncommon;
                rare = config.dangerousMobGradeRare;
                epic = config.dangerousMobGradeEpic;
                legendary = config.dangerousMobGradeLegendary;
            }
            case ELITE -> {
                common = config.eliteMobGradeCommon;
                uncommon = config.eliteMobGradeUncommon;
                rare = config.eliteMobGradeRare;
                epic = config.eliteMobGradeEpic;
                legendary = config.eliteMobGradeLegendary;
            }
            default -> {
                return EquipmentGrade.COMMON;
            }
        }

        // Apply legendary multiplier
        legendary = (int) (legendary * config.legendaryDropRateMultiplier);

        // Roll based on cumulative weights
        int cumCommon = common;
        int cumUncommon = cumCommon + uncommon;
        int cumRare = cumUncommon + rare;
        int cumEpic = cumRare + epic;
        // legendary is the rest

        if (roll < cumCommon) return EquipmentGrade.COMMON;
        else if (roll < cumUncommon) return EquipmentGrade.UNCOMMON;
        else if (roll < cumRare) return EquipmentGrade.RARE;
        else if (roll < cumEpic) return EquipmentGrade.EPIC;
        else return EquipmentGrade.LEGENDARY;
    }

    private static int rollPreLevel(MobTier tier) {
        int roll = RANDOM.nextInt(100);

        return switch (tier) {
            case COMMON -> 0;
            case DANGEROUS -> {
                // 85% +0, 10% +1, 4% +2, 1% +3
                if (roll < 85) yield 0;
                else if (roll < 95) yield 1;
                else if (roll < 99) yield 2;
                else yield 3;
            }
            case ELITE -> {
                // 70% +0, 15% +1, 8% +2, 5% +3, 2% +4
                if (roll < 70) yield 0;
                else if (roll < 85) yield 1;
                else if (roll < 93) yield 2;
                else if (roll < 98) yield 3;
                else yield 4;
            }
        };
    }

    private static void dropItem(LivingEntity entity, ItemStack stack) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            ItemEntity itemEntity = new ItemEntity(
                    serverLevel,
                    entity.getX(),
                    entity.getY() + 0.5,
                    entity.getZ(),
                    stack
            );
            itemEntity.setDefaultPickUpDelay();
            serverLevel.addFreshEntity(itemEntity);
        }
    }

    private enum MobTier {
        COMMON,
        DANGEROUS,
        ELITE
    }
}
