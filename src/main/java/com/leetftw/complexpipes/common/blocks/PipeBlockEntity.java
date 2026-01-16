package com.leetftw.complexpipes.common.blocks;

import com.leetftw.complexpipes.common.PipeMod;
import com.leetftw.complexpipes.common.pipe.network.PipeConnection;
import com.leetftw.complexpipes.common.pipe.network.PipeConnectionMode;
import com.leetftw.complexpipes.common.pipe.network.PipeNetworkView;
import com.leetftw.complexpipes.common.pipe.types.PipeType;
import com.leetftw.complexpipes.common.pipe.upgrade.PipeUpgrade;
import com.leetftw.complexpipes.common.util.routing.DefaultRoutingStrategy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Predicate;

public class PipeBlockEntity extends BlockEntity {
    // Note: a connection is any pipe which connects to a non-pipe block
    EnumMap<Direction, PipeConnection> ownedConnections;
    @Nullable PipeNetworkView networkView = null;

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
        if (!state.getValue(PipeBlock.HAS_ENTITY))
            return;

        // Make sure pipe network exists before continuing
        if (networkView == null) {
            networkView = PipeNetworkView.scanBlocks(level, pos);
        }

        for (Map.Entry<Direction, PipeConnection> connection : ownedConnections.entrySet()) {
            connection.getValue().tick(level, pos, networkView, type);
            if (connection.getValue().isDirty()) {
                setChanged();
                PipeMod.LOGGER.info("[PipeBlockEntity] Marked for saving");
                connection.getValue().clearDirty();
            }
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
                    // TODO: pop upgrades out
                    ownedConnections.remove(dir);
                }
            } else {
                if (!ownedConnections.containsKey(dir)) {
                    ownedConnections.put(dir, new PipeConnection(pos, dir));
                }
            }
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        for (Direction direction : Direction.values()) {
            String name = direction.getName();
            Optional<PipeConnection> connection = input.read(name, PipeConnection.CODEC);
            if (connection.isEmpty()) continue;
            ownedConnections.put(direction, connection.get());
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        for (Map.Entry<Direction, PipeConnection> connectionEntry : ownedConnections.entrySet()) {
            output.store(connectionEntry.getKey().getName(), PipeConnection.CODEC, connectionEntry.getValue());
        }
        PipeMod.LOGGER.info("[PipeBlockEntity] Saved to disk");
    }

    public Optional<PipeConnection> getConnectionForSide(Direction side) {
        return Optional.ofNullable(ownedConnections.get(side));
    }

    public Collection<PipeConnection> getConnections() {
        return ownedConnections.values();
    }

    public boolean trySetConnectionMode(Direction direction, PipeConnectionMode mode) {
        if (!ownedConnections.containsKey(direction))
            return false;

        ownedConnections.get(direction).setMode(mode);
        return true;
    }
}
