package lootboundrpg.lootbound_rpg.mixin;

import lootboundrpg.lootbound_rpg.upgrade.EquipmentScaling;
import lootboundrpg.lootbound_rpg.upgrade.UpgradeSystem;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Mixin to apply attack damage bonuses from upgraded swords.
 *
 * Intercepts the attack method to increase damage based on equipment level and grade.
 */
@Mixin(Player.class)
public abstract class PlayerAttackMixin {

    /**
     * Modifies the damage variable in the attack method.
     * Adds bonus damage based on the sword's equipment level and grade.
     */
    @ModifyVariable(
            method = "attack",
            at = @At("STORE"),
            ordinal = 0
    )
    private float lootbound_rpg$addBonusDamage(float originalDamage) {
        Player self = (Player) (Object) this;
        ItemStack weapon = self.getMainHandItem();

        // Only apply bonus to swords
        if (!weapon.is(ItemTags.SWORDS)) {
            return originalDamage;
        }

        int level = UpgradeSystem.getLevel(weapon);
        if (level <= 0) {
            return originalDamage;
        }

        // Use the ItemStack-aware method that includes grade bonus
        double bonus = EquipmentScaling.getAttackDamageBonus(weapon);
        return originalDamage + (float) bonus;
    }
}
