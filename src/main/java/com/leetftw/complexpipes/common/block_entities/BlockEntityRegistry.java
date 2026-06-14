package com.leetftw.complexpipes.common.block_entities;

import com.leetftw.complexpipes.common.blocks.BlockRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class BlockEntityRegistry {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);

    public static final Supplier<BlockEntityType<ItemMelterBlockEntity>> ITEM_MELTER_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register(
            "item_melter_be", () -> new BlockEntityType<>(
                    ItemMelterBlockEntity::new,
                    BlockRegistry.ITEM_MELTER.get()
            )
    );

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
        modEventBus.addListener((RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(
                    Capabilities.Item.BLOCK,
                    ITEM_MELTER_BLOCK_ENTITY.get(),
                    ItemMelterBlockEntity::getItemHandler
            );
            event.registerBlockEntity(
                    Capabilities.Fluid.BLOCK,
                    ITEM_MELTER_BLOCK_ENTITY.get(),
                    ItemMelterBlockEntity::getFluidHandler
            );
        });
    }
}
