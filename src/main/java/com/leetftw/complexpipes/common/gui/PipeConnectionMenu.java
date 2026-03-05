package com.leetftw.complexpipes.common.gui;

import com.leetftw.complexpipes.common.cards.PipeCard;
import com.leetftw.complexpipes.common.items.ItemComponentRegistry;
import com.leetftw.complexpipes.common.items.ItemRegistry;
import com.leetftw.complexpipes.common.items.PipeCardItem;
import com.leetftw.complexpipes.common.pipe.network.PipeConnection;
import com.leetftw.complexpipes.common.pipe.types.PipeTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.function.Supplier;

public class PipeConnectionMenu extends AbstractContainerMenu {
    private final PipeConnection pipeConnection;
    private final ContainerLevelAccess access;
    private ResourceKey<Level> dimension;

    private final Container cardContainer = new Container() {
        @Override
        public int getMaxStackSize() {
            return 1;
        }

        @Override
        public int getContainerSize() {
            return pipeConnection.getMaxCards();
        }

        @Override
        public boolean isEmpty() {
            return pipeConnection.getCardStream().findAny().isPresent();
        }

        @Override
        public @NonNull ItemStack getItem(int slot) {
            PipeCard card = pipeConnection.getCardInSlot(slot);
            if (card == null) return ItemStack.EMPTY;

            ItemStack stack = new ItemStack(card.getType().getItem(), 1);
            stack.set(ItemComponentRegistry.PIPE_CARD_DATA, card);

            return stack;
        }

        @Override
        public @NonNull ItemStack removeItem(int slot, int amount) {
            if (amount < 1)
                return ItemStack.EMPTY;

            if (amount > 1)
                return ItemStack.EMPTY;

            PipeCard card = pipeConnection.getCardInSlot(slot);
            if (card == null) return ItemStack.EMPTY;

            ItemStack stack = new ItemStack(card.getType().getItem(), 1);
            stack.set(ItemComponentRegistry.PIPE_CARD_DATA, card);

            pipeConnection.removeCardInSlot(slot);
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

            if (!(stack.getItem() instanceof PipeCardItem))
                return;

            if (stack.is(ItemRegistry.EXTRACTION_CARD) || stack.is(ItemRegistry.INSERTION_CARD))
                return;

            pipeConnection.setCardInSlot(slot, stack.get(ItemComponentRegistry.PIPE_CARD_DATA));
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
            for (int i = 0; i < pipeConnection.getMaxCards(); i++)
                pipeConnection.removeCardInSlot(i);
        }
    };

    // Client constructor
    public PipeConnectionMenu(int containerId, Inventory playerInventory, FriendlyByteBuf data) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL,
                ((Supplier<PipeConnection>)(() -> {
                        PipeConnection connection = new PipeConnection(PipeTypeRegistry.getType(ByteBufCodecs.STRING_UTF8.decode(data)), BlockPos.ZERO, Direction.UP);
                        connection.setPriority(Integer.MAX_VALUE);
                        connection.setRatio(Integer.MAX_VALUE);
                        return connection;
                })).get(),
                Level.OVERWORLD);
    }

    // Server constructor
    public PipeConnectionMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access, PipeConnection connection, ResourceKey<Level> dim) {
        super(MenuRegistry.PIPE_CONNECTION_MENU.get(), containerId);

        pipeConnection = connection;
        dimension = dim;
        this.access = access;

        // TODO: This will not be valid on the client
        int containerRows = (connection.getMaxCards() + 8) / 9;

        for (int i = 0; i < pipeConnection.getMaxCards(); i++) {
            addSlot(new Slot(cardContainer, i, 8 + (18 * (i % 9)), 18 + (i / 9 * 18) ) {
                @Override
                public boolean mayPlace(@NonNull ItemStack stack) {
                    if (!(stack.getItem() instanceof PipeCardItem))
                        return false;

                    PipeCard pipeCard = stack.get(ItemComponentRegistry.PIPE_CARD_DATA);
                    if (pipeCard == null)
                        return false;

                    return pipeConnection.mayAddCard(pipeCard);
                }
            });
        }
        addStandardInventorySlots(playerInventory, 8, (18 * containerRows) + 58);

        // 0: Priority
        addDataSlot(new DataSlot() {
            @Override
            public int get() {
                return pipeConnection.getPriority();
            }

            @Override
            public void set(int value) {
                pipeConnection.setPriority(value);
                suppressRemoteUpdates();
                broadcastChanges();
                resumeRemoteUpdates();
            }
        });
        // 1: Ratio
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
        // 2: BlockPos.X
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
        // 3: BlockPos.Y
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
        // 4: BlockPos.Z
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
        // 5: Side
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
        return AbstractContainerMenu.stillValid(access, player, pipeConnection.getType().getBlock());
    }
}
