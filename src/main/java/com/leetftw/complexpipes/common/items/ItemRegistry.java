package com.leetftw.complexpipes.common.items;

import com.leetftw.complexpipes.common.pipe.upgrade.StackPipeUpgrade;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.leetftw.complexpipes.common.PipeMod.MODID;

public class ItemRegistry {
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);



    // Creates a new food item with the id "examplemod:example_id", nutrition 1 and saturation 2
    /*public static final DeferredItem<Item> STACK_UPGRADE_ITEM = ITEMS.registerItem("stack_upgrade", properties ->
            new PipeUpgradeItem(properties.component(ItemComponentRegistry.PIPE_UPGRADE, StackPipeUpgrade.INSTANCE)));*/

    public static final DeferredItem<Item> DEBUG_ITEM = ITEMS.registerItem("pipe_upgrade_debug", PipeUpgradeItem::new);
}
