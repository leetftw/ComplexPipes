package com.leetftw.complexpipes.common.tests;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class GameTestRegistry {

    public static final DeferredRegister<Consumer<GameTestHelper>> TEST_FUNCTION_REGISTRY = DeferredRegister.create(BuiltInRegistries.TEST_FUNCTION, MODID);
    public static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BASIC_TEST = TEST_FUNCTION_REGISTRY.register("basic_test", () -> (GameTestHelper helper) -> {

    });
}
