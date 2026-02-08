package com.leetftw.complexpipes.common.gui;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class MenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, MODID);

    public static final Supplier<MenuType<PipeConnectionMenu>> PIPE_CONNECTION_MENU = MENUS.register("pipe_connection_menu", () -> IMenuTypeExtension.create(PipeConnectionMenu::new));
    public static final Supplier<MenuType<ItemStackFilterMenu>> ITEM_STACK_FILTER_MENU = MENUS.register("item_stack_filter_menu", () -> new MenuType<>(ItemStackFilterMenu::new, FeatureFlags.DEFAULT_FLAGS));

}
