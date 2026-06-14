package com.leetftw.complexpipes.common.crafting;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ItemMelterRecipe implements Recipe<ItemMelterRecipeInput> {

    private final Ingredient input;
    private final Fluid output;
    private final int output_amount;

    private PlacementInfo info;

    public ItemMelterRecipe(Ingredient input, Fluid output, int amount) {
        this.input = input;
        this.output = output;
        this.output_amount = amount;
    }

    @Override
    public boolean matches(ItemMelterRecipeInput input, Level level) {
        return this.input.test(input.getItem(0));
    }

    @Override
    public ItemStack assemble(ItemMelterRecipeInput itemMelterRecipeInput) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean showNotification() {
        return false;
    }

    @Override
    public String group() {
        return "";
    }

    public Ingredient getInput() {
        return input;
    }

    public Fluid getOutput() {
        return output;
    }

    public int getOutputAmount() {
        return output_amount;
    }

    public FluidStack getOutputStack() {
        return new FluidStack(output, output_amount);
    }

    @Override
    public RecipeSerializer<? extends Recipe<ItemMelterRecipeInput>> getSerializer() {
        return RecipeTypeRegistry.ITEM_MELTER_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<ItemMelterRecipeInput>> getType() {
        return RecipeTypeRegistry.ITEM_MELTER_RECIPE_TYPE.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.info == null) {
            List<Optional<Ingredient>> ingredients = new ArrayList<>();
            ingredients.add(Optional.of(input));

            // Create placement info
            this.info = PlacementInfo.createFromOptionals(ingredients);
        }
        return info;
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return null;
    }
}
