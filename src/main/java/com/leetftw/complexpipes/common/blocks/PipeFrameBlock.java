package com.leetftw.complexpipes.common.blocks;

import com.leetftw.complexpipes.common.ComplexPipes;
import com.leetftw.complexpipes.common.block_entities.PipeBlockEntity;
import com.leetftw.complexpipes.common.pipe.network.PipeNetworkView;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.Map;

public class PipeFrameBlock extends Block {
    public static final BooleanProperty NORTH_CON = BooleanProperty.create("north");
    public static final BooleanProperty EAST_CON = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH_CON = BooleanProperty.create("south");
    public static final BooleanProperty WEST_CON = BooleanProperty.create("west");
    public static final BooleanProperty UP_CON = BooleanProperty.create("up");
    public static final BooleanProperty DOWN_CON = BooleanProperty.create("down");

    public static final Map<Direction, BooleanProperty> CONNECTION_MAP = Map.of(
            Direction.NORTH, NORTH_CON,
            Direction.EAST, EAST_CON,
            Direction.SOUTH, SOUTH_CON,
            Direction.WEST, WEST_CON,
            Direction.UP, UP_CON,
            Direction.DOWN, DOWN_CON
    );

    public final boolean isFilled;

    public PipeFrameBlock(Properties properties, boolean isFilled) {
        super(properties);

        this.isFilled = isFilled;

        registerDefaultState(defaultBlockState()
                .setValue(NORTH_CON, false)
                .setValue(EAST_CON, false)
                .setValue(SOUTH_CON, false)
                .setValue(WEST_CON, false)
                .setValue(UP_CON, false)
                .setValue(DOWN_CON, false));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        VoxelShape shape = Shapes.box(5 / 16.0, 5 / 16.0, 5 / 16.0,
                11 / 16.0, 11 / 16.0, 11 / 16.0);
        if (state.getValue(NORTH_CON))
            shape = Shapes.or(shape, Shapes.box(5 / 16.0, 5 / 16.0, 0f,
                    11 / 16.0, 11 / 16.0, 5 / 16.0));
        if (state.getValue(EAST_CON))
            shape = Shapes.or(shape, Shapes.box(11 / 16.0, 5 / 16.0, 5 / 16.0,
                    1.0, 11 / 16.0, 11 / 16.0));
        if (state.getValue(SOUTH_CON))
            shape = Shapes.or(shape, Shapes.box(5 / 16.0, 5 / 16.0, 11 / 16.0,
                    11 / 16.0, 11 / 16.0, 1.0));
        if (state.getValue(WEST_CON))
            shape = Shapes.or(shape, Shapes.box(0, 5 / 16.0, 5 / 16.0,
                    5 / 16.0, 11 / 16.0, 11 / 16.0));
        if (state.getValue(UP_CON))
            shape = Shapes.or(shape, Shapes.box(5 / 16.0, 11 / 16.0, 5 / 16.0,
                    11 / 16.0, 1.0, 11 / 16.0));
        if (state.getValue(DOWN_CON))
            shape = Shapes.or(shape, Shapes.box(5 / 16.0, 0, 5 / 16.0,
                    11 / 16.0, 5 / 16.0, 11 / 16.0));
        return shape;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        super.createBlockStateDefinition(builder);
        builder.add(NORTH_CON);
        builder.add(EAST_CON);
        builder.add(SOUTH_CON);
        builder.add(WEST_CON);
        builder.add(UP_CON);
        builder.add(DOWN_CON);
    }

    protected BlockState getStateForPos(BlockState state, Level level, BlockPos pos)
    {
        for (Direction direction : Direction.values())
        {
            // Get the neighbour block
            BlockPos neighbourPos = pos.relative(direction);
            BlockState neighbor = level.getBlockState(neighbourPos);
            boolean isNeighbour = neighbor.getBlock().equals(this);

            // Update the blockstate of this block
            state = state.setValue(CONNECTION_MAP.get(direction), isNeighbour);
        }

        return state;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        // Determine new state first
        BlockState newState = getStateForPos(state, level, pos);

        // Then determine if a pipe or a connection was updated
        // If so, we need to rescan the network
        if (newState != state) {
            // Set the new state (causes a block update)
            level.setBlockAndUpdate(pos, newState);
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return getStateForPos(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }
}
