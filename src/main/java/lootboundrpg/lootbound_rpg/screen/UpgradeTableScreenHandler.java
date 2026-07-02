package lootboundrpg.lootbound_rpg.screen;

import lootboundrpg.lootbound_rpg.block.entity.UpgradeTableBlockEntity;
import lootboundrpg.lootbound_rpg.config.LootboundConfig;
import lootboundrpg.lootbound_rpg.item.ModItems;
import lootboundrpg.lootbound_rpg.registry.ModScreenHandlers;
import lootboundrpg.lootbound_rpg.upgrade.UpgradeSystem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Screen handler for the Upgrade Table.
 *
 * Manages the inventory slots and upgrade logic.
 */
public class UpgradeTableScreenHandler extends AbstractContainerMenu {

    private final Container container;
    private final Player player;

    // Pending upgrade result (set by performUpgrade, used by collectResult)
    private UpgradeSystem.UpgradeResult pendingResult = null;

    // Slot indices
    public static final int SLOT_EQUIPMENT = 0;
    public static final int SLOT_STONE = 1;
    public static final int SLOT_RESULT = 2;
    public static final int PLAYER_INVENTORY_START = 3;
    public static final int PLAYER_INVENTORY_END = 30;  // 27 slots
    public static final int PLAYER_HOTBAR_START = 30;
    public static final int PLAYER_HOTBAR_END = 39;     // 9 slots

    // Constructor for client (from network)
    public UpgradeTableScreenHandler(int syncId, Inventory playerInventory) {
        this(syncId, playerInventory, new SimpleContainer(UpgradeTableBlockEntity.INVENTORY_SIZE));
    }

