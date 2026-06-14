package com.leetftw.complexpipes.common.blocks;

import com.leetftw.complexpipes.client.ClientConfig;
import com.leetftw.complexpipes.common.ComplexPipes;
import com.leetftw.complexpipes.common.block_entities.PipeBlockEntity;
import com.leetftw.complexpipes.common.gui.PipeConnectionMenu;
import com.leetftw.complexpipes.common.items.ItemComponentRegistry;
import com.leetftw.complexpipes.common.items.ItemRegistry;
import com.leetftw.complexpipes.common.items.PipeCardItem;
import com.leetftw.complexpipes.common.pipe.network.PipeConnection;
import com.leetftw.complexpipes.common.pipe.network.PipeConnectionMode;
import com.leetftw.complexpipes.common.pipe.network.PipeNetworkView;
import com.leetftw.complexpipes.common.pipe.types.PipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.NonNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;


public class PipeBlock extends PipeFrameBlock implements EntityBlock
{
    public final PipeType<?> TYPE;

    public PipeBlock(Properties properties, PipeType<?> pipeType)
    {
        super(properties, true);
        TYPE = pipeType;
    }

    public PipeType<?> getType() {
        return TYPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return ClientConfig.RENDER_PIPE_BE.get() ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }

    @Override
    protected BlockState getStateForPos(BlockState state, Level level, BlockPos pos)
    {
        for (Direction direction : Direction.values())
        {
            // Get the neighbour block
            BlockPos neighbourPos = pos.relative(direction);
            BlockState neighbor = level.getBlockState(neighbourPos);
            boolean isNeighbour = neighbor.getBlock().equals(this);
            boolean sideConnected =  level.getCapability(TYPE.getBlockCapability(), neighbourPos, direction.getOpposite()) != null;
            isNeighbour |= sideConnected;

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

    private InteractionResult performHitAction(BlockPos pos, BlockHitResult hitResult, Function<Direction, InteractionResult> axisHandler, Supplier<InteractionResult> centerHandler) {
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

            return axisHandler.apply(axis);
        }
        else {
            return centerHandler.get();
        }
    }

    private InteractionResult useWithoutItemSide(BlockState state, Level level, BlockPos pos, Player player, Direction axis) {
        if (!level.isClientSide()) {
            // TODO: If player is crouching and the connection is IMPORT/EXPORT then remove the import/export card
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PipeBlockEntity pipeBE) {
                Optional<PipeConnection> connection = pipeBE.getConnectionForSide(axis);
                if (connection.isEmpty()) return InteractionResult.FAIL;

                player.openMenu(new SimpleMenuProvider(
                        (containerId, playerInventory, player1) -> new PipeConnectionMenu(containerId, playerInventory, ContainerLevelAccess.create(level, pos), connection.get(), level.dimension()),
                        state.getBlock().getName()
                ), (extraDataWriter) -> ByteBufCodecs.STRING_UTF8.encode(extraDataWriter, TYPE.getRegisteredId()));
            }
        }

        return InteractionResult.FAIL;
    }

