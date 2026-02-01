package com.leetftw.complexpipes.common.gui;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class MenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, MODID);

    public static final Supplier<MenuType<PipeConnectionMenu>> PIPE_CONNECTION_MENU = MENUS.register("my_menu", () -> new MenuType<>(PipeConnectionMenu::new, FeatureFlags.DEFAULT_FLAGS));
    public static final Supplier<MenuType<ItemStackFilterMenu>> ITEM_STACK_FILTER_MENU = MENUS.register("my_menu2", () -> new MenuType<>(ItemStackFilterMenu::new, FeatureFlags.DEFAULT_FLAGS));

}
