package com.leetftw.complexpipes.common.block_entities;

import com.leetftw.complexpipes.common.blocks.BlockRegistry;
import com.leetftw.complexpipes.common.crafting.ItemMelterRecipe;
import com.leetftw.complexpipes.common.crafting.ItemMelterRecipeInput;
import com.leetftw.complexpipes.common.crafting.RecipeTypeRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class ItemMelterBlockEntity extends BlockEntity {
    RecipeManager.CachedCheck<ItemMelterRecipeInput, ItemMelterRecipe> checker = RecipeManager.createCheck(RecipeTypeRegistry.ITEM_MELTER_RECIPE_TYPE.get());

    private ItemStacksResourceHandler itemHandler;
    private FluidStacksResourceHandler fluidHandler;
    int craftTime = 0;
    boolean crafting = false;

    public ItemMelterBlockEntity(BlockPos pos, BlockState blockState) {
        super(BlockEntityRegistry.ITEM_MELTER_BLOCK_ENTITY.get(), pos, blockState);
    }

    public ItemStacksResourceHandler getItemHandler(@Nullable Direction direction) {
        if (itemHandler == null) {
            itemHandler = new ItemStacksResourceHandler(1) {
                @Override
                public boolean isValid(int index, ItemResource resource) {
                    if (!hasLevel()) return false;

                    Level level = getLevel();
                    if (!(level instanceof ServerLevel serverLevel)) return false;

                    Optional<?> recipe = checker.getRecipeFor(new ItemMelterRecipeInput(resource.toStack()), serverLevel);
                    if (recipe.isEmpty()) return false;

                    return super.isValid(index, resource);
                }

                @Override
                public int extract(ItemResource resource, int amount, TransactionContext transaction) {
                    return crafting ? super.extract(resource, amount, transaction) : 0;
                }

                @Override
                public int extract(int index, ItemResource resource, int amount, TransactionContext transaction) {
                    return crafting ? super.extract(index, resource, amount, transaction) : 0;
                }
            };
        }
        return itemHandler;
    }

    public FluidStacksResourceHandler getFluidHandler(@Nullable Direction direction) {
        if (fluidHandler == null) {
            fluidHandler = new FluidStacksResourceHandler(1, 4000) {
                @Override
                public int insert(FluidResource resource, int amount, TransactionContext transaction) {
                    return crafting ? super.insert(resource, amount, transaction) : 0;
                }

                @Override
                public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
                    return crafting ? super.insert(index, resource, amount, transaction) : 0;
                }
            };
        }
        return fluidHandler;
    }

    @Override
    protected void saveAdditional(@NonNull ValueOutput output) {
        super.saveAdditional(output);
        itemHandler.serialize(output.child("items"));
        fluidHandler.serialize(output.child("fluids"));
        output.putInt("craftTime", craftTime);
    }

    @Override
    protected void loadAdditional(@NonNull ValueInput input) {
        super.loadAdditional(input);
        if (input.child("items").isPresent())
            getItemHandler(null).deserialize(input.child("items").get());

        if (input.child("fluids").isPresent())
            getFluidHandler(null).deserialize(input.child("fluids").get());

        craftTime = input.getIntOr("craftTime", 0);
    }

    public void tick(ServerLevel serverLevel, BlockPos pos, BlockState state1) {
        ItemStacksResourceHandler handler = getItemHandler(null);
        ItemResource storedResource = handler.getResource(0);

        ItemMelterRecipeInput input = new ItemMelterRecipeInput(storedResource.toStack());
        Optional<RecipeHolder<ItemMelterRecipe>> recipe = checker.getRecipeFor(input, serverLevel);

        if (recipe.isEmpty()) {
            craftTime = 0;
            return;
        }

        if (++craftTime != 100) return;

        FluidStacksResourceHandler fluidHandler = getFluidHandler(null);
        try (Transaction craft = Transaction.openRoot()) {
            crafting = true;
            FluidStack stackToCraft = recipe.get().value().getOutputStack();
            int insertedFluid = fluidHandler.insert(0, FluidResource.of(stackToCraft), stackToCraft.getAmount(), craft);
            int extractedItems = handler.extract(0, storedResource, 1, craft);
            if (insertedFluid == 250 && extractedItems == 1) {
                craftTime = 0;
                craft.commit();
            } else {
                craftTime--;
            }
            crafting = false;
        }
    }
}
