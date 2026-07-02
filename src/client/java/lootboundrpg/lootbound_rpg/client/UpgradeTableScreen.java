package lootboundrpg.lootbound_rpg.client;

import lootboundrpg.lootbound_rpg.LootboundRpgMod;
import lootboundrpg.lootbound_rpg.config.LootboundConfig;
import lootboundrpg.lootbound_rpg.screen.UpgradeTableScreenHandler;
import lootboundrpg.lootbound_rpg.upgrade.EquipmentGrade;
import lootboundrpg.lootbound_rpg.upgrade.UpgradeSystem;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/**
 * Client-side screen for the Upgrade Table with BDO-style upgrade animation.
 */
public class UpgradeTableScreen extends AbstractContainerScreen<UpgradeTableScreenHandler> {

    private static final Identifier TEXTURE = LootboundRpgMod.id("textures/gui/upgrade_table.png");
    private static final int BACKGROUND_WIDTH = 256;
    private static final int BACKGROUND_HEIGHT = 256;

    // Animation constants
    private static final long ANIMATION_DURATION_MS = 2000; // 2 seconds of tension
    private static final int ARROW1_X = 45;
    private static final int ARROW1_WIDTH = 27;
    private static final int ARROW2_X = 101;
    private static final int ARROW2_WIDTH = 27;
    private static final int ARROW_Y = 56;
    private static final int ARROW_HEIGHT = 7;

    // Slot positions for grade halo (must match UpgradeTableScreenHandler)
    private static final int EQUIPMENT_SLOT_X = 23;
    private static final int EQUIPMENT_SLOT_Y = 51;
    private static final int RESULT_SLOT_X = 135;
    private static final int RESULT_SLOT_Y = 51;
    private static final int SLOT_SIZE = 16;

    private Button upgradeButton;

    // Animation state
    private boolean isAnimating = false;
    private long animationStartTime = 0;
    private ItemStack animatingEquipment = ItemStack.EMPTY;
    private int previousLevel = -1;

    // Status display
    private String statusMessage = "";
    private int statusColor = 0xFFFFFFFF;
    private long statusMessageTime = 0;

    public UpgradeTableScreen(UpgradeTableScreenHandler handler, Inventory inventory, Component title) {
        super(handler, inventory, title, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();

        // Hide default labels (we draw our own)
        this.titleLabelX = -9999;
        this.titleLabelY = -9999;
        this.inventoryLabelX = -9999;
        this.inventoryLabelY = -9999;

        // Upgrade button
        int buttonX = this.leftPos + 174;
        int buttonY = this.topPos + 87;
        this.upgradeButton = Button.builder(
                Component.translatable("lootbound_rpg.gui.upgrade"),
                button -> onUpgradeClick()
        ).bounds(buttonX, buttonY, 72, 16).build();

        this.addRenderableWidget(upgradeButton);
    }

    private void onUpgradeClick() {
        if (isAnimating) return;
        if (!menu.canUpgrade()) return;

        if (!menu.hasEnoughXp()) {
            setStatus("Not enough XP!", 0xFFFF5555);
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(SoundEvents.VILLAGER_NO, 0.5f, 1.0f);
            }
            return;
        }

        // Store current state to detect result
        ItemStack equipment = menu.getEquipment();
        previousLevel = UpgradeSystem.getLevel(equipment);

        // Save equipment for display during animation
        animatingEquipment = equipment.copy();

        // Start animation
        isAnimating = true;
        animationStartTime = System.currentTimeMillis();
        upgradeButton.active = false;

        // Play tension sound
        if (minecraft != null && minecraft.player != null) {
            minecraft.player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5f, 0.5f);
        }

