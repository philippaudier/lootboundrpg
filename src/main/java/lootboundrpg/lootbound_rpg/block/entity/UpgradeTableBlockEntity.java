package lootboundrpg.lootbound_rpg.block.entity;

import lootboundrpg.lootbound_rpg.registry.ModBlockEntities;
import lootboundrpg.lootbound_rpg.screen.UpgradeTableScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

/**
 * Block entity for the Upgrade Table.
 *
 * Stores:
 * - Slot 0: Equipment to upgrade
 * - Slot 1: Upgrade stone
 * - Slot 2: Result (upgraded equipment on success)
 */
public class UpgradeTableBlockEntity extends BlockEntity implements Container, MenuProvider {

    public static final int SLOT_EQUIPMENT = 0;
    public static final int SLOT_STONE = 1;
    public static final int SLOT_RESULT = 2;
    public static final int INVENTORY_SIZE = 3;

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);

    public UpgradeTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.UPGRADE_TABLE, pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.lootbound_rpg.upgrade_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new UpgradeTableScreenHandler(syncId, playerInventory, this);
    }

    // Container implementation

    @Override
    public int getContainerSize() {
        return INVENTORY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : inventory) {
            if (!stack.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot < 0 || slot >= inventory.size()) return ItemStack.EMPTY;
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(inventory, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(inventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= inventory.size()) return;
        inventory.set(slot, stack);
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        return player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public void clearContent() {
        inventory.clear();
        setChanged();
    }

    // Save/Load using MC 26.2 ValueInput/ValueOutput API

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inventory);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, inventory);
    }

    /**
     * Drops all contents when the block is broken.
     */
    public void dropContents(Level level, BlockPos pos) {
        Containers.dropContents(level, pos, inventory);
    }
}
