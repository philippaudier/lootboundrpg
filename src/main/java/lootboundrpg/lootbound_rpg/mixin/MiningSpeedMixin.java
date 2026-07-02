package lootboundrpg.lootbound_rpg.mixin;

import lootboundrpg.lootbound_rpg.upgrade.EquipmentScaling;
import lootboundrpg.lootbound_rpg.upgrade.UpgradeSystem;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to apply mining speed bonuses based on equipment level.
 *
 * Intercepts getDestroySpeed to multiply mining speed for upgraded pickaxes.
 */
@Mixin(ItemStack.class)
public abstract class MiningSpeedMixin {

    /**
     * Modifies mining speed based on equipment level and grade.
     * Only affects pickaxes with equipment level > 0.
     */
    @Inject(method = "getDestroySpeed", at = @At("RETURN"), cancellable = true)
    private void lootbound_rpg$applyMiningSpeedBonus(BlockState state, CallbackInfoReturnable<Float> cir) {
        ItemStack self = (ItemStack) (Object) this;

        // Only apply to pickaxes
        if (!self.is(ItemTags.PICKAXES)) {
            return;
        }

        int level = UpgradeSystem.getLevel(self);
        if (level <= 0) {
            return;
        }

        float originalSpeed = cir.getReturnValue();
        // Use the ItemStack-aware method that includes grade bonus
        double multiplier = EquipmentScaling.getMiningSpeedMultiplier(self);
        float newSpeed = (float) (originalSpeed * multiplier);

        cir.setReturnValue(newSpeed);
    }
}
