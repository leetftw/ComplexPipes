package com.leetftw.complexpipes.common.pipe.network;

import com.leetftw.complexpipes.common.PipeMod;
import com.leetftw.complexpipes.common.blocks.PipeBlock;
import com.leetftw.complexpipes.common.blocks.PipeBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;

/// Stateless class
/// Only stores references to connections
public class PipeNetworkView {
    Set<BlockPos> pipes;
    public List<PipeConnection> connections;

    private PipeNetworkView() {
        pipes = new HashSet<>();
        connections = new ArrayList<>();
    }

    // TODO: make this iterative instead of recursive
    private static void createPipeNetwork(ServerLevel level, Set<BlockPos> enumeratedPipes, BlockPos currentPipe) {
        if (!enumeratedPipes.add(currentPipe))
            return;

        BlockState currentState = level.getBlockState(currentPipe);
        for (Direction d : Direction.values()) {
            if (!currentState.getValue(PipeBlock.CONNECTION_MAP.get(d)))
                continue;

            BlockPos neighbourPos = currentPipe.relative(d);
            BlockState neighbourState = level.getBlockState(neighbourPos);

            if (!(neighbourState.getBlock() instanceof PipeBlock))
                continue;

            createPipeNetwork(level, enumeratedPipes, neighbourPos);
        }
    }

    public static PipeNetworkView scanBlocks(ServerLevel level, BlockPos initialPos) {
        PipeNetworkView view = new PipeNetworkView();

        // Generate network
        createPipeNetwork(level, view.pipes, initialPos);

        // Refresh connections
        // Useful for when a pipe is placed to avoid race condition between BE initializing and enumeration commencing
        for (BlockPos pos : view.pipes) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) {
                PipeMod.LOGGER.warn("[PipeNetwork] Expected block entity at position " + pos.toShortString());
                continue;
            }

            if (blockEntity instanceof PipeBlockEntity pipeBE) {
                pipeBE.refreshConnections();
                view.connections.addAll(pipeBE.getConnections());
                pipeBE.setNetworkView(view);
            } else {
                // Wrong BE type?
                PipeMod.LOGGER.warn("[PipeNetwork] Wrong block entity class at position " + pos.toShortString());
            }
        }
        view.connections.sort(Comparator.comparing(PipeConnection::getPipePos));

        return view;
    }
}
