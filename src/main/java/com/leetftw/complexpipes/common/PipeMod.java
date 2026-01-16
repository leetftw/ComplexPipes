package com.leetftw.complexpipes.common;

import com.leetftw.complexpipes.client.gui.PipeConnectionScreen;
import com.leetftw.complexpipes.common.gui.MenuRegistry;
import com.leetftw.complexpipes.common.items.ItemComponentRegistry;
import com.leetftw.complexpipes.common.pipe.types.PipeTypeRegistry;
import com.leetftw.complexpipes.common.pipe.upgrade.PipeUpgradeRegistry;
import com.leetftw.complexpipes.common.pipe.upgrade.StackPipeUpgrade;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.leetftw.complexpipes.common.blocks.BlockRegistry;
import com.leetftw.complexpipes.common.items.ItemRegistry;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(PipeMod.MODID)
public class PipeMod {
    // Define mod id in a common place for everything to reference
    public static final String MODID = "complexpipes";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    /*// Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("example_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.examplemod")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> ItemRegistry.STACK_UPGRADE_ITEM.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                ItemStack debug = new ItemStack(ItemRegistry.DEBUG_ITEM.get(), 1);
                debug.set(ItemComponentRegistry.PIPE_UPGRADE, StackPipeUpgrade.INSTANCE);
                output.accept(debug);
                output.accept(ItemRegistry.STACK_UPGRADE_ITEM.get()); // Add the example item to the tab. For your own tabs, this method is preferred over the event
            }).build());*/

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public PipeMod(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        BlockRegistry.BLOCKS.register(modEventBus);
        ItemRegistry.ITEMS.register(modEventBus);
        //CREATIVE_MODE_TABS.register(modEventBus);
        ItemComponentRegistry.COMPONENTS.register(modEventBus);
        MenuRegistry.MENUS.register(modEventBus);

        PipeTypeRegistry.register(modEventBus);
        PipeUpgradeRegistry.register(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        LOGGER.info("HELLO FROM COMMON SETUP");

        if (Config.LOG_DIRT_BLOCK.getAsBoolean()) {
            LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));
        }

        LOGGER.info("{}{}", Config.MAGIC_NUMBER_INTRODUCTION.get(), Config.MAGIC_NUMBER.getAsInt());

        Config.ITEM_STRINGS.get().forEach((item) -> LOGGER.info("ITEM >> {}", item));
    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS) {
            event.accept(BlockRegistry.EXAMPLE_BLOCK_ITEM);
        }
    }
}
