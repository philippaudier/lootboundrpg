package lootboundrpg.lootbound_rpg.mobpack;

import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;

import java.util.Set;

/**
 * Factory for creating elite versions of mobs.
 * Elite mobs have:
 * - Custom colored name displayed above them
 * - Increased health
 * - Increased damage
 * - Better drop tier (handled by existing drop system)
 */
public class EliteMobFactory {

    // Elite stat multipliers
    private static final double ZOMBIE_HEALTH_MULT = 1.5;   // +50% health
    private static final double ZOMBIE_DAMAGE_MULT = 1.2;   // +20% damage

    private static final double SKELETON_HEALTH_MULT = 1.4; // +40% health
    private static final double SKELETON_DAMAGE_MULT = 1.15; // +15% damage

    private static final double SPIDER_HEALTH_MULT = 1.3;   // +30% health
    private static final double SPIDER_SPEED_MULT = 1.1;    // +10% speed

    // Default multipliers for other mob types
    private static final double DEFAULT_HEALTH_MULT = 1.4;
    private static final double DEFAULT_DAMAGE_MULT = 1.15;

    // Mob type sets for classification
    private static final Set<String> ZOMBIE_TYPES = Set.of("zombie", "husk", "drowned");
    private static final Set<String> SKELETON_TYPES = Set.of("skeleton", "stray");
    private static final Set<String> SPIDER_TYPES = Set.of("spider", "cave_spider");

    /**
     * Marks a mob as elite, applying stat boosts and custom name.
     */
    public static void makeElite(Mob mob) {
        if (mob == null) return;

        // Get entity type ID
        String entityId = BuiltInRegistries.ENTITY_TYPE.getKey(mob.getType()).getPath();

        // Get multipliers based on entity type
        double healthMult;
        double damageMult;
        double speedMult = 1.0;

        if (ZOMBIE_TYPES.contains(entityId)) {
            healthMult = ZOMBIE_HEALTH_MULT;
            damageMult = ZOMBIE_DAMAGE_MULT;
        } else if (SKELETON_TYPES.contains(entityId)) {
            healthMult = SKELETON_HEALTH_MULT;
            damageMult = SKELETON_DAMAGE_MULT;
        } else if (SPIDER_TYPES.contains(entityId)) {
            healthMult = SPIDER_HEALTH_MULT;
            damageMult = 1.0; // Spiders don't have base attack damage attribute
            speedMult = SPIDER_SPEED_MULT;
        } else {
            healthMult = DEFAULT_HEALTH_MULT;
            damageMult = DEFAULT_DAMAGE_MULT;
        }

        // Apply health boost
        AttributeInstance healthAttr = mob.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            double newHealth = healthAttr.getBaseValue() * healthMult;
            healthAttr.setBaseValue(newHealth);
            mob.setHealth((float) newHealth); // Heal to full
        }

        // Apply damage boost
        if (damageMult > 1.0) {
            AttributeInstance damageAttr = mob.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damageAttr != null) {
                damageAttr.setBaseValue(damageAttr.getBaseValue() * damageMult);
            }
        }

        // Apply speed boost for spiders
        if (speedMult > 1.0) {
            AttributeInstance speedAttr = mob.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.setBaseValue(speedAttr.getBaseValue() * speedMult);
            }
        }

        // Set custom elite name
        String baseName = getBaseName(entityId);
        Component eliteName = Component.literal("")
                .append(Component.literal("[").withStyle(ChatFormatting.DARK_RED))
                .append(Component.literal("ELITE").withStyle(ChatFormatting.RED, ChatFormatting.BOLD))
                .append(Component.literal("] ").withStyle(ChatFormatting.DARK_RED))
                .append(Component.literal(baseName).withStyle(ChatFormatting.GOLD));

        mob.setCustomName(eliteName);
        mob.setCustomNameVisible(true);

        // Add persistence so elite doesn't despawn easily
        mob.setPersistenceRequired();
    }

    /**
     * Checks if a mob is an elite.
     * We identify elites by their custom name containing "ELITE".
     */
    public static boolean isElite(LivingEntity entity) {
        if (entity.hasCustomName()) {
            String name = entity.getCustomName().getString();
            return name.contains("ELITE");
        }
        return false;
    }

    /**
     * Gets the display name for a mob type ID.
     */
    private static String getBaseName(String entityId) {
        return switch (entityId) {
            case "zombie" -> "Zombie";
            case "husk" -> "Husk";
            case "drowned" -> "Drowned";
            case "skeleton" -> "Skeleton";
            case "stray" -> "Stray";
            case "spider" -> "Spider";
            case "cave_spider" -> "Cave Spider";
            case "creeper" -> "Creeper";
            case "enderman" -> "Enderman";
            case "witch" -> "Witch";
            default -> capitalize(entityId.replace("_", " "));
        };
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }
}
