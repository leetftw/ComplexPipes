package com.leetftw.complexpipes.client.datagen.providers;

import com.leetftw.complexpipes.common.blocks.BlockRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class PipeRecipeProvider extends net.minecraft.data.recipes.RecipeProvider {
    public PipeRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        shaped(RecipeCategory.REDSTONE, BlockRegistry.BASIC_PIPE_FRAME_ITEM, 8)
                .define('I', Items.IRON_INGOT)
                .pattern(" I ")
                .pattern("I I")
                .pattern(" I ")
                .unlockedBy("has_iron_ingot", this.has(Items.IRON_INGOT))
                .save(output);

        shaped(RecipeCategory.REDSTONE, BlockRegistry.ENHANCED_PIPE_FRAME, 8)
                .define('F', BlockRegistry.BASIC_PIPE_FRAME)
                .define('C', Items.GOLD_INGOT)
                .pattern("FFF")
                .pattern("FCF")
                .pattern("FFF")
                .unlockedBy("has_gold_ingot", this.has(Items.GOLD_INGOT))
                .save(output);

        shaped(RecipeCategory.REDSTONE, BlockRegistry.ADVANCED_PIPE_FRAME, 8)
                .define('F', BlockRegistry.ENHANCED_PIPE_FRAME)
                .define('C', Items.DIAMOND)
                .pattern("FFF")
                .pattern("FCF")
                .pattern("FFF")
                .unlockedBy("has_diamond", this.has(Items.DIAMOND))
                .save(output);

        shaped(RecipeCategory.REDSTONE, BlockRegistry.EXTREME_PIPE_FRAME, 8)
                .define('F', BlockRegistry.ADVANCED_PIPE_FRAME)
                .define('C', Items.NETHERITE_INGOT)
                .pattern("FFF")
                .pattern("FCF")
                .pattern("FFF")
                .unlockedBy("has_netherite_ingot", this.has(Items.NETHERITE_INGOT))
                .save(output);
    }

    // The runner to add to the data generator
    public static class Runner extends RecipeProvider.Runner {
        // Get the parameters from the `GatherDataEvent`s.
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new PipeRecipeProvider(provider, output);
        }

        @Override
        public String getName() {
            return "PipeRecipeProvider";
        }
    }
}
