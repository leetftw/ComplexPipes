package com.leetftw.complexpipes.common.crafting;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

public record ItemMelterRecipeInput(ItemStack input) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        if (index != 0) throw new IndexOutOfBoundsException();
        return input;
    }

    @Override
    public int size() {
        return 1;
    }
}
