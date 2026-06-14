package com.leetftw.complexpipes.common.crafting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

public class ItemMelterRecipeSerializerCodecs {
    public static final MapCodec<ItemMelterRecipe> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Ingredient.CODEC.fieldOf("ingredient").forGetter(ItemMelterRecipe::getInput),
                    BuiltInRegistries.FLUID.byNameCodec().fieldOf("output").forGetter(ItemMelterRecipe::getOutput),
                    Codec.INT.fieldOf("output_amount").forGetter(ItemMelterRecipe::getOutputAmount)
            ).apply(instance, ItemMelterRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ItemMelterRecipe> STREAM_CODEC =
            StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, ItemMelterRecipe::getInput,
                    ByteBufCodecs.registry(Registries.FLUID), ItemMelterRecipe::getOutput,
                    ByteBufCodecs.INT, ItemMelterRecipe::getOutputAmount,
                    ItemMelterRecipe::new
            );

    public static MapCodec<ItemMelterRecipe> codec() {
        return CODEC;
    }

    public static StreamCodec<RegistryFriendlyByteBuf, ItemMelterRecipe> streamCodec() {
        return STREAM_CODEC;
    }

    public static RecipeSerializer<ItemMelterRecipe> create() {
        return new RecipeSerializer<>(CODEC, STREAM_CODEC);
    }
}
