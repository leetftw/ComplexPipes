package com.leetftw.complexpipes.common;

import com.leetftw.complexpipes.common.gui.MenuRegistry;
import com.leetftw.complexpipes.common.items.ItemComponentRegistry;
import com.leetftw.complexpipes.common.items.creative.CreativeModeTabRegistry;
import com.leetftw.complexpipes.common.network.PipeScreenNumericSyncPayload;
import com.leetftw.complexpipes.common.pipe.types.PipeTypeRegistry;
import com.leetftw.complexpipes.common.pipe.upgrades.PipeUpgradeRegistry;
import com.leetftw.complexpipes.common.tests.GameRuleRegistry;
import com.leetftw.complexpipes.common.tests.GameTestRegistry;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

import com.leetftw.complexpipes.common.blocks.BlockRegistry;
import com.leetftw.complexpipes.common.items.ItemRegistry;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ComplexPipes.MODID)
public class ComplexPipes {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "complexpipes";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ComplexPipes(IEventBus modEventBus, ModContainer modContainer) {
        // Register vanilla registries
        BlockRegistry.BLOCKS.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        ItemComponentRegistry.COMPONENTS.register(modEventBus);
        MenuRegistry.MENUS.register(modEventBus);
        CreativeModeTabRegistry.CREATIVE_MODE_TABS.register(modEventBus);
        GameRuleRegistry.GAME_RULE_REGISTRY.register(modEventBus);
        GameTestRegistry.TEST_FUNCTION_REGISTRY.register(modEventBus);

        // Register custom registries
        PipeTypeRegistry.register(modEventBus);
        PipeUpgradeRegistry.register(modEventBus);

        // Register network packets
        modEventBus.addListener(ComplexPipes::registerPayloads);

        // Register config
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfig.SPEC);
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1")
                        .executesOn(HandlerThread.MAIN);

        registrar.playToServer(
                PipeScreenNumericSyncPayload.TYPE,
                PipeScreenNumericSyncPayload.STREAM_CODEC,
                PipeScreenNumericSyncPayload::handleReceivedOnServer
        );
    }
}
