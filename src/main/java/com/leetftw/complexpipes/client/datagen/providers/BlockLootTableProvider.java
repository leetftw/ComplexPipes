package com.leetftw.complexpipes.client.datagen.providers;

import com.leetftw.complexpipes.common.pipe.types.PipeType;
import com.leetftw.complexpipes.common.pipe.types.PipeTypeRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.NonNull;

import java.util.Set;

public class BlockLootTableProvider extends BlockLootSubProvider {
    HolderLookup.Provider registryLookup;

    public BlockLootTableProvider(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        registryLookup = registries;
    }

    @Override
    protected void generate()
    {
        PipeTypeRegistry.map(PipeType::getBlock).forEach(this::dropSelf);
    }

    @Override
    protected @NonNull Iterable<Block> getKnownBlocks()
    {
        return PipeTypeRegistry.map(PipeType::getBlock).map(pipeBlock -> (Block) pipeBlock)::iterator;
    }
}
