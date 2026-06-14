package com.leetftw.complexpipes.client.datagen.providers;

import com.leetftw.complexpipes.common.crafting.ItemMelterRecipe;
import com.leetftw.complexpipes.common.crafting.RecipeTypeRegistry;
import com.leetftw.complexpipes.common.fluids.BasicFluidRegistryEntry;
import com.leetftw.complexpipes.common.fluids.FluidRegistry;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.concurrent.CompletableFuture;

public class ItemMelterRecipeProvider extends RecipeProvider {
    protected ItemMelterRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    private void buildRecipe(Item inputItem, Fluid outputFluid, int outputAmount) {
        String itemId = BuiltInRegistries.ITEM.getKey(inputItem).getPath();
        String fluidID = BuiltInRegistries.FLUID.getKey(outputFluid).getPath();

        new ItemMelterRecipeBuilder(
                Ingredient.of(inputItem),
                outputFluid,
                outputAmount
        )
        .unlockedBy("has_" + itemId, this.has(Items.ENDER_PEARL))
        .save(output, itemId + "_to_" + fluidID);
    }

    @Override
    protected void buildRecipes() {
        buildRecipe(Items.ENDER_PEARL, FluidRegistry.LIQUID_ENDER.getSource(), 250);
        buildRecipe(Items.REDSTONE, FluidRegistry.LIQUID_REDSTONE.getSource(), 250);
        buildRecipe(Items.GLASS, FluidRegistry.LIQUID_GLASS.getSource(), 500);

    }

    public static class Runner extends RecipeProvider.Runner {
         public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
            super(output, lookupProvider);
        }

        @Override
        protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput output) {
            return new ItemMelterRecipeProvider(provider, output);
        }

        @Override
        public String getName() {
            return "ItemMelterRecipeProvider";
        }
    }
}
