package com.leetftw.complexpipes.common.blocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;
import static com.leetftw.complexpipes.common.items.ItemRegistry.ITEMS;

public class BlockRegistry {
    // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    // Creates a new Block with the id "examplemod:example_block", combining the namespace and path
    public static final DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerSimpleBlock("example_block", p -> p.mapColor(MapColor.STONE));
    // Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);


    /*public static final DeferredBlock<PipeBlock> ITEM_PIPE = BLOCKS.registerBlock("item_pipe",
            properties -> new PipeBlock(properties.noOcclusion().dynamicShape(), PipeTypeRegistry.getType("item")));
    public static final DeferredItem<BlockItem> ITEM_PIPE_ITEM = ITEMS.registerSimpleBlockItem(ITEM_PIPE);

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static final Supplier<BlockEntityType<PipeBlockEntity>> PIPE_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "pipe_entity",
            () -> new BlockEntityType<>(
                    (pos, state) -> new PipeBlockEntity(pos, state, PipeTypeRegistry.getType("item")),
                    false,
                    ITEM_PIPE.get()
            )
    );*/
}
