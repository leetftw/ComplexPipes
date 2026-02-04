package com.leetftw.complexpipes.common.blocks;

import com.leetftw.complexpipes.common.pipe.network.ClientPipeConnection;
import com.leetftw.complexpipes.common.pipe.network.PipeConnection;
import com.leetftw.complexpipes.common.pipe.network.PipeConnectionMode;
import com.leetftw.complexpipes.common.pipe.network.PipeNetworkView;
import com.leetftw.complexpipes.common.pipe.types.PipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.*;

public class PipeBlockEntity extends BlockEntity {
    // Note: a connection is any pipe which connects to a non-pipe block
    EnumMap<Direction, PipeConnection> ownedConnections;
    @Nullable PipeNetworkView networkView = null;
    List<ClientPipeConnection> clientPipeConnections = new ArrayList<>();

    public final PipeType<?> TYPE;

    public PipeBlockEntity(BlockPos pos, BlockState blockState, PipeType<?> pipeType) {
        super(Objects.requireNonNull(pipeType.getBlockEntityType()), pos, blockState);
        ownedConnections = new EnumMap<>(Direction.class);
        //CAPABILITY = capability;
        TYPE = pipeType;
    }

    public void setNetworkView(@Nullable PipeNetworkView networkView) {
        this.networkView = networkView;
    }

    // Called on block tick
    int tickCounter = 0;
    public <T> void tick(ServerLevel level, BlockPos pos, BlockState state, PipeType<T> type) {
        if (ownedConnections.isEmpty())
            return;

        // Make sure pipe network exists before continuing
        if (networkView == null || !networkView.isValid()) {
            networkView = PipeNetworkView.scanBlocks(level, pos);
        }

        for (Map.Entry<Direction, PipeConnection> connection : ownedConnections.entrySet()) {
            connection.getValue().tick(level, pos, networkView, type);
            if (connection.getValue().isDirty()) {
                setChanged();
                //PipeMod.LOGGER.info("[PipeBlockEntity] Marked for saving");
                connection.getValue().clearDirty();
            }
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);

        if (!(level instanceof ServerLevel)) return;
        Optional<PipeBlockEntity> pipeBE = level.getBlockEntity(pos, TYPE.getBlockEntityType());
        if (pipeBE.isEmpty()) return;

        List<ItemStack> droppedItems = new ArrayList<>();
        for (PipeConnection connection : pipeBE.get().getConnections()) {
            connection.appendItems(droppedItems);
        }

        for (ItemStack droppedItem : droppedItems) {
            ItemEntity itemEntity = new ItemEntity(level, pos.getX(), pos.getY(), pos.getZ(), droppedItem);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }

    public void refreshConnections() {
        BlockPos pos = getBlockPos();
        Level level = getLevel();
        if (!(level instanceof ServerLevel serverLevel)) return;

        for (Direction dir : Direction.values()) {
            BlockPos neighbour = pos.relative(dir);

            if (serverLevel.getCapability(TYPE.getBlockCapability(), neighbour, dir.getOpposite()) == null) {
                if (ownedConnections.containsKey(dir)) {
                    List<ItemStack> droppedItems = new ArrayList<>();
                    ownedConnections.get(dir).appendItems(droppedItems);
                    for (ItemStack droppedItem : droppedItems) {
                        ItemEntity itemEntity = new ItemEntity(serverLevel, pos.getX(), pos.getY(), pos.getZ(), droppedItem);
                        itemEntity.setDefaultPickUpDelay();
                        serverLevel.addFreshEntity(itemEntity);
                    }
                    ownedConnections.remove(dir);
                    setChanged();
                }
            } else {
                if (!ownedConnections.containsKey(dir)) {
                    ownedConnections.put(dir, new PipeConnection(TYPE, pos, dir));
                    setChanged();
                }
            }
        }

        for (PipeConnection ownedConnection : ownedConnections.values())
            ownedConnection.overwritePipePos(pos);
    }

    // Return our packet here. This method returning a non-null result tells the game to use this packet for syncing.
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // The packet uses the CompoundTag returned by #getUpdateTag. An alternative overload of #create exists
        // that allows you to specify a custom update tag, including the ability to omit data the client might not need.
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ValueInput valueInput) {
        if (valueInput.keySet().contains("clientConnections")) {
            handleUpdateTag(valueInput);
        }
        else {
            super.onDataPacket(net, valueInput);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (PipeConnection connection : ownedConnections.values()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putString("side", connection.getSide().name());
            entryTag.putString("mode", connection.getMode().name());
            list.add(entryTag);
        }
        tag.put("clientConnections", list);
        return tag;
    }

    // Handle a received update tag here. The default implementation calls #loadWithComponents here,
    // so you do not need to override this method if you don't plan to do anything beyond that.
    @Override
    public void handleUpdateTag(ValueInput input) {
        //super.handleUpdateTag(input);
        Optional<ValueInput.ValueInputList> listOptional = input.childrenList("clientConnections");

        //Optional<CompoundTag> listTagOpt = input.read("list", CompoundTag.CODEC);
        if (listOptional.isEmpty()) return;

        clientPipeConnections.clear();
        ValueInput.ValueInputList list = listOptional.get();
        list.forEach(entryInput ->
            clientPipeConnections.add(new ClientPipeConnection(
                    Direction.valueOf(entryInput.getStringOr("side", "EAST")),
                    PipeConnectionMode.valueOf(entryInput.getStringOr("mode", "PASSIVE"))
            ))
        );
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (Direction direction : Direction.values()) {
            String name = direction.getName();
            Optional<PipeConnection> connection = input.read(name, PipeConnection.CODEC);
            if (connection.isEmpty()) continue;
            ownedConnections.put(direction, connection.get());
            if (networkView != null) networkView.invalidate();
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (Map.Entry<Direction, PipeConnection> connectionEntry : ownedConnections.entrySet()) {
            output.store(connectionEntry.getKey().getName(), PipeConnection.CODEC, connectionEntry.getValue());
        }
        //PipeMod.LOGGER.info("[PipeBlockEntity] Saved to disk");
    }

    public List<ClientPipeConnection> getClientPipeConnections() {
        return clientPipeConnections;
    }

    public void setClientPipeConnections(List<ClientPipeConnection> clientPipeConnections) {
        this.clientPipeConnections = clientPipeConnections;
    }

    public Optional<PipeConnection> getConnectionForSide(Direction side) {
        return Optional.ofNullable(ownedConnections.get(side));
    }

    public Collection<PipeConnection> getConnections() {
        return ownedConnections.values();
    }

    @Override
    public void setChanged() {
        super.setChanged();

        if (level instanceof ServerLevel serverLevel) {
            Packet<?> packet = getUpdatePacket();
            for (ServerPlayer player : serverLevel.getChunkSource().chunkMap.getPlayers(new ChunkPos(getBlockPos()), false)) {
                player.connection.send(packet);
            }


            /*PacketDistributor.sendToPlayersTrackingChunk(serverLevel,
                    new ChunkPos(getBlockPos()),
                    createClientPayload()
            );*/
        }
    }
}
