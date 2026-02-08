package com.leetftw.complexpipes.common.cards.builtin;

import com.leetftw.complexpipes.common.cards.PipeCard;
import com.leetftw.complexpipes.common.cards.PipeCardRegistry;
import com.leetftw.complexpipes.common.cards.PipeCardType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class RouterPipeCard extends PipeCard {
    private final String strategyId;
    public static final MapCodec<RouterPipeCard> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Codec.STRING.fieldOf("strategy").forGetter(RouterPipeCard::getRoutingStrategyId)
            ).apply(builder, RouterPipeCard::new)
    );

    public RouterPipeCard(String strategyId) {
        this.strategyId = strategyId;
    }

    @Override
    public @NonNull String getRoutingStrategyId() {
        return strategyId;
    }

    @Override
    public int getMaxInstalledCount() {
        return 1;
    }

    @Override
    public MapCodec<? extends PipeCard> codec() {
        return CODEC;
    }

    @Override
    public PipeCardType getType() {
        return PipeCardRegistry.PIPE_CARD_TYPE_REGISTRY.getValue(Identifier.fromNamespaceAndPath(MODID, strategyId + "_router"));
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof RouterPipeCard pipeCard) && (pipeCard.getRoutingStrategyId().equals(getRoutingStrategyId()));
    }

    @Override
    public int hashCode() {
        return "RouterPipeCard".hashCode() * 17 + strategyId.hashCode();
    }
}
