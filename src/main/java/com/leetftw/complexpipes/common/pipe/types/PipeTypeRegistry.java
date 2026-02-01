package com.leetftw.complexpipes.common.pipe.types;

import com.leetftw.complexpipes.common.blocks.PipeBlock;
import com.leetftw.complexpipes.common.blocks.PipeBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class PipeTypeRegistry {
    private static boolean registryFixed = false;
    private static HashMap<String, PipeType<?>> registeredTypes = new HashMap<>();
    private static List<String> keys = new ArrayList<>();

    public static <T> void registerType(String name, PipeType<T> type) {
        if (registryFixed)
            throw new UnsupportedOperationException("An attempt was made to register a pipe type after registries have already settled.");

        type.setRegisteredId(name);
        registeredTypes.put(name, type);
        keys.add(name);
    }

    public static PipeType<?> getType(String name) {
        return registeredTypes.get(name);
    }
    public static void forEach(Consumer<PipeType<?>> method) {
        registeredTypes.values().forEach(method);
    }
    public static <T> Stream<T> map(Function<PipeType<?>, T> method) {
        return registeredTypes.values().stream().map(method);
    }
    public static int getNumericId(String registeredId) {
        return keys.indexOf(registeredId);
    }
    public static String getStringId(int numericId) {
        return keys.get(numericId);
    }

    public static void register(IEventBus modEventBus) {
        BuiltinPipeTypes.registerTypes();

        modEventBus.addListener((RegisterEvent event) -> {
            event.register(
                    BuiltInRegistries.BLOCK.key(),
                    registry -> {
                        for (Map.Entry<String, PipeType<?>> type : registeredTypes.entrySet()) {
                            Identifier id = Identifier.fromNamespaceAndPath(MODID, type.getKey() + "_pipe");
                            PipeBlock block = new PipeBlock(Block.Properties.of().setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), id)).noOcclusion().dynamicShape(), type.getValue());
                            type.getValue().setBlock(block);
                            registry.register(id, block);
                        }
                    }
            );
        });

        // Note: This assumes blocks are registered before items
        // (true for 1.21.11)
        modEventBus.addListener((RegisterEvent event) -> {
            event.register(
                    BuiltInRegistries.ITEM.key(),
                    registry -> {
                        for (Map.Entry<String, PipeType<?>> type : registeredTypes.entrySet()) {
                            Identifier id = Identifier.fromNamespaceAndPath(MODID, type.getKey() + "_pipe");
                            //PipeBlock block = new PipeBlock(Block.Properties.of().setId(ResourceKey.create(BuiltInRegistries.BLOCK.key(), id)).noOcclusion().dynamicShape(), type.getValue());
                            BlockItem item = new BlockItem(type.getValue().getBlock(), new Item.Properties()
                                    .setId(ResourceKey.create(BuiltInRegistries.ITEM.key(), id))
                                    .useBlockDescriptionPrefix());
                            registry.register(id, item);
                        }
                    }
            );
        });

        // Note: This assumes blocks are registered before block entities
        // (true for 1.21.11)
        modEventBus.addListener((RegisterEvent event) -> {
            event.register(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE.key(),
                    registry -> {
                        for (Map.Entry<String, PipeType<?>> type : registeredTypes.entrySet()) {
                            BlockEntityType<PipeBlockEntity> blockEntityType = new BlockEntityType<>(
                                    ((pos, state) -> new PipeBlockEntity(pos, state, type.getValue())),
                                    type.getValue().getBlock()
                            );
                            type.getValue().setBeType(blockEntityType);
                            registry.register(Identifier.fromNamespaceAndPath(MODID, type.getKey() + "_pipe_be"), blockEntityType);
                        }
                    }
            );
        });
    }
}
