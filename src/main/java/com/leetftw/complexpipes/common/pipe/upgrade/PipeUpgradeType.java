package com.leetftw.complexpipes.common.pipe.upgrade;

import com.leetftw.complexpipes.common.items.PipeUpgradeItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class PipeUpgradeType {
    private final MapCodec<? extends PipeUpgrade> CODEC;
    private final Supplier<PipeUpgrade> CONSTRUCTOR;
    private PipeUpgradeItem item;

    public PipeUpgradeType(MapCodec<? extends PipeUpgrade> codec, Supplier<PipeUpgrade> constructor) {
        CODEC = codec;
        CONSTRUCTOR = constructor;
    }

    public MapCodec<? extends PipeUpgrade> getCodec() {
        return CODEC;
    }

    public PipeUpgradeItem getItem() {
        return item;
    }

    public PipeUpgrade instantiate() {
        return CONSTRUCTOR.get();
    }

    void receiveItem(PipeUpgradeItem item) {
        this.item = item;
    }
}
