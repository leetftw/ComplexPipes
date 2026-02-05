package com.leetftw.complexpipes.common.cards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.function.Function;

public abstract class PipeUpgrade extends PipeCard {
    public abstract int getMinTransferAmount();
    public abstract int getMaxTransferAmount();

    public abstract double getTransferIntervalMultiplier();
    public abstract double getTransferAmountMultiplier();
}