    private InteractionResult useWithoutItemCenter(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof PipeBlockEntity pipeBE) {
                if (pipeBE.getNetworkView() == null)
                    return null;
                Component header = Component.literal(String.format(
                        "%-14s | %-4s | %-9s | %4s | %5s | %7s",
                        "Pos", "Side", "Mode", "Prio", "Ratio", "Rate/t"
                )).withStyle(ChatFormatting.GRAY);

                player.sendSystemMessage(header);

                for (PipeConnection c : pipeBE.getNetworkView().connections) {
                    Component line = Component.literal(String.format(
                            "%-14s | %-4s | %-9s | %4d | %5d | ~%6d",
                            c.getPipePos().toShortString(),
                            c.getSide().getSerializedName().toUpperCase(),
                            c.getMode().name(),
                            c.getPriority(),
                            c.getRatio(),
                            Double.valueOf(c.calculateResourcesPerTick()).intValue()
                    )).withStyle(ChatFormatting.WHITE);

                    player.sendSystemMessage(line);
                }
            }
        }

        return null;
    }

    @Override
    protected @NonNull InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        InteractionResult result = super.useWithoutItem(state, level, pos, player, hitResult);

        InteractionResult computed = performHitAction(pos, hitResult,
                (axis) -> useWithoutItemSide(state, level, pos, player, axis),
                () -> useWithoutItemCenter(state, level, pos, player)
        );

        return computed != null ? computed : result;
    }

    private InteractionResult useItemOnSide(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Direction axis) {
        if (level.isClientSide()) return null;

        // If it is a wrench, toggle the connection on that side
        if (stack.is(Tags.Items.TOOLS_WRENCH)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (!(blockEntity instanceof PipeBlockEntity pipeBE))
                return InteractionResult.FAIL;

            // Make sure pipe has connection
            pipeBE.refreshConnections();
            Optional<PipeConnection> connectionOptional = pipeBE.getConnectionForSide(axis);
            if (connectionOptional.isEmpty()) {
                // Add a disabled connection if there is no connection at all
                pipeBE.setDisabled(axis);
                if (pipeBE.getNetworkView() != null) pipeBE.getNetworkView().invalidate();
                return InteractionResult.SUCCESS;
            }

            // Toggle connection
            PipeConnection connection = connectionOptional.get();
            if (connection.getMode() == PipeConnectionMode.DISABLED) {
                connection.setMode(PipeConnectionMode.PASSIVE);
                pipeBE.setChanged();
                if (pipeBE.getNetworkView() != null) pipeBE.getNetworkView().invalidate();
                return InteractionResult.SUCCESS;
            } else if (connection.getMode() == PipeConnectionMode.PASSIVE) {
                connection.setMode(PipeConnectionMode.DISABLED);
                pipeBE.setChanged();
                if (pipeBE.getNetworkView() != null) pipeBE.getNetworkView().invalidate();
                return InteractionResult.SUCCESS;
            }

            // Pop off all cards
            List<ItemStack> items = new ArrayList<>();
            connection.appendItems(items);

            // First try to add all cards to the player's inventory, if that fails drop them on the ground
            for (ItemStack storedStack : items) {
                if (!player.getInventory().add(storedStack)) {
                    player.drop(storedStack, false);
                }
            }

            connection.setMode(PipeConnectionMode.PASSIVE);
            pipeBE.setChanged();
            if (pipeBE.getNetworkView() != null) pipeBE.getNetworkView().invalidate();
            return InteractionResult.SUCCESS;
        }

        // Otherwise it has to be a card
        if (!(stack.getItem() instanceof PipeCardItem)) return null;

        // Get connection
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof PipeBlockEntity pipeBE)) return null;
        pipeBE.refreshConnections();
        Optional<PipeConnection> connectionOptional = pipeBE.getConnectionForSide(axis);
        if (connectionOptional.isEmpty()) return null;

        // For upgrades: try adding upgrade
        // For cards: try setting modes
        boolean added = false;

        // TODO: Clean up this logic
        if (stack.is(ItemRegistry.EXTRACTION_CARD)) {
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
        else if (stack.getItem() instanceof PipeCardItem) added = connectionOptional.get().tryAddCard(stack.get(ItemComponentRegistry.PIPE_CARD_DATA));

        if (!added) return InteractionResult.FAIL;

        // Inform player and consume item
        player.sendOverlayMessage(Component.literal("Installed ").append(stack.getDisplayName()));
        stack.consume(1, player);
        return InteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        InteractionResult result = super.useItemOn(stack, state, level, pos, player, hand, hitResult);

        InteractionResult computed = performHitAction(pos, hitResult,
                (axis) -> useItemOnSide(stack, state, level, pos, player, hand, axis),
                () -> null
        );

        return computed != null ? computed : result;
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