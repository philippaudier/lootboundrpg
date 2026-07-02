package lootboundrpg.lootbound_rpg.client;

import lootboundrpg.lootbound_rpg.LootboundRpgMod;
import lootboundrpg.lootbound_rpg.config.LootboundConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Renders a BDO-style mob health bar when looking at a living entity.
 * Displays at the top center of the screen with the mob's name and health.
 */
public class LbMobHealthRenderer {

    // Colors (ARGB format) - matching LbHudRenderer theme
    private static final int COLOR_BACKGROUND_DARK = 0xCC0d0d0d;
    private static final int COLOR_BORDER_GOLD = 0xFFc9a227;
    private static final int COLOR_BORDER_DARK = 0xFF8b7355;

    // Health bar colors - red gradient
    private static final int COLOR_HEALTH_START = 0xFFcc2222;
    private static final int COLOR_HEALTH_END = 0xFFff4444;

    // Boss/Elite colors - purple gradient
    private static final int COLOR_BOSS_START = 0xFF8822cc;
    private static final int COLOR_BOSS_END = 0xFFaa44ff;

    // Layout constants
    private static final int BAR_WIDTH = 180;
    private static final int BAR_HEIGHT = 8;
    private static final int BORDER_WIDTH = 2;
    private static final int NAME_PADDING = 4;

    // Cache for smooth transitions
    private static Entity lastTarget = null;
    private static float displayHealth = 0f;
    private static long lastTargetTime = 0;
    private static final long FADE_DELAY_MS = 2000; // Keep showing for 2s after looking away

    /**
     * Registers the mob health bar renderer with Fabric's HudElementRegistry.
     */
    public static void register() {
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            LootboundRpgMod.id("lb_mob_health"),
            LbMobHealthRenderer::renderMobHealth
        );
    }

    /**
     * Checks if mob health bars are enabled.
     */
    public static boolean isEnabled() {
        return LootboundConfig.get().enableMobHealthBar;
    }

    /**
     * Main render callback for mob health bar.
     */
    private static void renderMobHealth(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        if (!isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        // Get the entity the player is looking at
        LivingEntity target = getTargetEntity(mc);
        long currentTime = System.currentTimeMillis();

        if (target != null) {
            lastTarget = target;
            lastTargetTime = currentTime;
            // Smoothly interpolate health display
            float targetHealth = target.getHealth();
            displayHealth = displayHealth + (targetHealth - displayHealth) * 0.3f;
        } else if (lastTarget != null && lastTarget.isAlive()) {
            // Keep showing last target for a brief moment
            if (currentTime - lastTargetTime > FADE_DELAY_MS) {
                lastTarget = null;
                return;
            }
            target = (LivingEntity) lastTarget;
            // Update health even when not looking
            float targetHealth = target.getHealth();
            displayHealth = displayHealth + (targetHealth - displayHealth) * 0.3f;
        } else {
            lastTarget = null;
            return;
        }

        // Don't show health bar for dead entities
        if (target.getHealth() <= 0) {
            lastTarget = null;
            return;
        }

        renderHealthBar(graphics, mc, target);
    }

    /**
     * Gets the living entity the player is looking at.
     */
    private static LivingEntity getTargetEntity(Minecraft mc) {
        HitResult hitResult = mc.hitResult;
        if (hitResult == null || hitResult.getType() != HitResult.Type.ENTITY) {
            return null;
        }

        EntityHitResult entityHit = (EntityHitResult) hitResult;
        Entity entity = entityHit.getEntity();

        // Only show for living entities, not players
        if (entity instanceof LivingEntity living && !(entity instanceof Player)) {
            return living;
        }

        return null;
    }

    /**
     * Renders the health bar for the target entity.
     */
    private static void renderHealthBar(GuiGraphicsExtractor graphics, Minecraft mc, LivingEntity target) {
        Font font = mc.font;
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        // Position at top center
        int centerX = screenWidth / 2;
        int startX = centerX - BAR_WIDTH / 2;
        int startY = 10;

        // Get mob info
        String mobName = target.getDisplayName().getString();
        float health = displayHealth;
        float maxHealth = target.getMaxHealth();
        float healthPercent = Math.clamp(health / maxHealth, 0f, 1f);

        // Check if this is a boss-type mob (high health)
        boolean isBoss = maxHealth > 100;

        // === MOB NAME ===
        int nameWidth = font.width(mobName);
        int nameX = centerX - nameWidth / 2;
        int nameY = startY;

        // Draw name with shadow
        graphics.text(font, mobName, nameX, nameY, 0xFFFFFFFF, true);

        // === HEALTH BAR ===
        int barY = startY + font.lineHeight + NAME_PADDING;

        // Draw frame
        drawBarFrame(graphics, startX, barY, BAR_WIDTH, BAR_HEIGHT);

        // Draw health fill
        int fillWidth = (int) ((BAR_WIDTH - BORDER_WIDTH * 2 - 2) * healthPercent);
        if (fillWidth > 0) {
            int startColor = isBoss ? COLOR_BOSS_START : COLOR_HEALTH_START;
            int endColor = isBoss ? COLOR_BOSS_END : COLOR_HEALTH_END;

            drawGradientBar(graphics,
                startX + BORDER_WIDTH + 1,
                barY + BORDER_WIDTH + 1,
                fillWidth,
                BAR_HEIGHT - BORDER_WIDTH * 2 - 2,
                startColor, endColor);
        }

        // === HEALTH TEXT (optional, for bosses) ===
        if (isBoss) {
            String healthText = (int) health + "/" + (int) maxHealth;
            int textWidth = font.width(healthText);
            int textX = centerX - textWidth / 2;
            int textY = barY + BAR_HEIGHT + 2;
            graphics.text(font, healthText, textX, textY, 0xFFAAAAAA, true);
        }
    }

    private static void drawBarFrame(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        // Outer gold border
        graphics.fill(x, y, x + width, y + height, COLOR_BORDER_GOLD);
        // Inner dark border
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, COLOR_BORDER_DARK);
        // Dark background
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
        // Highlight on top
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
}
