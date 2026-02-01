package com.leetftw.complexpipes.common.pipe.upgrades;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

import java.util.function.Function;
import java.util.function.Predicate;

public abstract class PipeUpgrade {
    // Thanks Commoble!
    public static final Codec<MapCodec<? extends PipeUpgrade>> BASIC_CODEC = Identifier.CODEC.flatXmap(
            id -> {
                var mapCodec = PipeUpgradeRegistry.PIPE_UPGRADE_REGISTRY.getValue(id);
                return mapCodec == null ? DataResult.error(() -> "Invalid upgrade identifier: " + id) : DataResult.success(mapCodec.getCodec());
            },
            mapCodec -> {
                var id = PipeUpgradeRegistry.PIPE_UPGRADE_REGISTRY.filterElements(type -> type.getCodec() == mapCodec).listElementIds().map(ResourceKey::identifier).findFirst();
                return id.map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unregistered upgrade codec: " + mapCodec));
            }
    );

    public static final Codec<PipeUpgrade> CODEC = BASIC_CODEC.dispatch(
            // first argument is BaseClass -> ThingType
            // but our ThingType is just MapCodec here
            // so we can use that abstract codec method we made earlier
            PipeUpgrade::codec,
            // second argument is ThingType -> MapCodec
            // but our ThingType is already a MapCodec, so we can just do this
            Function.identity());

    public abstract PipeUpgradeType getType();

    public abstract int getMinTransferAmount();
    public abstract int getMaxTransferAmount();

    public abstract double getTransferIntervalMultiplier();
    public abstract double getTransferAmountMultiplier();

    public boolean isFilter() {
        return false;
    }
    public boolean allowResourceTransfer(Object object) {
        return true;
    }

    public abstract int getMaxInstalledCount();
    public abstract MapCodec<? extends PipeUpgrade> codec();

    @Override
    public abstract boolean equals(Object obj);

    @Override
    public abstract int hashCode();
}
