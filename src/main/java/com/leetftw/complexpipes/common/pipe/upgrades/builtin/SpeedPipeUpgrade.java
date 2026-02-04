package com.leetftw.complexpipes.common.pipe.upgrades.builtin;

import com.leetftw.complexpipes.common.ServerConfig;
import com.leetftw.complexpipes.common.pipe.upgrades.BuiltinPipeUpgrades;
import com.leetftw.complexpipes.common.pipe.upgrades.PipeUpgrade;
import com.leetftw.complexpipes.common.pipe.upgrades.PipeUpgradeType;
import com.mojang.serialization.MapCodec;

import java.util.function.Predicate;

/// Increases the stack size transferred per pipe operation
public class SpeedPipeUpgrade extends PipeUpgrade {
    public static final SpeedPipeUpgrade INSTANCE = new SpeedPipeUpgrade();
    public static final MapCodec<SpeedPipeUpgrade> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public PipeUpgradeType getType() {
        return BuiltinPipeUpgrades.SPEED_UPGRADE;
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
        return ServerConfig.SPEED_UPGRADE_MULTIPLIER.get();
    }

    @Override
    public double getTransferAmountMultiplier() {
        return 1;
    }

    @Override
    public int getMaxInstalledCount() {
        return ServerConfig.MAX_SPEED_UPGRADES.get();

        // Upgrades:    0,   1,   2,    3,           4
        // Item/fluid:  20,  10,  5,    2.5 (=3),    1  t/op
        // Energy:      1    (     incompatible      )  t/op
    }

    @Override
    public MapCodec<? extends PipeUpgrade> codec() {
        return CODEC;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SpeedPipeUpgrade;
    }

    @Override
    public int hashCode() {
        return "SpeedPipeUpgrade".hashCode();
    }
}
