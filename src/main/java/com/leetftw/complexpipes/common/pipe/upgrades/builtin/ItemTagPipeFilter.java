package com.leetftw.complexpipes.common.pipe.upgrades.builtin;

import com.leetftw.complexpipes.common.pipe.upgrades.PipeUpgrade;
import com.leetftw.complexpipes.common.pipe.upgrades.PipeUpgradeType;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class ItemTagPipeFilter extends PipeUpgrade {
    public static final int SLOT_COUNT = 9;

    @SuppressWarnings("unchecked")
    public TagKey<Item>[] items = new TagKey[SLOT_COUNT];
    public PipeFilterMode mode = PipeFilterMode.WHITELIST;

    private Predicate<Object> predicate = null;

    public static final MapCodec<ItemTagPipeFilter> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    Codec.list(Codec.pair(Codec.intRange(0, SLOT_COUNT - 1).fieldOf("slot").codec(), TagKey.codec(Registries.ITEM).fieldOf("resource").codec())).fieldOf("resources").forGetter(a -> {
                        List<Pair<Integer, TagKey<Item>>> pairs = new ArrayList<>();
                        for (int i = 0; i < SLOT_COUNT; i++)
                            if (a.items[i] != null)
                                pairs.add(new Pair<>(i, a.items[i]));
                        return pairs;
                    }),
                    Codec.STRING.fieldOf("mode").forGetter(a -> a.getMode().name())
            ).apply(builder, ItemTagPipeFilter::new)
    );

    public ItemTagPipeFilter() {

    }

    private ItemTagPipeFilter(List<Pair<Integer, TagKey<Item>>> items, String mode) {
        this.mode = PipeFilterMode.valueOf(mode);
        for (Pair<Integer, TagKey<Item>> storedItem : items)
            this.items[storedItem.getFirst()] = storedItem.getSecond();
    }

    public PipeFilterMode getMode() {
        return mode;
    }

    @Override
    public PipeUpgradeType getType() {
        return null;
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
    public Predicate<Object> getFilter() {
        if (predicate == null)
            predicate = a -> !(a instanceof ItemStack stack) || Arrays.stream(items).anyMatch(stack::is);
        return predicate;
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
        if (!(obj instanceof ItemTagPipeFilter upgrade)) return false;
        return mode == upgrade.mode && Arrays.equals(items, upgrade.items);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mode, Arrays.hashCode(items));
    }
}
