package com.leetftw.complexpipes.common.items;

import com.leetftw.complexpipes.common.pipe.upgrade.PipeUpgrade;
import com.leetftw.complexpipes.common.pipe.upgrade.StackPipeUpgrade;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.leetftw.complexpipes.common.PipeMod.MODID;
import static com.leetftw.complexpipes.common.pipe.upgrade.PipeUpgrade.BASIC_CODEC;

public class ItemComponentRegistry {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MODID);

    public static final Supplier<DataComponentType<PipeUpgrade>> PIPE_UPGRADE = COMPONENTS.registerComponentType(
            "pipe_upgrade",
            builder -> builder
                    // The codec to read/write the data to disk
                    .persistent(PipeUpgrade.CODEC)
    );
}
