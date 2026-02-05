package com.leetftw.complexpipes.common.items;

import com.leetftw.complexpipes.common.cards.PipeCard;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class ItemComponentRegistry {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

    public static final Supplier<DataComponentType<PipeCard>> PIPE_CARD_DATA = COMPONENTS.registerComponentType(
            "card_data",
            builder -> builder
                    // The codec to read/write the data to disk
                    .persistent(PipeCard.CODEC)
    );
}
