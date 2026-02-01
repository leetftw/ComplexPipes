package com.leetftw.complexpipes.common.tests;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.serialization.Codec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.*;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class GameRuleRegistry {
    public static final DeferredRegister<GameRule<?>> GAME_RULE_REGISTRY = DeferredRegister.create(BuiltInRegistries.GAME_RULE, MODID);
    public static final DeferredHolder<GameRule<?>, GameRule<Boolean>> NO_PIPE_FILTER = GAME_RULE_REGISTRY.register("no_pipe_filtering", () -> new GameRule<>(
            GameRuleCategory.MISC,
            GameRuleType.BOOL,
            BoolArgumentType.bool(),
            GameRuleTypeVisitor::visitBoolean,
            Codec.BOOL,
            p_460985_ -> p_460985_ ? 1 : 0,
            false,
            FeatureFlagSet.of()
    ));
}
