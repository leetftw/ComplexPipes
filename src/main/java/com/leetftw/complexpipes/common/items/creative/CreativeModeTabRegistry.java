package com.leetftw.complexpipes.common.items.creative;

import com.leetftw.complexpipes.common.items.ItemRegistry;
import com.leetftw.complexpipes.common.pipe.types.PipeTypeRegistry;
import com.leetftw.complexpipes.common.pipe.upgrades.PipeUpgradeRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class CreativeModeTabRegistry {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("main_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.complexpipes")) //The language key for the title of your CreativeModeTab
            .withTabsBefore(CreativeModeTabs.COMBAT)
            .icon(() -> PipeTypeRegistry.getType("item").getBlock().asItem().getDefaultInstance())
            .displayItems(CreativeModeTabRegistry::addCreativeItems).build());

    private static void addCreativeItems(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        PipeTypeRegistry.forEach(pipeType -> output.accept(pipeType.getBlock()));
        PipeUpgradeRegistry.PIPE_UPGRADE_REGISTRY.forEach(pipeUpgrade -> output.accept(pipeUpgrade.getItem()));
        ItemRegistry.ITEMS.getEntries().forEach(itemHolder -> output.accept(itemHolder.get()));
    }
}
