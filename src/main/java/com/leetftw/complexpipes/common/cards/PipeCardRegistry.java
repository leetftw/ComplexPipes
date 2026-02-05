package com.leetftw.complexpipes.common.cards;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class PipeCardRegistry {
    public static final ResourceKey<Registry<PipeCardType>> PIPE_CARD_TYPE_REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MODID, "upgrade_codecs"));
    public static final Registry<PipeCardType> PIPE_CARD_TYPE_REGISTRY = new RegistryBuilder<>(PIPE_CARD_TYPE_REGISTRY_KEY)
            //.sync(true)
            .maxId(256)
            .create();

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener((NewRegistryEvent event) -> {
            event.register(PIPE_CARD_TYPE_REGISTRY);
        });

        modEventBus.addListener((RegisterEvent event) -> {
            event.register(PIPE_CARD_TYPE_REGISTRY_KEY, BuiltinPipeCards::registerCards);
            event.register(BuiltInRegistries.ITEM.key(), BuiltinPipeCards::registerUpgradeItems);
        });
    }
}