    // Constructor for server (from block entity)
    public UpgradeTableScreenHandler(int syncId, Inventory playerInventory, Container container) {
        super(ModScreenHandlers.UPGRADE_TABLE, syncId);
        this.container = container;
        this.player = playerInventory.player;

        checkContainerSize(container, UpgradeTableBlockEntity.INVENTORY_SIZE);

        // Equipment slot (slot 0) - only accepts upgradeable items
        // Position matches texture: inner slot at (23, 51)
        this.addSlot(new Slot(container, SLOT_EQUIPMENT, 23, 51) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return UpgradeSystem.isUpgradeable(stack);
            }

            @Override
            public int getMaxStackSize() {
                return 1; // Only one item at a time
            }
        });

        // Stone slot (slot 1) - only accepts upgrade stones
        // Position matches texture: x=79, y=51
        this.addSlot(new Slot(container, SLOT_STONE, 79, 51) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return isUpgradeStone(stack);
            }
        });

        // Result slot (slot 2) - output only, cannot place items
        // Position matches texture: x=135, y=51
        this.addSlot(new Slot(container, SLOT_RESULT, 135, 51) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // Cannot place items in result slot
            }
        });

        // Player inventory (3 rows of 9) - 256x256 GUI
        // Position matches texture: starts at x=48, y=146
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 48 + col * 18, 146 + row * 18));
            }
        }

        // Player hotbar - at y=206
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(playerInventory, col, 48 + col * 18, 206));
        }
    }

    /**
     * Checks if an item is an upgrade stone.
     */
    public static boolean isUpgradeStone(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item == ModItems.CRUDE_UPGRADE_STONE ||
               item == ModItems.REFINED_UPGRADE_STONE ||
               item == ModItems.RARE_UPGRADE_STONE ||
               item == ModItems.PERFECT_UPGRADE_STONE;
    }

    /**
     * Gets the equipment in the table.
     */
    public ItemStack getEquipment() {
        return container.getItem(SLOT_EQUIPMENT);
    }

    /**
     * Gets the stone in the table.
     */
    public ItemStack getStone() {
        return container.getItem(SLOT_STONE);
    }

    /**
     * Gets the result item (upgraded equipment after success).
     */
    public ItemStack getResult() {
        return container.getItem(SLOT_RESULT);
    }

    /**
     * Checks if the result slot is empty (ready for upgrade).
     */
    public boolean isResultSlotEmpty() {
        return container.getItem(SLOT_RESULT).isEmpty();
    }

    /**
     * Checks if an upgrade can be attempted.
     */
    public boolean canUpgrade() {
        ItemStack equipment = getEquipment();
        ItemStack stone = getStone();

        if (!UpgradeSystem.isUpgradeable(equipment)) return false;
        if (stone.isEmpty()) return false;
        if (!isResultSlotEmpty()) return false; // Result slot must be empty

        int currentLevel = UpgradeSystem.getLevel(equipment);
        if (currentLevel >= UpgradeSystem.MAX_LEVEL) return false;

        int targetLevel = currentLevel + 1;
        return UpgradeSystem.isValidStone(stone, targetLevel);
    }

    /**
     * Gets the XP cost for the current upgrade.
     */
    public int getXpCost() {
        ItemStack equipment = getEquipment();
        if (!UpgradeSystem.isUpgradeable(equipment)) return 0;

        int targetLevel = UpgradeSystem.getLevel(equipment) + 1;
        return UpgradeSystem.getXpCost(targetLevel);
    }

    /**
     * Gets the success chance for the current upgrade.
     */
    public double getSuccessChance() {
        ItemStack equipment = getEquipment();
        if (!UpgradeSystem.isUpgradeable(equipment)) return 0;

        int targetLevel = UpgradeSystem.getLevel(equipment) + 1;
        return UpgradeSystem.getSuccessChance(targetLevel);
    }

    /**
     * Gets the downgrade chance for the current upgrade.
     */
    public double getDowngradeChance() {
        ItemStack equipment = getEquipment();
        if (!UpgradeSystem.isUpgradeable(equipment)) return 0;

        int targetLevel = UpgradeSystem.getLevel(equipment) + 1;
        return UpgradeSystem.getDowngradeChance(targetLevel);
    }

    /**
     * Checks if the player has enough XP.
     */
    public boolean hasEnoughXp() {
        if (player.getAbilities().instabuild) return true; // Creative mode
        if (!LootboundConfig.get().upgradeConsumesXp) return true; // XP disabled
        return player.experienceLevel >= getXpCost();
    }

    /**
     * Performs the upgrade attempt.
     * Does NOT move the item - call collectResult after animation to move on success.
     * @return The result of the upgrade
     */
    public UpgradeSystem.UpgradeResult performUpgrade() {
        if (!canUpgrade()) {
            return UpgradeSystem.UpgradeResult.INVALID_ITEM;
        }

        if (!hasEnoughXp()) {
            return UpgradeSystem.UpgradeResult.INVALID_STONE;
        }

        ItemStack equipment = getEquipment();
        ItemStack stone = getStone();
        int xpCost = getXpCost();

        // Consume XP (unless creative or XP consumption disabled)
        if (!player.getAbilities().instabuild && LootboundConfig.get().upgradeConsumesXp) {
            player.giveExperienceLevels(-xpCost);
        }

        // Attempt the upgrade (this consumes the stone, modifies equipment in place)
        UpgradeSystem.UpgradeResult result = UpgradeSystem.attemptUpgrade(equipment, stone);

        // Store result for collection phase
        pendingResult = result;

        // Notify the container changed
        container.setChanged();

        return result;
    }

    /**
     * Collects the upgrade result - moves item to result slot if success.
     * Called by client after animation completes.
     */
    public void collectResult() {
        if (pendingResult == UpgradeSystem.UpgradeResult.SUCCESS) {
            ItemStack equipment = getEquipment();
            if (!equipment.isEmpty()) {
                container.setItem(SLOT_RESULT, equipment.copy());
                container.setItem(SLOT_EQUIPMENT, ItemStack.EMPTY);
                container.setChanged();
            }
        }
        pendingResult = null;
    }

    /**
     * Check if there's a pending successful upgrade to collect.
     */
    public boolean hasPendingSuccess() {
        return pendingResult == UpgradeSystem.UpgradeResult.SUCCESS;
    }

    /**
     * Check if there's a pending downgrade (failed AND level decreased).
     */
    public boolean hasPendingDowngrade() {
        return pendingResult == UpgradeSystem.UpgradeResult.DOWNGRADE;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotIndex);

        if (slot.hasItem()) {
            ItemStack originalStack = slot.getItem();
            newStack = originalStack.copy();

            // Moving from upgrade table slots to player inventory
            if (slotIndex < PLAYER_INVENTORY_START) {
                if (!this.moveItemStackTo(originalStack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_END, true)) {
                    return ItemStack.EMPTY;
                }
            }
            // Moving from player inventory to upgrade table
            else {
                // Try equipment slot first
                if (UpgradeSystem.isUpgradeable(originalStack)) {
                    if (!this.moveItemStackTo(originalStack, SLOT_EQUIPMENT, SLOT_EQUIPMENT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // Try stone slot
                else if (isUpgradeStone(originalStack)) {
                    if (!this.moveItemStackTo(originalStack, SLOT_STONE, SLOT_STONE + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                }
                // Move between inventory and hotbar
                else if (slotIndex < PLAYER_HOTBAR_START) {
                    if (!this.moveItemStackTo(originalStack, PLAYER_HOTBAR_START, PLAYER_HOTBAR_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(originalStack, PLAYER_INVENTORY_START, PLAYER_HOTBAR_START, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }

            if (originalStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return newStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return container.stillValid(player);
    }

    /**
     * Handles button clicks from the client.
     * Button ID 0 = start upgrade (performs upgrade, no movement)
     * Button ID 1 = collect result (moves item if success, plays feedback)
     */
    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == 0) {
            // Start upgrade - perform upgrade but don't move item yet
            UpgradeSystem.UpgradeResult result = performUpgrade();
            return result == UpgradeSystem.UpgradeResult.SUCCESS ||
                   result == UpgradeSystem.UpgradeResult.FAILURE ||
                   result == UpgradeSystem.UpgradeResult.DOWNGRADE;
        } else if (buttonId == 1) {
            // Collect result - move item and play feedback
            int targetLevel = UpgradeSystem.getLevel(getEquipment()) + 1;
            boolean wasSuccess = hasPendingSuccess();
            boolean wasDowngrade = hasPendingDowngrade();

            collectResult();

            // Play feedback sounds and particles
            if (wasSuccess) {
                playSuccessFeedback(player, targetLevel);
            } else if (wasDowngrade) {
                playDowngradeFeedback(player);
            } else {
                playFailureFeedback(player);
            }

            return true;
        }
        return false;
    }

    /**
     * Plays success feedback (sounds and particles) based on the upgrade level.
     * +1 to +3: Simple feedback
     * +4 to +6: More pronounced feedback
     * +7 to +9: Rare achievement feedback
     * +10: Special celebration
     */
    private void playSuccessFeedback(Player player, int newLevel) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        double x = player.getX();
        double y = player.getY() + 1.0;
        double z = player.getZ();

        if (newLevel <= 3) {
            // Simple: anvil sound + few enchant particles
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.ANVIL_USE,
                    SoundSource.BLOCKS, 0.8f, 1.2f);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, x, y, z, 10, 0.5, 0.5, 0.5, 0.1);

        } else if (newLevel <= 6) {
            // Pronounced: enchantment table sound + more particles
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE,
                    SoundSource.BLOCKS, 1.0f, 1.0f);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, x, y, z, 25, 0.6, 0.6, 0.6, 0.15);
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 8, 0.4, 0.4, 0.4, 0.0);

        } else if (newLevel <= 9) {
            // Rare: achievement sound + firework particles
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                    SoundSource.PLAYERS, 1.0f, 1.0f);
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 30, 0.5, 0.5, 0.5, 0.3);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, x, y, z, 40, 0.8, 0.8, 0.8, 0.2);

        } else {
            // +10 Special: level up + challenge complete + big particle burst
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS, 1.0f, 1.0f);
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE,
                    SoundSource.PLAYERS, 1.0f, 0.8f);
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, x, y, z, 60, 0.8, 1.0, 0.8, 0.5);
            serverLevel.sendParticles(ParticleTypes.END_ROD, x, y, z, 30, 0.6, 0.8, 0.6, 0.1);
            serverLevel.sendParticles(ParticleTypes.ENCHANT, x, y, z, 50, 1.0, 1.0, 1.0, 0.3);
        }
    }

    /**
     * Plays failure feedback (dissipation effect).
     */
    private void playFailureFeedback(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        double x = player.getX();
        double y = player.getY() + 1.0;
        double z = player.getZ();

        // Fire extinguish sound + smoke particles
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH,
                SoundSource.BLOCKS, 0.8f, 0.8f);
        serverLevel.sendParticles(ParticleTypes.SMOKE, x, y, z, 15, 0.4, 0.3, 0.4, 0.02);
        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 5, 0.3, 0.2, 0.3, 0.01);
    }

    /**
     * Plays downgrade feedback (failure + level loss).
     */
    private void playDowngradeFeedback(Player player) {
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        double x = player.getX();
        double y = player.getY() + 1.0;
        double z = player.getZ();

        // Anvil break sound + red particles (more dramatic than simple failure)
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.ANVIL_DESTROY,
                SoundSource.BLOCKS, 1.0f, 0.6f);
        serverLevel.playSound(null, player.blockPosition(), SoundEvents.GLASS_BREAK,
                SoundSource.BLOCKS, 0.5f, 0.8f);
        serverLevel.sendParticles(ParticleTypes.SMOKE, x, y, z, 25, 0.5, 0.4, 0.5, 0.03);
        serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 10, 0.4, 0.3, 0.4, 0.02);
        serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER, x, y, z, 5, 0.3, 0.3, 0.3, 0.0);
    }
}
