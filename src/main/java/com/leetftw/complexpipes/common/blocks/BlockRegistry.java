package com.leetftw.complexpipes.common.blocks;

import com.leetftw.complexpipes.common.block_entities.ItemMelterBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;
import static com.leetftw.complexpipes.common.items.ItemRegistry.ITEMS;

public class BlockRegistry {
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    // Creates a new Block with the id "examplemod:example_block", combining the namespace and path
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", p -> p.mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);

    public static final DeferredBlock<PipeFrameBlock> BASIC_PIPE_FRAME = BLOCKS.register("basic_pipe_frame", identifier ->
            new PipeFrameBlock(Block.Properties.of().setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), identifier)).noOcclusion().dynamicShape(), false));
    public static final DeferredBlock<PipeFrameBlock> ENHANCED_PIPE_FRAME = BLOCKS.register("enhanced_pipe_frame", identifier ->
            new PipeFrameBlock(Block.Properties.of().setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), identifier)).noOcclusion().dynamicShape(), false));
    public static final DeferredBlock<PipeFrameBlock> ADVANCED_PIPE_FRAME = BLOCKS.register("advanced_pipe_frame", identifier ->
            new PipeFrameBlock(Block.Properties.of().setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), identifier)).noOcclusion().dynamicShape(), false));
    public static final DeferredBlock<PipeFrameBlock> EXTREME_PIPE_FRAME = BLOCKS.register("extreme_pipe_frame", identifier ->
            new PipeFrameBlock(Block.Properties.of().setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), identifier)).noOcclusion().dynamicShape(), false));

    public static final DeferredItem<BlockItem> BASIC_PIPE_FRAME_ITEM = ITEMS.registerSimpleBlockItem(BASIC_PIPE_FRAME);
    public static final DeferredItem<BlockItem> ENHANCED_PIPE_FRAME_ITEM = ITEMS.registerSimpleBlockItem(ENHANCED_PIPE_FRAME);
    public static final DeferredItem<BlockItem> ADVANCED_PIPE_FRAME_ITEM = ITEMS.registerSimpleBlockItem(ADVANCED_PIPE_FRAME);
    public static final DeferredItem<BlockItem> EXTREME_PIPE_FRAME_ITEM = ITEMS.registerSimpleBlockItem(EXTREME_PIPE_FRAME);

    public static final DeferredBlock<ItemMelterBlock> ITEM_MELTER = BLOCKS.registerBlock("item_melter", ItemMelterBlock::new);
    public static final DeferredItem<BlockItem> ITEM_MELTER_ITEM = ITEMS.registerSimpleBlockItem("item_melter", ITEM_MELTER);

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
