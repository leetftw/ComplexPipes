package com.leetftw.complexpipes.common.pipe.upgrades.builtin;

import com.leetftw.complexpipes.common.ServerConfig;
import com.leetftw.complexpipes.common.pipe.upgrades.BuiltinPipeUpgrades;
import com.leetftw.complexpipes.common.pipe.upgrades.PipeUpgrade;
import com.leetftw.complexpipes.common.pipe.upgrades.PipeUpgradeType;
import com.mojang.serialization.MapCodec;

import java.util.function.Predicate;

/// Increases the stack size transferred per pipe operation
public class StackPipeUpgrade extends PipeUpgrade {
    public static final StackPipeUpgrade INSTANCE = new StackPipeUpgrade();
    public static final MapCodec<StackPipeUpgrade> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public PipeUpgradeType getType() {
        return BuiltinPipeUpgrades.STACK_UPGRADE;
    }

    @Override
    public int getMinTransferAmount() {
        return -1;
    }

    @Override
    public int getMaxTransferAmount() {
        return -1;
    }

    @Override
    public double getTransferIntervalMultiplier() {
        return 1;
    }

    @Override
    public double getTransferAmountMultiplier() {
        return ServerConfig.STACK_UPGRADE_MULTIPLIER.get();
    }

    @Override
    public int getMaxInstalledCount() {
        return ServerConfig.MAX_STACK_UPGRADES.get();

        // Upgrades: 0,   1,   2,    3,    4,    5,    6
        // Items:    1,   2,   4,    8,    16,   32,   64     items/op
        // Fluid:    250, 500, 1000, 2000, 4000, 8000, 16000  mB/op
    }

    @Override
    public MapCodec<? extends PipeUpgrade> codec() {
        return CODEC;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StackPipeUpgrade;
    }

    @Override
    public int hashCode() {
        return "StackPipeUpgrade".hashCode();
    }
}
