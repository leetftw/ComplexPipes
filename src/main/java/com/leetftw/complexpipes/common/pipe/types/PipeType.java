package com.leetftw.complexpipes.common.pipe.types;

import com.leetftw.complexpipes.common.blocks.PipeBlock;
import com.leetftw.complexpipes.common.blocks.PipeBlockEntity;
import com.leetftw.complexpipes.common.pipe.upgrades.PipeUpgradeType;
import com.leetftw.complexpipes.common.util.PipeHandlerWrapper;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.capabilities.BlockCapability;
import org.jetbrains.annotations.NotNull;

public abstract class PipeType<T> {
    private String id;
    private PipeBlock block;
    private BlockEntityType<PipeBlockEntity> beType;

    public final String getRegisteredId() {
        return id;
    }

    public final PipeBlock getBlock() {
        return block;
    }

    public final BlockEntityType<PipeBlockEntity> getBlockEntityType() {
        return beType;
    }

    public abstract BlockCapability<T, Direction> getBlockCapability();
    public abstract PipeHandlerWrapper<T> getHandlerWrapper();
    public abstract int getDefaultTransferAmount();
    public abstract int getDefaultTransferSpeed();
    public abstract Identifier getTexturePath();
    public abstract boolean supportsUpgrade(PipeUpgradeType upgradeType);

    final void setRegisteredId(@NotNull String id) {
        this.id = id;
    }

    final void setBlock(@NotNull PipeBlock block) {
        this.block = block;
    }

    final void setBeType(@NotNull BlockEntityType<PipeBlockEntity> beType) {
        this.beType = beType;
    }
}
