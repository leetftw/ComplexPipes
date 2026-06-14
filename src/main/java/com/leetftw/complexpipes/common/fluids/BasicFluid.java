package com.leetftw.complexpipes.common.fluids;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.WaterFluid;
import net.neoforged.neoforge.fluids.FluidType;

import java.util.function.Supplier;

public abstract class BasicFluid extends WaterFluid {
    BasicFluidRegistryEntry entry;

    public BasicFluid(BasicFluidRegistryEntry entry) {
        this.entry = entry;
    }

    @Override
    public FluidType getFluidType() {
        return entry.getFluidType();
    }

    @Override
    public BlockState createLegacyBlock(FluidState state) {
        return entry.getLegacyBlock().defaultBlockState().setValue(LiquidBlock.LEVEL, getLegacyLevel(state));
    }

    @Override
    protected boolean canConvertToSource(ServerLevel level) {
        return false;
    }

    @Override
    public Fluid getFlowing() {
        return entry.getFlowing();
    }

    @Override
    public Fluid getSource() {
        return entry.getSource();
    }

    @Override
    public Item getBucket() {
        return entry.getBucket();
    }

    @Override
    public boolean isSame(Fluid fluid) {
        return fluid == getSource() || fluid == getFlowing();
    }

    static class Flowing extends BasicFluid {
        public Flowing(BasicFluidRegistryEntry entry) {
            super(entry);
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    static class Source extends BasicFluid {
        public Source(BasicFluidRegistryEntry entry) {
            super(entry);
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
