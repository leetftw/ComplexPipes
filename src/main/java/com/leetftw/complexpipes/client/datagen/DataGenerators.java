package com.leetftw.complexpipes.client.datagen;

import com.leetftw.complexpipes.client.datagen.providers.BlockLootTableProvider;
import com.leetftw.complexpipes.client.datagen.providers.BlockModelProvider;
import com.leetftw.complexpipes.client.datagen.providers.PipeRecipeProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

@EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
public class DataGenerators
{
    @SubscribeEvent
    public static void onGatherData(GatherDataEvent.Client event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(true, new LootTableProvider(packOutput, Set.of(), List.of(new LootTableProvider.SubProviderEntry(
                BlockLootTableProvider::new,
                LootContextParamSets.BLOCK
        )), lookupProvider));

        event.createProvider(BlockModelProvider::new);
        event.createProvider(PipeRecipeProvider.Runner::new);
    }
}