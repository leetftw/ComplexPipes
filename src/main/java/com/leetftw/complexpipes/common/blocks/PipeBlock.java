package com.leetftw.complexpipes.common.blocks;

import com.leetftw.complexpipes.common.ComplexPipes;
import com.leetftw.complexpipes.common.gui.PipeConnectionMenu;
import com.leetftw.complexpipes.common.items.ItemComponentRegistry;
import com.leetftw.complexpipes.common.items.ItemRegistry;
import com.leetftw.complexpipes.common.items.PipeCardItem;
import com.leetftw.complexpipes.common.items.PipeUpgradeItem;
import com.leetftw.complexpipes.common.pipe.network.PipeConnection;
import com.leetftw.complexpipes.common.pipe.network.PipeConnectionMode;
import com.leetftw.complexpipes.common.pipe.network.PipeNetworkView;
import com.leetftw.complexpipes.common.pipe.types.PipeType;
import com.leetftw.complexpipes.common.util.routing.RoundRobinRoutingStrategy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;


public class PipeBlock extends Block implements EntityBlock
{
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

    //public final BlockCapability<ResourceHandler<T>, Direction> CAPABILITY;
    public final PipeType<?> TYPE;

    public PipeBlock(Properties properties, PipeType<?> pipeType)
    {
        super(properties);
        //CAPABILITY = capability;
        TYPE = pipeType;

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
    protected RenderShape getRenderShape(BlockState state) {
        // TODO: Use a BlockEntityRenderer because the multipart model is straining chunk mesh updates
        return RenderShape.MODEL;
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

    private BlockState getStateForPos(BlockState state, Level level, BlockPos pos)
    {
        BlockState initialState = state;

        boolean connection = false;
        for (Direction direction : Direction.values())
        {
            // Get the neighbour block
            BlockPos neighbourPos = pos.relative(direction);
            BlockState neighbor = level.getBlockState(neighbourPos);
            boolean isNeighbour = neighbor.getBlock().getClass() == this.getClass();
            boolean sideConnected =  level.getCapability(TYPE.getBlockCapability(), neighbourPos, direction.getOpposite()) != null;
            isNeighbour |= sideConnected;
            connection |= sideConnected;

            // Update the blockstate of this block
            state = state.setValue(CONNECTION_MAP.get(direction), isNeighbour);
        }

        return state;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, @Nullable Orientation orientation, boolean movedByPiston) {
        ComplexPipes.LOGGER.info("[PipeBlock] Block update " + pos.toShortString());

        // Determine new state first
        BlockState newState = getStateForPos(state, level, pos);

        // Then determine if a pipe or a connection was updated
        // If so, we need to rescan the network
        if (newState != state) {
            ComplexPipes.LOGGER.info("[PipeBlock] State of block changed!");
            // Set the new state (causes a block update)
            level.setBlockAndUpdate(pos, newState);

            // Rescan network
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PipeBlockEntity pipeBE
                && level instanceof ServerLevel serverLevel) {
                pipeBE.setNetworkView(PipeNetworkView.scanBlocks(serverLevel, pos));
            }
        }
    }
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context)
    {
        return getStateForPos(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    private void performHitAction(BlockPos pos, BlockHitResult hitResult, Consumer<Direction> axisHandler, Runnable centerHandler) {
        Vec3 hitLocation = hitResult.getLocation();
        Vec3 offsetFromCenter = hitLocation.subtract(pos.getCenter());

        // Determine major axis of offset
        double x = Math.abs(offsetFromCenter.x);
        double y = Math.abs(offsetFromCenter.y);
        double z = Math.abs(offsetFromCenter.z);

        double centerRange = 3f/16f;
        if (x > centerRange || y > centerRange || z > centerRange) {
            Direction axis;
            if (x >= y && x >= z) {
                axis = offsetFromCenter.x > 0 ? Direction.EAST : Direction.WEST;
            } else if (y >= x && y >= z) {
                axis = offsetFromCenter.y > 0 ? Direction.UP : Direction.DOWN;
            } else {
                axis = offsetFromCenter.z > 0 ? Direction.SOUTH : Direction.NORTH;
            }

            axisHandler.accept(axis);
        }
        else {
            centerHandler.run();
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        InteractionResult result = super.useWithoutItem(state, level, pos, player, hitResult);

        performHitAction(pos, hitResult, (axis) -> {
            if (!level.isClientSide()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof PipeBlockEntity pipeBE) {
                    Optional<PipeConnection> connection = pipeBE.getConnectionForSide(axis);
                    if (connection.isEmpty()) return;

                    player.openMenu(new SimpleMenuProvider(
                            (containerId, playerInventory, player1) -> new PipeConnectionMenu(containerId, playerInventory, connection.get(), TYPE),
                            Component.literal("Pipe Upgrades")
                    ));
                }
            }
        }, () -> {
            if (!level.isClientSide()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof PipeBlockEntity pipeBE) {
                    for (PipeConnection connection : pipeBE.networkView.connections) {
                        player.displayClientMessage(Component.literal(connection.getPipePos().toShortString() + " | " + connection.getSide().toString() + " | " + connection.getMode().name() + " | " + connection.calculateResourcesPerTick() + " per tick"), false);
                    }
                }
            }
        });

        return result;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        AtomicReference<InteractionResult> result = new AtomicReference<>(super.useItemOn(stack, state, level, pos, player, hand, hitResult));

        if (!(stack.getItem() instanceof PipeCardItem)) return result.get();
        performHitAction(pos, hitResult, (axis) -> {
            if (level.isClientSide()) return;

            // Get connection
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof PipeBlockEntity pipeBE)) return;
            Optional<PipeConnection> connectionOptional = pipeBE.getConnectionForSide(axis);
            if (connectionOptional.isEmpty()) return;

            // For upgrades: try adding upgrade
            // For cards: try setting modes
            boolean added = false;
            if (stack.getItem() instanceof PipeUpgradeItem) added = connectionOptional.get().tryAddUpgrade(stack.get(ItemComponentRegistry.PIPE_UPGRADE));
            else if (stack.is(ItemRegistry.ROUND_ROBIN_CARD)) {
                connectionOptional.get().setRoutingStrategy(new RoundRobinRoutingStrategy());
                added = true;
            }
            else if (stack.is(ItemRegistry.EXTRACTION_CARD)) {
                PipeConnectionMode oldMode = connectionOptional.get().getMode();
                if (oldMode != PipeConnectionMode.EXTRACT) {
                    added = oldMode != PipeConnectionMode.INSERT || player.getInventory().add(new ItemStack(ItemRegistry.INSERTION_CARD.get(), 1));
                    if (added) connectionOptional.get().setMode(PipeConnectionMode.EXTRACT);
                }
            }
            else if (stack.is(ItemRegistry.INSERTION_CARD)) {
                PipeConnectionMode oldMode = connectionOptional.get().getMode();
                if (oldMode != PipeConnectionMode.INSERT) {
                    added = oldMode != PipeConnectionMode.EXTRACT || player.getInventory().add(new ItemStack(ItemRegistry.EXTRACTION_CARD.get(), 1));
                    if (added) connectionOptional.get().setMode(PipeConnectionMode.INSERT);
                }
            }

            if (!added) return;

            // Inform player and consume item
            stack.consume(1, player);
            result.set(InteractionResult.CONSUME);
            player.displayClientMessage(Component.literal("Installed ").append(stack.getDisplayName()), true);

        }, () -> {});

        return result.get();
    }

    @Override
    public @org.jspecify.annotations.Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PipeBlockEntity(pos, state, TYPE);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide())
            return null;

        if (type != TYPE.getBlockEntityType())
            return null;

        return (level1, pos, state1, blockEntity) -> {
            if (level1.isClientSide())
                return;

            if (blockEntity instanceof PipeBlockEntity pipeBE
                && level1 instanceof ServerLevel serverLevel)
                pipeBE.tick(serverLevel, pos, state1, TYPE);
        };
    }
}