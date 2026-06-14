package com.leetftw.complexpipes.common.fluids;

import com.leetftw.complexpipes.common.blocks.BlockRegistry;
import com.leetftw.complexpipes.common.items.ItemRegistry;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.function.BiFunction;

public class BasicFluidRegistryEntry {
    private DeferredHolder<FluidType, FluidType> type = null;
    private DeferredHolder<Fluid, BasicFluid> source = null;
    private DeferredHolder<Fluid, BasicFluid> flowing = null;
    private DeferredBlock<LiquidBlock> block = null;
    private DeferredItem<BucketItem> bucket = null;

    public BasicFluidRegistryEntry(String name, BiFunction<BasicFluidRegistryEntry, Boolean, BasicFluid> constructor) {
        type = FluidRegistry.FLUID_TYPES.register(name, identifier -> new FluidType(FluidType.Properties.create().descriptionId(identifier.toString())));

        source = FluidRegistry.FLUIDS.register(name, () -> constructor.apply(
                this,
                false
        ));

        flowing = FluidRegistry.FLUIDS.register("flowing_" + name, () -> constructor.apply(
                this,
                true
        ));

        block = BlockRegistry.BLOCKS.registerBlock(name, properties -> new LiquidBlock(source.get(), properties),
                () -> BlockBehaviour.Properties.of()
                        .mapColor(MapColor.WATER)
                        .replaceable()
                        .noCollision()
                        .strength(100.0F)
                        .pushReaction(PushReaction.DESTROY)
                        .noLootTable()
                        .liquid()
                        .sound(SoundType.EMPTY));

        bucket = ItemRegistry.ITEMS.registerItem(name + "_bucket", properties -> new BucketItem(source.get(), properties));
    }

    public FluidType getFluidType() {
        return type.get();
    }

    public BasicFluid getSource() {
        return source.get();
    }

    public BasicFluid getFlowing() {
        return flowing.get();
    }

    public LiquidBlock getLegacyBlock() {
        return block.get();
    }

    public BucketItem getBucket() {
        return bucket.get();
    }

}
