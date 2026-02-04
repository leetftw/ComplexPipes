package com.leetftw.complexpipes.common.gui;

import com.leetftw.complexpipes.common.items.ItemComponentRegistry;
import com.leetftw.complexpipes.common.items.PipeUpgradeItem;
import com.leetftw.complexpipes.common.pipe.network.PipeConnection;
import com.leetftw.complexpipes.common.pipe.types.PipeTypeRegistry;
import com.leetftw.complexpipes.common.pipe.upgrades.PipeUpgrade;
import com.leetftw.complexpipes.common.pipe.types.PipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

public class PipeConnectionMenu extends AbstractContainerMenu {
    private final PipeConnection pipeConnection;
    private ResourceKey<Level> dimension;

    private final Container upgradeContainer = new Container() {
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
            return pipeConnection.getUpgradeStream().noneMatch(Objects::nonNull);
        }

        @Override
        public @NonNull ItemStack getItem(int slot) {
            PipeUpgrade upgrade = pipeConnection.getUpgradeInSlot(slot);
            if (upgrade == null) return ItemStack.EMPTY;

            ItemStack stack = new ItemStack(upgrade.getType().getItem(), 1);
            stack.set(ItemComponentRegistry.PIPE_UPGRADE, upgrade);

            return stack;
        }

        @Override
        public @NonNull ItemStack removeItem(int slot, int amount) {
            if (amount < 1)
                return ItemStack.EMPTY;

            if (amount > 1)
                return ItemStack.EMPTY;

            PipeUpgrade upgrade = pipeConnection.getUpgradeInSlot(slot);
            if (upgrade == null) return ItemStack.EMPTY;

            ItemStack stack = new ItemStack(upgrade.getType().getItem(), 1);
            stack.set(ItemComponentRegistry.PIPE_UPGRADE, upgrade);

            pipeConnection.removeUpgradeInSlot(slot);
            return stack;
        }

        @Override
        public @NonNull ItemStack removeItemNoUpdate(int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (stack.getCount() < 1)
                return;

            if (stack.getCount() > 1)
                return;

            if (!(stack.getItem() instanceof PipeUpgradeItem))
                return;

            pipeConnection.setUpgradeInSlot(slot, stack.get(ItemComponentRegistry.PIPE_UPGRADE));
        }

        @Override
        public void setChanged() {
            // ???
        }

        @Override
        public boolean stillValid(@NonNull Player player) {
            return true;
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < PipeConnection.MAX_UPGRADES; i++)
                pipeConnection.removeUpgradeInSlot(i);
        }
    };

    // Client constructor
    public PipeConnectionMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, new PipeConnection(PipeTypeRegistry.getType("item"), BlockPos.ZERO, Direction.UP), null, Level.OVERWORLD);
    }

    // Server constructor
    public PipeConnectionMenu(int containerId, Inventory playerInventory, PipeConnection connection, PipeType<?> type, ResourceKey<Level> dim) {
        super(MenuRegistry.PIPE_CONNECTION_MENU.get(), containerId);

        pipeConnection = connection;
        dimension = dim;

        for (int i = 0; i < PipeConnection.MAX_UPGRADES; i++) {
            addSlot(new Slot(upgradeContainer, i, 8 + (18 * i), 18) {
                @Override
                public boolean isActive() {
                    return true;
                    //return connection.getMode() == PipeConnectionMode.EXTRACT || connection.getMode() == PipeConnectionMode.INSERT;
                }
            });
        }
        addStandardInventorySlots(playerInventory, 8, 18 + 18 + 18 + 13);

        // 0: PipeType ID
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return PipeTypeRegistry.getNumericId(pipeConnection.getType().getRegisteredId());
            }

            @Override
            public void set(int value) {
                pipeConnection.overwriteType(PipeTypeRegistry.getType(PipeTypeRegistry.getStringId(value)));
            }
        });
        // 1: Priority
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return pipeConnection.getPriority();
            }

            @Override
            public void set(int value) {
                pipeConnection.setPriority(value);
            }
        });
        // 2: Ratio
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return pipeConnection.getRatio();
            }

            @Override
            public void set(int value) {
                pipeConnection.setRatio(value);
                suppressRemoteUpdates();
                broadcastChanges();
                resumeRemoteUpdates();
            }
        });
        // 3: Dimension
        /*addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return VanillaRegistries.createLookup().get(Registries.DIMENSION).get().value().getId(dimension);
            }

            @Override
            public void set(int value) {
                Registry<Level> levelRegistry = VanillaRegistries.createLookup().get(Registries.DIMENSION).get().value();
                Level dimLevel = levelRegistry.byIdOrThrow(value);
                dimension = dimLevel.dimension();
            }
        });*/
        // 4: BlockPos.X
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return pipeConnection.getPipePos().getX();
            }

            @Override
            public void set(int value) {
                BlockPos oldPos = pipeConnection.getPipePos();
                pipeConnection.overwritePipePos(new BlockPos(value, oldPos.getY(), oldPos.getZ()));
            }
        });
        // 5: BlockPos.Y
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return pipeConnection.getPipePos().getY();
            }

            @Override
            public void set(int value) {
                BlockPos oldPos = pipeConnection.getPipePos();
                pipeConnection.overwritePipePos(new BlockPos(oldPos.getX(), value, oldPos.getZ()));
            }
        });
        // 6: BlockPos.Z
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return pipeConnection.getPipePos().getZ();
            }

            @Override
            public void set(int value) {
                BlockPos oldPos = pipeConnection.getPipePos();
                pipeConnection.overwritePipePos(new BlockPos(oldPos.getX(), oldPos.getY(), value));
            }
        });
        // 7: Side
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return pipeConnection.getSide().ordinal();
            }

            @Override
            public void set(int value) {
                pipeConnection.overwriteSide(Direction.values()[value]);
            }
        });
    }

    public PipeConnection getPipeConnection() {
        return pipeConnection;
    }

    public ResourceKey<Level> getDimension() {
        return dimension;
    }

    @Override
    public @NonNull ItemStack quickMoveStack(@NonNull Player player, int index) {
        // TODO: Implement this hell
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@NonNull Player player) {
        return true;
    }
}
