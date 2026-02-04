package com.leetftw.complexpipes.common.items;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class ItemRegistry {
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);



    // Creates a new food item with the id "examplemod:example_id", nutrition 1 and saturation 2
    /*public static final DeferredItem<Item> STACK_UPGRADE_ITEM = ITEMS.registerItem("stack_upgrade", properties ->
            new PipeUpgradeItem(properties.component(ItemComponentRegistry.PIPE_UPGRADE, StackPipeUpgrade.INSTANCE)));*/

    public static final DeferredItem<Item> DEBUG_ITEM = ITEMS.registerItem("pipe_upgrade_debug", PipeUpgradeItem::new);

    public static final DeferredItem<Item> EXTRACTION_CARD = ITEMS.registerItem("extraction_card", PipeCardItem::new);
    public static final DeferredItem<Item> INSERTION_CARD = ITEMS.registerItem("insertion_card", PipeCardItem::new);
    public static final DeferredItem<Item> ROUND_ROBIN_ROUTER = ITEMS.registerItem("round_robin_router", PipeCardItem::new);
    public static final DeferredItem<Item> RATIO_ROUTER = ITEMS.registerItem("ratio_router", PipeCardItem::new);
}