        // Trigger server-side upgrade
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        // Handle animation
        if (isAnimating) {
            long elapsed = System.currentTimeMillis() - animationStartTime;

            // Animation complete
            if (elapsed >= ANIMATION_DURATION_MS) {
                isAnimating = false;

                // Detect result by checking if equipment level changed
                ItemStack currentEquip = menu.getEquipment();
                int currentLevel = UpgradeSystem.getLevel(currentEquip);
                boolean success = currentLevel > previousLevel;
                boolean downgrade = currentLevel < previousLevel;

                // Send collect button to server (moves item on success)
                if (minecraft != null && minecraft.gameMode != null) {
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 1);
                }

                if (success) {
                    setStatus("Enhancement successful!", 0xFF55FF55);
                } else if (downgrade) {
                    setStatus("DOWNGRADE! Level -1", 0xFFFF0000);
                } else {
                    setStatus("Enhancement failed!", 0xFFFF5555);
                }

                animatingEquipment = ItemStack.EMPTY;
            }
        }

        // Update button state
        if (!isAnimating) {
            boolean canUpgrade = menu.canUpgrade() && menu.hasEnoughXp();
            upgradeButton.active = canUpgrade;
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);

        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                TEXTURE,
                this.leftPos, this.topPos,
                0.0f, 0.0f,
                this.imageWidth, this.imageHeight,
                BACKGROUND_WIDTH, BACKGROUND_HEIGHT
        );

        // Draw grade halos behind equipment and result slots
        drawGradeHalos(graphics);

        // Draw animated progress bars on arrows
        if (isAnimating) {
            drawAnimatedArrows(graphics);
        }
    }

    private void drawAnimatedArrows(GuiGraphicsExtractor graphics) {
        long elapsed = System.currentTimeMillis() - animationStartTime;
        float progress = Math.min(1.0f, (float) elapsed / ANIMATION_DURATION_MS);

        // Pulsing color effect
        int pulse = (int) (Math.sin(elapsed / 100.0) * 30 + 225);
        int progressColor = (0xFF << 24) | (pulse << 8) | 50; // Green pulsing

        int arrow1X = this.leftPos + ARROW1_X;
        int arrow2X = this.leftPos + ARROW2_X;
        int arrowY = this.topPos + ARROW_Y;

        // First arrow fills from 0% to 50% progress
        if (progress <= 0.5f) {
            float arrow1Progress = progress * 2.0f; // 0 to 1 for first half
            int fillWidth = (int) (ARROW1_WIDTH * arrow1Progress);
            if (fillWidth > 0) {
                graphics.fill(arrow1X + 1, arrowY + 1, arrow1X + 1 + fillWidth, arrowY + ARROW_HEIGHT - 1, progressColor);
            }
        } else {
            // First arrow full
            graphics.fill(arrow1X + 1, arrowY + 1, arrow1X + ARROW1_WIDTH - 1, arrowY + ARROW_HEIGHT - 1, progressColor);

            // Second arrow fills from 50% to 100% progress
            float arrow2Progress = (progress - 0.5f) * 2.0f; // 0 to 1 for second half
            int fillWidth = (int) (ARROW2_WIDTH * arrow2Progress);
            if (fillWidth > 0) {
                graphics.fill(arrow2X + 1, arrowY + 1, arrow2X + 1 + fillWidth, arrowY + ARROW_HEIGHT - 1, progressColor);
            }
        }

        // Cover result slot and show "?"
        int resultSlotX = this.leftPos + RESULT_SLOT_X;
        int resultSlotY = this.topPos + RESULT_SLOT_Y;
        graphics.fill(resultSlotX, resultSlotY, resultSlotX + SLOT_SIZE, resultSlotY + SLOT_SIZE, 0xFF191920);

        // Draw pulsing "?" in result slot
        String question = "?";
        int qx = resultSlotX + (SLOT_SIZE / 2) - font.width(question) / 2;
        int qy = resultSlotY + (SLOT_SIZE / 2) - 4;
        int qColor = (0xFF << 24) | (pulse << 16) | (pulse << 8) | pulse;
        graphics.text(font, question, qx, qy, qColor, false);

        // Equipment slot stays visible - don't cover it during animation
        // The saved equipment reference ensures we can track state
    }

    /**
     * Draws grade-colored halos behind equipment and result slots.
     */
    private void drawGradeHalos(GuiGraphicsExtractor graphics) {
        if (!LootboundConfig.get().enableUpgradeTableGradeGlow) return;

        // Equipment slot halo
        ItemStack equipment = menu.getEquipment();
        if (!equipment.isEmpty() && UpgradeSystem.isUpgradeable(equipment)) {
            EquipmentGrade grade = UpgradeSystem.getGrade(equipment);
            drawSlotHalo(graphics, EQUIPMENT_SLOT_X, EQUIPMENT_SLOT_Y, grade);
        }

        // Result slot halo (only when not animating)
        if (!isAnimating) {
            ItemStack result = menu.getResult();
            if (!result.isEmpty() && UpgradeSystem.isUpgradeable(result)) {
                EquipmentGrade grade = UpgradeSystem.getGrade(result);
                drawSlotHalo(graphics, RESULT_SLOT_X, RESULT_SLOT_Y, grade);
            }
        }
    }

    /**
     * Draws a colored halo behind a slot based on grade.
     */
    private void drawSlotHalo(GuiGraphicsExtractor graphics, int slotX, int slotY, EquipmentGrade grade) {
        int color = getGradeHaloColor(grade);
        if (color == 0) return; // No halo for COMMON

        int x = this.leftPos + slotX;
        int y = this.topPos + slotY;

        // Draw a slightly larger rectangle behind the slot for the halo effect
        // Using 2-pixel padding around the 16x16 slot
        int padding = 2;
        graphics.fill(x - padding, y - padding, x + SLOT_SIZE + padding, y + SLOT_SIZE + padding, color);
    }

    /**
     * Gets the halo color for a grade (ARGB format).
     */
    private int getGradeHaloColor(EquipmentGrade grade) {
        long time = System.currentTimeMillis();

        return switch (grade) {
            case COMMON -> 0; // No halo
            case UNCOMMON -> 0x4055FF55; // Subtle green (25% alpha)
            case RARE -> 0x405555FF;     // Subtle blue (25% alpha)
            case EPIC -> 0x50AA55FF;     // Purple (31% alpha)
            case LEGENDARY -> {
                // Golden with subtle pulse
                int pulse = (int) (Math.sin(time / 300.0) * 20 + 60); // Alpha pulses 40-80
                yield (pulse << 24) | 0xFFD700; // Gold color with pulsing alpha
            }
        };
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        // Draw title
        String titleText = "Upgrade Table";
        int titleX = this.leftPos + (this.imageWidth - font.width(titleText)) / 2;
        graphics.text(font, titleText, titleX, this.topPos + 9, 0xFFE0E0E0, false);

        // Draw upgrade info
        if (!isAnimating) {
            extractUpgradeInfo(graphics);
        } else {
            // During animation, show "Enhancing..."
            int centerX = this.leftPos + 86;
            int msgY = this.topPos + 90;
            String enhancing = "Enhancing...";
            graphics.text(font, enhancing, centerX - font.width(enhancing) / 2, msgY, 0xFFFFFF00, false);
        }

        // Draw centered status message under slots
        drawStatusMessage(graphics);
    }

    private void extractUpgradeInfo(GuiGraphicsExtractor graphics) {
        ItemStack equipment = menu.getEquipment();
        ItemStack result = menu.getResult();

        // Info panel positions
        int panelX = this.leftPos + 175;
        int lineY1 = this.topPos + 35;
        int lineY2 = this.topPos + 53;
        int lineY3 = this.topPos + 71;

        // Check if there's an upgraded item in the result slot
        if (!result.isEmpty()) {
            int resultLevel = UpgradeSystem.getLevel(result);
            graphics.text(font, "+" + resultLevel + " Done!", panelX, lineY1, 0xFF55FF55, false);
            graphics.text(font, "Take item", panelX, lineY2, 0xFFAAAAAA, false);
            return;
        }

        if (!UpgradeSystem.isUpgradeable(equipment)) {
            graphics.text(font, "Insert item", panelX, lineY1, 0xFF808080, false);
            return;
        }

        int currentLevel = UpgradeSystem.getLevel(equipment);

        if (currentLevel >= UpgradeSystem.MAX_LEVEL) {
            graphics.text(font, "+10 MAX", panelX, lineY1, 0xFFFFAA00, false);
            return;
        }

        int targetLevel = currentLevel + 1;
        double chance = menu.getSuccessChance();
        double downgradeChance = menu.getDowngradeChance();
        int xpCost = menu.getXpCost();
        int chancePercent = (int) (chance * 100);
        int downgradePercent = (int) (downgradeChance * 100);

        // Line 1: Level progression
        graphics.text(font, "+" + currentLevel + " -> +" + targetLevel, panelX, lineY1, 0xFF55FF55, false);

        // Line 2: Success chance
        int chanceColor = chancePercent >= 50 ? 0xFF55FF55 : (chancePercent >= 25 ? 0xFFFFFF55 : 0xFFFF5555);
        graphics.text(font, "Chance: " + chancePercent + "%", panelX, lineY2, chanceColor, false);

        // Line 3: XP cost or Downgrade risk
        if (downgradePercent > 0) {
            // Show downgrade risk (more important than XP cost)
            graphics.text(font, "Risk: -1 " + downgradePercent + "%", panelX, lineY3, 0xFFFF5555, false);
        } else {
            int xpColor = menu.hasEnoughXp() ? 0xFFFFFFFF : 0xFFFF5555;
            graphics.text(font, "Cost: " + xpCost + " XP", panelX, lineY3, xpColor, false);
        }

        // Stone status - centered in box under button (box is 173-247, y=106-118)
        ItemStack stone = menu.getStone();
        int boxCenterX = this.leftPos + 210; // Center of the box (173+247)/2
        int statusPanelY = this.topPos + 109;
        String statusText;
        int statusColor;

        if (stone.isEmpty()) {
            statusText = "Need stone";
            statusColor = 0xFF808080;
        } else if (!UpgradeTableScreenHandler.isUpgradeStone(stone) ||
                   !UpgradeSystem.isValidStone(stone, targetLevel)) {
            statusText = "Wrong tier!";
            statusColor = 0xFFFF5555;
        } else {
            statusText = "Ready!";
            statusColor = 0xFF55FF55;
        }

        int textX = boxCenterX - font.width(statusText) / 2;
        graphics.text(font, statusText, textX, statusPanelY, statusColor, false);
    }

    private void drawStatusMessage(GuiGraphicsExtractor graphics) {
        if (statusMessage.isEmpty()) return;

        long elapsed = System.currentTimeMillis() - statusMessageTime;
        if (elapsed >= 3000) {
            statusMessage = "";
            return;
        }

        // Centered under the 3 slots
        int centerX = this.leftPos + 86; // Center of slot area
        int msgY = this.topPos + 90;
        int textWidth = font.width(statusMessage);

        graphics.text(font, statusMessage, centerX - textWidth / 2, msgY, statusColor, true);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        // Labels handled in extractRenderState
    }

    private void setStatus(String message, int color) {
        this.statusMessage = message;
        this.statusColor = color | 0xFF000000;
        this.statusMessageTime = System.currentTimeMillis();
    }
}
