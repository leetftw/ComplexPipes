package com.leetftw.complexpipes.common.cards;

import com.leetftw.complexpipes.common.gui.ItemStackFilterMenu;
import com.leetftw.complexpipes.common.items.ItemComponentRegistry;
import com.leetftw.complexpipes.common.cards.builtin.upgrade.EnergyPipeUpgrade;
import com.leetftw.complexpipes.common.cards.builtin.ItemStackPipeFilter;
import com.leetftw.complexpipes.common.cards.builtin.upgrade.SpeedPipeUpgrade;
import com.leetftw.complexpipes.common.cards.builtin.upgrade.StackPipeUpgrade;
import com.leetftw.complexpipes.common.items.PipeCardItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.registries.RegisterEvent;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class BuiltinPipeCards {
    public static final PipeCardType STACK_UPGRADE = new PipeCardType(StackPipeUpgrade.CODEC, StackPipeUpgrade::new);
    private static final Identifier STACK_UPGRADE_ID = Identifier.fromNamespaceAndPath(MODID, "stack_upgrade");

    public static final PipeCardType SPEED_UPGRADE = new PipeCardType(SpeedPipeUpgrade.CODEC, SpeedPipeUpgrade::new);
    private static final Identifier SPEED_UPGRADE_ID = Identifier.fromNamespaceAndPath(MODID, "speed_upgrade");

    public static final PipeCardType ENERGY_UPGRADE = new PipeCardType(EnergyPipeUpgrade.CODEC, EnergyPipeUpgrade::new);
    private static final Identifier ENERGY_UPGRADE_ID = Identifier.fromNamespaceAndPath(MODID, "energy_upgrade");

    public static final PipeCardType ITEM_STACK_FILTER = new PipeCardType(ItemStackPipeFilter.CODEC, ItemStackPipeFilter::new);
    private static final Identifier ITEM_STACK_FILTER_ID = Identifier.fromNamespaceAndPath(MODID, "item_filter");

    private static void registerItem(RegisterEvent.RegisterHelper<Item> register, Identifier id, PipeCardType upgrade) {
        ResourceKey<Item> itemId = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
        PipeCardItem item = new PipeCardItem(new Item.Properties().setId(itemId).component(ItemComponentRegistry.PIPE_CARD_DATA, upgrade.instantiate()));
        register.register(itemId.identifier(), item);
        upgrade.receiveItem(item);
    }

    private static void registerItemWithMenu(RegisterEvent.RegisterHelper<Item> register, Identifier id, PipeCardType upgrade, SimpleMenuProvider provider) {
        ResourceKey<Item> itemId = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
        PipeCardItem item = new PipeCardItem(new Item.Properties().setId(itemId).component(ItemComponentRegistry.PIPE_CARD_DATA, upgrade.instantiate())) {
            @Override
            public InteractionResult use(Level level, Player player, InteractionHand hand) {
                if (hand != InteractionHand.MAIN_HAND)
                    return super.use(level, player, hand);

                ItemStack stack = player.getInventory().getItem(hand.asEquipmentSlot().getIndex());
                if (stack.isEmpty())
                    return super.use(level, player, hand);



                player.openMenu(provider);
                return super.use(level, player, hand);
            }
        };
        register.register(itemId.identifier(), item);
        upgrade.receiveItem(item);
    }

    public static void registerCards(RegisterEvent.RegisterHelper<PipeCardType> register) {
        register.register(STACK_UPGRADE_ID, STACK_UPGRADE);
        register.register(SPEED_UPGRADE_ID, SPEED_UPGRADE);
        register.register(ENERGY_UPGRADE_ID, ENERGY_UPGRADE);
        register.register(ITEM_STACK_FILTER_ID, ITEM_STACK_FILTER);
    }

    public static void registerUpgradeItems(RegisterEvent.RegisterHelper<Item> register) {
        registerItem(register, STACK_UPGRADE_ID, STACK_UPGRADE);
        registerItem(register, SPEED_UPGRADE_ID, SPEED_UPGRADE);
        registerItem(register, ENERGY_UPGRADE_ID, ENERGY_UPGRADE);
        registerItemWithMenu(register, ITEM_STACK_FILTER_ID, ITEM_STACK_FILTER,
                new SimpleMenuProvider((containerId, playerInventory, player) -> new ItemStackFilterMenu(containerId, playerInventory, player.getInventory().getSelectedItem()),
                        Component.literal("Item Filter")));
    }
}

