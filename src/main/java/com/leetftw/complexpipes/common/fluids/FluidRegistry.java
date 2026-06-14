package com.leetftw.complexpipes.common.fluids;

import com.leetftw.complexpipes.common.blocks.BlockRegistry;
import com.leetftw.complexpipes.common.items.ItemRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.*;

import java.util.function.Supplier;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class FluidRegistry {
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, MODID);
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, MODID);

    public static final BasicFluidRegistryEntry LIQUID_ENDER = new BasicFluidRegistryEntry("liquified_ender_pearl",
            (entry, isFlowing) -> isFlowing ? new BasicFluid.Flowing(entry) : new BasicFluid.Source(entry));
    public static final BasicFluidRegistryEntry LIQUID_GLASS = new BasicFluidRegistryEntry("liquified_glass",
            (entry, isFlowing) -> isFlowing ? new BasicFluid.Flowing(entry) : new BasicFluid.Source(entry));
    public static final BasicFluidRegistryEntry LIQUID_REDSTONE = new BasicFluidRegistryEntry("liquified_redstone",
            (entry, isFlowing) -> isFlowing ? new BasicFluid.Flowing(entry) : new BasicFluid.Source(entry));
    public static final BasicFluidRegistryEntry ENDER_REDSTONE_ALLOY = new BasicFluidRegistryEntry("ender_redstone_alloy",
            (entry, isFlowing) -> isFlowing ? new BasicFluid.Flowing(entry) : new BasicFluid.Source(entry));
    public static final BasicFluidRegistryEntry ENDER_GLASS_ALLOY = new BasicFluidRegistryEntry("ender_glass_alloy",
            (entry, isFlowing) -> isFlowing ? new BasicFluid.Flowing(entry) : new BasicFluid.Source(entry));

    public static void register(IEventBus modEventBus) {
        FLUIDS.register(modEventBus);
        FLUID_TYPES.register(modEventBus);
    }
}
