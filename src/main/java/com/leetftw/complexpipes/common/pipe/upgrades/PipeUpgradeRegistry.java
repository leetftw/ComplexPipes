package com.leetftw.complexpipes.common.pipe.upgrades;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class PipeUpgradeRegistry {
    public static final ResourceKey<Registry<PipeUpgradeType>> PIPE_UPGRADE_REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath(MODID, "upgrade_codecs"));
    public static final Registry<PipeUpgradeType> PIPE_UPGRADE_REGISTRY = new RegistryBuilder<>(PIPE_UPGRADE_REGISTRY_KEY)
            //.sync(true)
            .maxId(256)
            .create();

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener((NewRegistryEvent event) -> {
            event.register(PIPE_UPGRADE_REGISTRY);
        });

        modEventBus.addListener((RegisterEvent event) -> {
            event.register(PIPE_UPGRADE_REGISTRY_KEY, BuiltinPipeUpgrades::registerUpgrades);
            event.register(BuiltInRegistries.ITEM.key(), BuiltinPipeUpgrades::registerUpgradeItems);
        });
    }
}
