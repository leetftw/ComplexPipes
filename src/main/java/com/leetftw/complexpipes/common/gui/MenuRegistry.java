package com.leetftw.complexpipes.common.gui;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.leetftw.complexpipes.common.PipeMod.MODID;

public class MenuRegistry {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(BuiltInRegistries.MENU, MODID);

    public static final Supplier<MenuType<PipeConnectionMenu>> PIPE_CONNECTION_MENU = MENUS.register("my_menu", () -> new MenuType<>(PipeConnectionMenu::new, FeatureFlags.DEFAULT_FLAGS));

}
