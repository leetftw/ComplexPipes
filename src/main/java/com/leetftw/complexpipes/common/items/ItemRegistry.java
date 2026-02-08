package com.leetftw.complexpipes.common.items;

import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class ItemRegistry {
    // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);

    public static final DeferredItem<Item> DEBUG_ITEM = ITEMS.registerItem("pipe_card_debug", PipeCardItem::new);

    public static final DeferredItem<Item> EXTRACTION_CARD = ITEMS.registerItem("extraction_card", PipeCardItem::new);
    public static final DeferredItem<Item> INSERTION_CARD = ITEMS.registerItem("insertion_card", PipeCardItem::new);
    /*public static final DeferredItem<Item> ROUND_ROBIN_ROUTER = ITEMS.registerItem("round_robin_router", PipeCardItem::new);
    public static final DeferredItem<Item> RATIO_ROUTER = ITEMS.registerItem("ratio_router", PipeCardItem::new);*/
}
