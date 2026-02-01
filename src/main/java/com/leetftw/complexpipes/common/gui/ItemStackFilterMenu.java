package com.leetftw.complexpipes.common.gui;

import com.leetftw.complexpipes.common.items.ItemComponentRegistry;
import com.leetftw.complexpipes.common.items.PipeUpgradeItem;
import com.leetftw.complexpipes.common.pipe.network.PipeConnection;
import com.leetftw.complexpipes.common.pipe.network.PipeConnectionMode;
import com.leetftw.complexpipes.common.pipe.types.PipeType;
import com.leetftw.complexpipes.common.pipe.types.PipeTypeRegistry;
import com.leetftw.complexpipes.common.pipe.upgrades.BuiltinPipeUpgrades;
import com.leetftw.complexpipes.common.pipe.upgrades.PipeUpgrade;
import com.leetftw.complexpipes.common.pipe.upgrades.builtin.ItemStackPipeFilter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public class ItemStackFilterMenu extends AbstractContainerMenu {
    private ItemStack stack;
    private ItemStackPipeFilter filter = null;
    private Container filterContainer = new Container() {
        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getContainerSize() {
            return PipeConnection.MAX_UPGRADES;
        }

        @Override
        public boolean isEmpty() {
            return Arrays.stream(filter.items).noneMatch(Objects::nonNull);
        }

        @Override
        public ItemStack getItem(int slot) {
            ItemResource resource = filter.items[slot];
            if (resource == null) return ItemStack.EMPTY;

            return resource.toStack(1);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            if (amount <= 0) return ItemStack.EMPTY;

            filter.items[slot] = null;
            stack.set(ItemComponentRegistry.PIPE_UPGRADE, filter);

            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return null;
        }

        @Override
        public void setItem(int slot, ItemStack stack2) {
            filter.items[slot] = ItemResource.of(stack2);
            stack.set(ItemComponentRegistry.PIPE_UPGRADE, filter);
        }

        @Override
        public void setChanged() {
            // ???
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < ItemStackPipeFilter.SLOT_COUNT; i++)
                filter.items[i] = null;
            stack.set(ItemComponentRegistry.PIPE_UPGRADE, filter);
        }
    };

    private class FilterSlot extends Slot {
        public FilterSlot(Container container, int slot, int x, int y) {
            super(container, slot, x, y);
        }

        @Override
        public ItemStack safeInsert(ItemStack stack2, int increment) {
            if (this.mayPlace(stack2)) {
                container.setItem(getContainerSlot(), stack2);
                setChanged();
            }
            return stack2;
        }
    }

    // Client constructor
    public ItemStackFilterMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, null);
    }

    // Server constructor
    public ItemStackFilterMenu(int containerId, Inventory playerInventory, ItemStack stack) {
        super(MenuRegistry.ITEM_STACK_FILTER_MENU.get(), containerId);
        if (stack == null) {
            stack = new ItemStack(BuiltinPipeUpgrades.ITEM_STACK_FILTER.getItem(), 1);
            stack.set(ItemComponentRegistry.PIPE_UPGRADE, new ItemStackPipeFilter());
        }

        this.stack = stack;

        PipeUpgrade upgrade = stack.get(ItemComponentRegistry.PIPE_UPGRADE);
        if (upgrade instanceof ItemStackPipeFilter itemFilter) {
            this.filter = itemFilter.clone();
        }

        for (int i = 0; i < ItemStackPipeFilter.SLOT_COUNT; i++)
            addSlot(new FilterSlot(filterContainer, i, 8 + (18 * i), 18));

        addStandardInventorySlots(playerInventory, 8, 18 + 18 + 13);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
