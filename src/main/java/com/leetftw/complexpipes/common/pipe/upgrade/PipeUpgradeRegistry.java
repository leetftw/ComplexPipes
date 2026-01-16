package com.leetftw.complexpipes.common.pipe.upgrade;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.leetftw.complexpipes.common.items.ItemComponentRegistry;
import com.leetftw.complexpipes.common.items.PipeUpgradeItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

import java.util.HashMap;
import java.util.Map;

import static com.leetftw.complexpipes.common.PipeMod.MODID;

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
