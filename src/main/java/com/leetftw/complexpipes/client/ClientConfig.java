package com.leetftw.complexpipes.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue RENDER_PIPE_BE = BUILDER
            .comment("Whether to use render pipes as block entities instead of blocks. This can prevent lag spikes when placing down many pipes in a single chunk, but reduces overall performance when many pipes are on screen.")
            .define("renderPipeAsBlockEntity", false);

    static final ModConfigSpec SPEC = BUILDER.build();
}
