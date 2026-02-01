package com.leetftw.complexpipes.common;

import com.leetftw.complexpipes.common.gui.MenuRegistry;
import com.leetftw.complexpipes.common.items.ItemComponentRegistry;
import com.leetftw.complexpipes.common.items.creative.CreativeModeTabRegistry;
import com.leetftw.complexpipes.common.network.PipeSyncPayload;
import com.leetftw.complexpipes.common.pipe.types.PipeTypeRegistry;
import com.leetftw.complexpipes.common.pipe.upgrades.PipeUpgradeRegistry;
import com.leetftw.complexpipes.common.tests.GameTestRegistry;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
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
        BlockRegistry.BLOCKS.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        ItemComponentRegistry.COMPONENTS.register(modEventBus);
        MenuRegistry.MENUS.register(modEventBus);
        CreativeModeTabRegistry.CREATIVE_MODE_TABS.register(modEventBus);

        PipeTypeRegistry.register(modEventBus);
        PipeUpgradeRegistry.register(modEventBus);

        GameTestRegistry.TEST_FUNCTION_REGISTRY.register(modEventBus);

        modEventBus.addListener(ComplexPipes::registerPayloads);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                PipeSyncPayload.TYPE,
                PipeSyncPayload.STREAM_CODEC
        );
    }
}
