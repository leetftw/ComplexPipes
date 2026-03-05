package com.leetftw.complexpipes.common.cards;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public abstract class PipeCard {
    // Thanks Commoble!
    public static final Codec<MapCodec<? extends PipeCard>> BASIC_CODEC = Identifier.CODEC.flatXmap(
            id -> {
                var mapCodec = PipeCardRegistry.PIPE_CARD_TYPE_REGISTRY.getValue(id);
                return mapCodec == null ? DataResult.error(() -> "Invalid upgrade identifier: " + id) : DataResult.success(mapCodec.getCodec());
            },
            mapCodec -> {
                var id = PipeCardRegistry.PIPE_CARD_TYPE_REGISTRY.filterElements(type -> type.getCodec() == mapCodec).listElementIds().map(ResourceKey::identifier).findFirst();
                return id.map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unregistered upgrade codec: " + mapCodec));
            }
    );

    public static final Codec<PipeCard> CODEC = BASIC_CODEC.dispatch(
            // first argument is BaseClass -> ThingType
            // but our ThingType is just MapCodec here
            // so we can use that abstract codec method we made earlier
            PipeCard::codec,
            // second argument is ThingType -> MapCodec
            // but our ThingType is already a MapCodec, so we can just do this
            Function.identity());

    public abstract int getMaxInstalledCount();
    public abstract MapCodec<? extends PipeCard> codec();

    public abstract PipeCardType getType();

    public boolean isFilter() {
        return false;
    }
    public boolean allowResourceTransfer(Object object) {
        return true;
    }

    public @Nullable String getRoutingStrategyId() {
        return null;
    }

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();

    public boolean compatibleWith(PipeCard card) {
        return true;
    };
}
