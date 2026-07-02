package lootboundrpg.lootbound_rpg.mixin;

import lootboundrpg.lootbound_rpg.component.ModDataComponents;
import lootboundrpg.lootbound_rpg.upgrade.EquipmentGrade;
import lootboundrpg.lootbound_rpg.upgrade.UpgradeSystem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Mixin to modify item display names to show equipment level and grade.
 *
 * For upgradeable items:
 * - Colors the entire name based on grade (COMMON=white, RARE=blue, etc.)
 * - Appends " +X" when level > 0
 *
 * Example: "Iron Sword +3" in blue for RARE grade
 */
@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    /**
     * Injects at the end of getHoverName to modify the display name.
     */
    @Inject(method = "getHoverName", at = @At("RETURN"), cancellable = true)
    private void lootbound_rpg$modifyEquipmentName(CallbackInfoReturnable<Component> cir) {
        ItemStack self = (ItemStack) (Object) this;

        // Only modify upgradeable items
        if (!UpgradeSystem.isUpgradeable(self)) {
            return;
        }

        int level = UpgradeSystem.getLevel(self);
        EquipmentGrade grade = UpgradeSystem.getGrade(self);

        Component originalName = cir.getReturnValue();

        // Build the new name with grade color
        MutableComponent newName = Component.literal("")
                .withStyle(grade.getColor());

        // Copy the original name text with the grade color
        newName.append(originalName.copy().withStyle(grade.getColor()));

        // Append level if > 0
        if (level > 0) {
            newName.append(Component.literal(" +" + level).withStyle(grade.getColor()));
        }

        cir.setReturnValue(newName);
    }
}
