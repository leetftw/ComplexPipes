package com.leetftw.complexpipes.common.pipe.upgrade;

import com.mojang.serialization.MapCodec;

import java.util.function.Predicate;

/// Increases the stack size transferred per pipe operation for energy pipes
public class EnergyPipeUpgrade extends PipeUpgrade {
    public static final EnergyPipeUpgrade INSTANCE = new EnergyPipeUpgrade();
    public static final MapCodec<EnergyPipeUpgrade> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public PipeUpgradeType getType() {
        return BuiltinPipeUpgrades.ENERGY_UPGRADE;
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
        return 4;
    }

    @Override
    public Predicate<Object> getFilter() {
        return a -> true;
    }

    @Override
    public int getMaxInstalledCount() {
        return 6;

        // Upgrades: 0,   1,   2,    3,    4,    5,    6
        // Energy:   128, 512, 2048, 8192, 32k,  128k, 512k   FE/t
    }

    @Override
    public MapCodec<? extends PipeUpgrade> codec() {
        return CODEC;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EnergyPipeUpgrade;
    }

    @Override
    public int hashCode() {
        return "EnergyPipeUpgrade".hashCode();
    }
}
