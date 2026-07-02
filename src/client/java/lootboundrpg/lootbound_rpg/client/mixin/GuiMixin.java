package lootboundrpg.lootbound_rpg.client.mixin;

import lootboundrpg.lootbound_rpg.client.LbHudRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Mixin to hide vanilla HUD elements when Lootbound HUD is enabled.
 * Only hides health, hunger, and XP bars - hotbar remains unchanged.
 */
@Mixin(Gui.class)
public class GuiMixin {

    /**
     * Cancels vanilla health bar rendering when Lb HUD is enabled.
     * require = 0 makes this optional if method name differs in this MC version.
     */
    @Inject(method = "renderPlayerHealth", at = @At("HEAD"), cancellable = true, require = 0)
    private void lootbound_cancelPlayerHealth(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (LbHudRenderer.isEnabled()) {
            ci.cancel();
        }
    }

    /**
     * Cancels vanilla food bar rendering when Lb HUD is enabled.
     * require = 0 makes this optional if method name differs in this MC version.
     */
    @Inject(method = "renderFood", at = @At("HEAD"), cancellable = true, require = 0)
    private void lootbound_cancelFood(GuiGraphicsExtractor graphics, int x, int y, CallbackInfo ci) {
        if (LbHudRenderer.isEnabled()) {
            ci.cancel();
        }
    }

    /**
     * Cancels vanilla experience bar rendering when Lb HUD is enabled.
     * require = 0 makes this optional if method name differs in this MC version.
     */
    @Inject(method = "renderExperienceBar", at = @At("HEAD"), cancellable = true, require = 0)
    private void lootbound_cancelExperienceBar(GuiGraphicsExtractor graphics, int x, CallbackInfo ci) {
        if (LbHudRenderer.isEnabled()) {
            ci.cancel();
        }
    }
}
