package lootboundrpg.lootbound_rpg.client;

import lootboundrpg.lootbound_rpg.LootboundRpgMod;
import lootboundrpg.lootbound_rpg.config.LootboundConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;

/**
 * Custom Lootbound-style HUD renderer.
 * Renders stylized status bars (health, hunger, XP) in the top-left corner.
 * Registered via HudElementRegistry.
 */
public class LbHudRenderer {

    // Colors (ARGB format)
    private static final int COLOR_BACKGROUND_DARK = 0xCC0d0d0d;
    private static final int COLOR_BORDER_GOLD = 0xFFc9a227;
    private static final int COLOR_BORDER_DARK = 0xFF8b7355;

    // Health bar colors
    private static final int COLOR_HEALTH_START = 0xFFcc2222;
    private static final int COLOR_HEALTH_END = 0xFFff4444;
    private static final int COLOR_HEALTH_LOW_START = 0xFF881111;
    private static final int COLOR_HEALTH_LOW_END = 0xFFcc2222;

    // Hunger bar colors
    private static final int COLOR_HUNGER_START = 0xFFcc8822;
    private static final int COLOR_HUNGER_END = 0xFFffaa33;

    // Layout constants
    private static final int BAR_WIDTH = 120;
    private static final int BAR_HEIGHT = 12;
    private static final int BAR_PADDING = 4;
    private static final int BORDER_WIDTH = 2;

    /**
     * Registers the HUD renderer with Fabric's HudElementRegistry.
     */
    public static void register() {
        // Attach our HUD element before the chat (renders on top of game, before chat)
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            LootboundRpgMod.id("lb_status_bars"),
            LbHudRenderer::renderStatusBars
        );
    }

    /**
     * Checks if the custom HUD is enabled.
     */
    public static boolean isEnabled() {
        return LootboundConfig.get().enableLbHud;
    }

    /**
     * Main render callback for status bars.
     */
    private static void renderStatusBars(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        if (!isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        if (mc.player.isSpectator()) return;

        Player player = mc.player;
        Font font = mc.font;

        int startX = 10;
        int startY = 10;

        // === HEALTH BAR ===
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        float healthPercent = Math.clamp(health / maxHealth, 0f, 1f);
        boolean lowHealth = healthPercent < 0.3f;

        drawBarFrame(graphics, startX, startY, BAR_WIDTH, BAR_HEIGHT);

        int fillWidth = (int) ((BAR_WIDTH - BORDER_WIDTH * 2 - 2) * healthPercent);
        if (fillWidth > 0) {
            int healthStartColor = lowHealth ? COLOR_HEALTH_LOW_START : COLOR_HEALTH_START;
            int healthEndColor = lowHealth ? COLOR_HEALTH_LOW_END : COLOR_HEALTH_END;

            // Pulsing effect for low health
            if (lowHealth) {
                long time = System.currentTimeMillis();
                float pulse = (float) (Math.sin(time / 150.0) * 0.3 + 0.7);
                healthStartColor = modulateAlpha(healthStartColor, pulse);
                healthEndColor = modulateAlpha(healthEndColor, pulse);
            }

            drawGradientBar(graphics,
                startX + BORDER_WIDTH + 1,
                startY + BORDER_WIDTH + 1,
                fillWidth,
                BAR_HEIGHT - BORDER_WIDTH * 2 - 2,
                healthStartColor, healthEndColor);
        }

        String healthText = (int) health + "/" + (int) maxHealth;
        int textX = startX + BAR_WIDTH / 2 - font.width(healthText) / 2;
        int textY = startY + (BAR_HEIGHT - 8) / 2;
        graphics.text(font, healthText, textX, textY, 0xFFFFFFFF, true);

        // === HUNGER BAR ===
        int hungerY = startY + BAR_HEIGHT + BAR_PADDING;
        int foodLevel = player.getFoodData().getFoodLevel();
        float hungerPercent = foodLevel / 20f;

        drawBarFrame(graphics, startX, hungerY, BAR_WIDTH, BAR_HEIGHT);

        int hungerFillWidth = (int) ((BAR_WIDTH - BORDER_WIDTH * 2 - 2) * hungerPercent);
        if (hungerFillWidth > 0) {
            drawGradientBar(graphics,
                startX + BORDER_WIDTH + 1,
                hungerY + BORDER_WIDTH + 1,
                hungerFillWidth,
                BAR_HEIGHT - BORDER_WIDTH * 2 - 2,
                COLOR_HUNGER_START, COLOR_HUNGER_END);
        }

        String hungerText = foodLevel + "/20";
        textX = startX + BAR_WIDTH / 2 - font.width(hungerText) / 2;
        textY = hungerY + (BAR_HEIGHT - 8) / 2;
        graphics.text(font, hungerText, textX, textY, 0xFFFFFFFF, true);

        // === XP BAR ===
        int xpY = hungerY + BAR_HEIGHT + BAR_PADDING;
        float xpPercent = player.experienceProgress;
        int xpLevel = player.experienceLevel;
        int xpBarHeight = 8;

        drawBarFrame(graphics, startX, xpY, BAR_WIDTH, xpBarHeight);

        int xpFillWidth = (int) ((BAR_WIDTH - BORDER_WIDTH * 2 - 2) * xpPercent);
        if (xpFillWidth > 0) {
            drawGradientBar(graphics,
                startX + BORDER_WIDTH + 1,
                xpY + BORDER_WIDTH + 1,
                xpFillWidth,
                xpBarHeight - BORDER_WIDTH * 2 - 2,
                0xFF22aa22, 0xFF44ff44);
        }

        if (xpLevel > 0) {
            String xpText = "Lv." + xpLevel;
            graphics.text(font, xpText, startX + BAR_WIDTH + 5, xpY, 0xFF44ff44, true);
        }
    }

    private static void drawBarFrame(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, COLOR_BORDER_GOLD);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, COLOR_BORDER_DARK);
        graphics.fill(x + BORDER_WIDTH, y + BORDER_WIDTH,
            x + width - BORDER_WIDTH, y + height - BORDER_WIDTH, COLOR_BACKGROUND_DARK);
    }

    private static void drawGradientBar(GuiGraphicsExtractor graphics, int x, int y, int width, int height,
                                         int colorStart, int colorEnd) {
        for (int i = 0; i < width; i++) {
            float t = (float) i / width;
            int color = lerpColor(colorStart, colorEnd, t);
            graphics.fill(x + i, y, x + i + 1, y + height, color);
        }
        graphics.fill(x, y, x + width, y + 1, 0x33FFFFFF);
    }

    private static int lerpColor(int color1, int color2, float t) {
        int a1 = (color1 >> 24) & 0xFF, r1 = (color1 >> 16) & 0xFF;
        int g1 = (color1 >> 8) & 0xFF, b1 = color1 & 0xFF;
        int a2 = (color2 >> 24) & 0xFF, r2 = (color2 >> 16) & 0xFF;
        int g2 = (color2 >> 8) & 0xFF, b2 = color2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * t), r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t), b = (int) (b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int modulateAlpha(int color, float factor) {
        int a = (int) (((color >> 24) & 0xFF) * factor);
        return (a << 24) | (color & 0x00FFFFFF);
    }
}
