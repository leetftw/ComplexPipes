package com.leetftw.complexpipes.common.cards;

import com.leetftw.complexpipes.common.items.PipeCardItem;
import com.mojang.serialization.MapCodec;

import java.util.function.Supplier;

public class PipeCardType {
    private final MapCodec<? extends PipeCard> CODEC;
    private final Supplier<PipeCard> CONSTRUCTOR;
    private PipeCardItem item;

    public PipeCardType(MapCodec<? extends PipeCard> codec, Supplier<PipeCard> constructor) {
        CODEC = codec;
        CONSTRUCTOR = constructor;
    }

    public MapCodec<? extends PipeCard> getCodec() {
        return CODEC;
    }

    public PipeCardItem getItem() {
        return item;
    }

    public PipeCard instantiate() {
        return CONSTRUCTOR.get();
    }

    void receiveItem(PipeCardItem item) {
        this.item = item;
    }
}
