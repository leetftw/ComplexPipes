package com.leetftw.complexpipes.common.crafting;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class RecipeTypeRegistry {
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, MODID);

    public static final Supplier<RecipeType<ItemMelterRecipe>> ITEM_MELTER_RECIPE_TYPE =
            RECIPE_TYPES.register("item_melter", RecipeType::simple);

    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);

    public static final Supplier<RecipeSerializer<ItemMelterRecipe>> ITEM_MELTER_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("item_melter", ItemMelterRecipeSerializerCodecs::create);

    public static void register(IEventBus modEventBus) {
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);
    }
}
