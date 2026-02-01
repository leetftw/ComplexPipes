package com.leetftw.complexpipes.common.pipe.network;

import com.leetftw.complexpipes.common.ComplexPipes;
import com.leetftw.complexpipes.common.blocks.PipeBlock;
import com.leetftw.complexpipes.common.blocks.PipeBlockEntity;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
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

    private static void createPipeNetwork(ServerLevel level, LongOpenHashSet enumeratedPipes, BlockPos start) {
        ArrayDeque<BlockPos> stack = new ArrayDeque<>();
        stack.push(start);

        while (!stack.isEmpty()) {
            BlockPos current = stack.pop();

            if (!enumeratedPipes.add(current.asLong()))
                continue;

            BlockState currentState = level.getBlockState(current);

            for (Direction d : Direction.values()) {
                if (!currentState.getValue(PipeBlock.CONNECTION_MAP.get(d)))
                    continue;

                BlockPos neighbourPos = current.relative(d);
                BlockState neighbourState = level.getBlockState(neighbourPos);

                if (neighbourState.getBlock() instanceof PipeBlock) {
                    stack.push(neighbourPos);
                }
            }
        }
    }

    public static PipeNetworkView scanBlocks(ServerLevel level, BlockPos initialPos) {
        PipeNetworkView view = new PipeNetworkView();
        LongOpenHashSet pipeHashSet = new LongOpenHashSet();

        // Generate network
        createPipeNetwork(level, pipeHashSet, initialPos);

        // Convert hash set
        pipeHashSet.stream().map(BlockPos::of).forEach(a -> view.pipes.add(a));

        // Refresh connections
        // Useful for when a pipe is placed to avoid race condition between BE initializing and enumeration commencing
        for (BlockPos pos : view.pipes) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity == null) {
                ComplexPipes.LOGGER.warn("[PipeNetwork] Expected block entity at position " + pos.toShortString());
                continue;
            }

            if (blockEntity instanceof PipeBlockEntity pipeBE) {
                pipeBE.refreshConnections();
                view.connections.addAll(pipeBE.getConnections());
                pipeBE.setNetworkView(view);
            } else {
                // Wrong BE type?
                ComplexPipes.LOGGER.warn("[PipeNetwork] Wrong block entity class at position " + pos.toShortString());
            }
        }
        view.connections.sort(Comparator.comparing(PipeConnection::getPipePos));

        return view;
    }
}
