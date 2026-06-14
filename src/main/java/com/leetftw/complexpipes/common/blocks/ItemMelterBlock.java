package com.leetftw.complexpipes.common.blocks;

import com.leetftw.complexpipes.common.block_entities.BlockEntityRegistry;
import com.leetftw.complexpipes.common.block_entities.ItemMelterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public class ItemMelterBlock extends Block implements EntityBlock {

    public ItemMelterBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemMelterBlockEntity(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide())
            return null;

        if (type != BlockEntityRegistry.ITEM_MELTER_BLOCK_ENTITY.get())
            return null;

        return (level1, pos, state1, blockEntity) -> {
            if (level1.isClientSide())
                return;

            if (blockEntity instanceof ItemMelterBlockEntity melterBlockEntity
                    && level1 instanceof ServerLevel serverLevel)
                melterBlockEntity.tick(serverLevel, pos, state1);
        };
    }
}
