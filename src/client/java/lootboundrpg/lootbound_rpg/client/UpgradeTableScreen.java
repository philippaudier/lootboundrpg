package lootboundrpg.lootbound_rpg.client;

import lootboundrpg.lootbound_rpg.LootboundRpgMod;
import lootboundrpg.lootbound_rpg.screen.UpgradeTableScreenHandler;
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
 * Client-side screen for the Upgrade Table.
 *
 * Layout (190 pixels tall):
 * - Title centered at top
 * - Equipment and Stone slots with arrow at y=35
 * - Info (level, chance, XP) on right side
 * - Upgrade button below info
 * - Status message centered below slots
 * - Player inventory at bottom
 */
public class UpgradeTableScreen extends AbstractContainerScreen<UpgradeTableScreenHandler> {

    private static final Identifier TEXTURE = LootboundRpgMod.id("textures/gui/upgrade_table.png");
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 256;

    private Button upgradeButton;
    private String statusMessage = "";
    private int statusColor = 0xFFFFFFFF;
    private long statusMessageTime = 0;

    // For detecting upgrade results
    private boolean waitingForResult = false;
    private int previousLevel = -1;
    private int previousStoneCount = -1;

    public UpgradeTableScreen(UpgradeTableScreenHandler handler, Inventory inventory, Component title) {
        // GUI size: 176x210 (taller to fit everything with proper spacing)
        super(handler, inventory, title, 176, 210);
    }

    @Override
    protected void init() {
        super.init();

        // Center the title
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        // Inventory label position
        this.inventoryLabelY = 118;

        // Upgrade button - on the right side, vertically centered with slots
        int buttonX = this.leftPos + 98;
        int buttonY = this.topPos + 32;
        this.upgradeButton = Button.builder(
                Component.translatable("lootbound_rpg.gui.upgrade"),
                button -> onUpgradeClick()
        ).bounds(buttonX, buttonY, 60, 20).build();

        this.addRenderableWidget(upgradeButton);
    }

    private void onUpgradeClick() {
        if (!menu.canUpgrade()) {
            return;
        }

        if (!menu.hasEnoughXp()) {
            setStatus(Component.translatable("lootbound_rpg.gui.not_enough_xp").getString(), 0xFF5555);
            if (minecraft != null && minecraft.player != null) {
                minecraft.player.playSound(SoundEvents.VILLAGER_NO, 0.5f, 1.0f);
            }
            return;
        }

        // Store current state to detect result
        ItemStack equipment = menu.getEquipment();
        ItemStack stone = menu.getStone();
        previousLevel = UpgradeSystem.getLevel(equipment);
        previousStoneCount = stone.getCount();
        waitingForResult = true;

        // Use clickMenuButton to trigger server-side upgrade
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, 0);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        // Update button state
        boolean canUpgrade = menu.canUpgrade() && menu.hasEnoughXp();
        upgradeButton.active = canUpgrade;

        // Detect upgrade result
        if (waitingForResult) {
            ItemStack equipment = menu.getEquipment();
            ItemStack stone = menu.getStone();
            int currentStoneCount = stone.getCount();

            // Check if stone was consumed
            if (currentStoneCount < previousStoneCount || (previousStoneCount > 0 && stone.isEmpty())) {
                int currentLevel = UpgradeSystem.getLevel(equipment);

                if (currentLevel > previousLevel) {
                    // SUCCESS
                    setStatus(Component.translatable("lootbound_rpg.gui.success").getString(), 0x55FF55);
                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.playSound(SoundEvents.ENCHANTMENT_TABLE_USE, 1.0f, 1.2f);
                    }
                } else {
                    // FAILURE
                    setStatus(Component.translatable("lootbound_rpg.gui.failure").getString(), 0xFF5555);
                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.playSound(SoundEvents.GLASS_BREAK, 0.8f, 0.8f);
                    }
                }

                waitingForResult = false;
            }
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
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        extractUpgradeInfo(graphics);
    }

    private void extractUpgradeInfo(GuiGraphicsExtractor graphics) {
        ItemStack equipment = menu.getEquipment();

        // Info below the slots, centered
        int centerX = this.leftPos + this.imageWidth / 2;
        int infoY = this.topPos + 58;

        if (!UpgradeSystem.isUpgradeable(equipment)) {
            // Hint when no equipment
            String hint = "Place equipment to upgrade";
            graphics.text(font, hint, centerX - font.width(hint) / 2, infoY, 0xFF404040, false);
            return;
        }

        int currentLevel = UpgradeSystem.getLevel(equipment);

        if (currentLevel >= UpgradeSystem.MAX_LEVEL) {
            String maxText = "MAX LEVEL REACHED!";
            graphics.text(font, maxText, centerX - font.width(maxText) / 2, infoY, 0xFFFFAA00, false);
            return;
        }

        int targetLevel = currentLevel + 1;
        double chance = menu.getSuccessChance();
        int xpCost = menu.getXpCost();

        // Info line: "+X -> +Y | XX% | X XP" centered
        int chancePercent = (int) (chance * 100);
        String levelText = "+" + currentLevel + " -> +" + targetLevel;
        String chanceText = chancePercent + "%";
        String xpText = xpCost + " XP";

        int chanceColor = chancePercent >= 50 ? 0xFF00AA00 : (chancePercent >= 25 ? 0xFFAAAA00 : 0xFFAA0000);
        int xpColor = menu.hasEnoughXp() ? 0xFF00AA00 : 0xFFAA0000;

        // Draw each part with spacing
        int totalWidth = font.width(levelText) + 10 + font.width(chanceText) + 10 + font.width(xpText);
        int startX = centerX - totalWidth / 2;

        graphics.text(font, levelText, startX, infoY, 0xFF404040, false);
        startX += font.width(levelText) + 10;
        graphics.text(font, chanceText, startX, infoY, chanceColor, false);
        startX += font.width(chanceText) + 10;
        graphics.text(font, xpText, startX, infoY, xpColor, false);

        // Stone warning - centered below info
        ItemStack stone = menu.getStone();
        int warningY = this.topPos + 72;
        if (stone.isEmpty()) {
            String warning = "Insert upgrade stone";
            graphics.text(font, warning, centerX - font.width(warning) / 2, warningY, 0xFF808080, false);
        } else if (!UpgradeTableScreenHandler.isUpgradeStone(stone) ||
                   !UpgradeSystem.isValidStone(stone, targetLevel)) {
            String warning = "Wrong stone tier!";
            graphics.text(font, warning, centerX - font.width(warning) / 2, warningY, 0xFFAA0000, false);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        // Title (centered)
        graphics.text(font, this.title, this.titleLabelX, this.titleLabelY, 0xFF404040, false);
        // Inventory label
        graphics.text(font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 0xFF404040, false);

        // Status message (centered, in dedicated area)
        if (!statusMessage.isEmpty()) {
            long elapsed = System.currentTimeMillis() - statusMessageTime;
            if (elapsed < 3000) {
                int textWidth = font.width(statusMessage);
                int textX = (this.imageWidth - textWidth) / 2;
                int textY = 90; // Clear area for status
                graphics.text(font, statusMessage, textX, textY, statusColor, true);
            } else {
                statusMessage = "";
            }
        }
    }

    private void setStatus(String message, int color) {
        this.statusMessage = message;
        this.statusColor = color | 0xFF000000;
        this.statusMessageTime = System.currentTimeMillis();
    }
}
