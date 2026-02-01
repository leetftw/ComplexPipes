package com.leetftw.complexpipes.common.pipe.upgrades.builtin;

import com.leetftw.complexpipes.common.pipe.upgrades.BuiltinPipeUpgrades;
import com.leetftw.complexpipes.common.pipe.upgrades.PipeUpgrade;
import com.leetftw.complexpipes.common.pipe.upgrades.PipeUpgradeType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ItemStackPipeFilter extends PipeUpgrade {
    public static final int SLOT_COUNT = 9;

    public ItemResource[] items = new ItemResource[SLOT_COUNT];
    public PipeFilterMode mode = PipeFilterMode.WHITELIST;

    private Predicate<Object> predicate = null;

    public static final MapCodec<ItemStackPipeFilter> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Codec.list(Codec.pair(Codec.intRange(0, SLOT_COUNT - 1).fieldOf("slot").codec(), ItemResource.CODEC.fieldOf("resource").codec())).fieldOf("resources").forGetter(a -> {
                        List<Pair<Integer, ItemResource>> pairs = new ArrayList<>();
                        for (int i = 0; i < SLOT_COUNT; i++)
                            if (a.items[i] != null)
                                pairs.add(new Pair<>(i, a.items[i]));
                        return pairs;
                    }),
                    Codec.STRING.fieldOf("mode").forGetter(a -> a.getMode().name())
            ).apply(builder, ItemStackPipeFilter::new)
    );

    public ItemStackPipeFilter() {

    }

    private ItemStackPipeFilter(List<Pair<Integer, ItemResource>> items, String mode) {
        this.mode = PipeFilterMode.valueOf(mode);
        for (Pair<Integer, ItemResource> storedItem : items)
            this.items[storedItem.getFirst()] = storedItem.getSecond();
    }

    @Override
    public ItemStackPipeFilter clone() {
        ItemStackPipeFilter cloned = new ItemStackPipeFilter();
        cloned.items = items.clone();
        cloned.mode = mode;
        return cloned;
    }

    public PipeFilterMode getMode() {
        return mode;
    }

    @Override
    public PipeUpgradeType getType() {
        return BuiltinPipeUpgrades.ITEM_STACK_FILTER;
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
        return 1;
    }

    @Override
    public boolean isFilter() {
        return true;
    }

    @Override
    public boolean allowResourceTransfer(Object object) {
        if (!(object instanceof ItemResource resource))
            return false;

        for (ItemResource item : items)
            if (resource.equals(item))
                return mode == PipeFilterMode.WHITELIST;

        return mode != PipeFilterMode.WHITELIST;
    }

    @Override
    public int getMaxInstalledCount() {
        return 1;
    }

    @Override
    public MapCodec<? extends PipeUpgrade> codec() {
        return CODEC;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ItemStackPipeFilter upgrade)) return false;
        return mode == upgrade.mode && Arrays.equals(items, upgrade.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, Arrays.hashCode(items));
    }
}
