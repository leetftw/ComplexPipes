package com.leetftw.complexpipes.common.pipe.upgrades;

import com.leetftw.complexpipes.common.gui.ItemStackFilterMenu;
import com.leetftw.complexpipes.common.items.ItemComponentRegistry;
import com.leetftw.complexpipes.common.items.PipeUpgradeItem;
import com.leetftw.complexpipes.common.pipe.upgrades.builtin.EnergyPipeUpgrade;
import com.leetftw.complexpipes.common.pipe.upgrades.builtin.ItemStackPipeFilter;
import com.leetftw.complexpipes.common.pipe.upgrades.builtin.SpeedPipeUpgrade;
import com.leetftw.complexpipes.common.pipe.upgrades.builtin.StackPipeUpgrade;
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

public class BuiltinPipeUpgrades {
    public static final PipeUpgradeType STACK_UPGRADE = new PipeUpgradeType(StackPipeUpgrade.CODEC, StackPipeUpgrade::new);
    private static final Identifier STACK_UPGRADE_ID = Identifier.fromNamespaceAndPath(MODID, "stack_upgrade");

    public static final PipeUpgradeType SPEED_UPGRADE = new PipeUpgradeType(SpeedPipeUpgrade.CODEC, SpeedPipeUpgrade::new);
    private static final Identifier SPEED_UPGRADE_ID = Identifier.fromNamespaceAndPath(MODID, "speed_upgrade");

    public static final PipeUpgradeType ENERGY_UPGRADE = new PipeUpgradeType(EnergyPipeUpgrade.CODEC, EnergyPipeUpgrade::new);
    private static final Identifier ENERGY_UPGRADE_ID = Identifier.fromNamespaceAndPath(MODID, "energy_upgrade");

    public static final PipeUpgradeType ITEM_STACK_FILTER = new PipeUpgradeType(ItemStackPipeFilter.CODEC, ItemStackPipeFilter::new);
    private static final Identifier ITEM_STACK_FILTER_ID = Identifier.fromNamespaceAndPath(MODID, "item_filter");

    public static void registerUpgrades(RegisterEvent.RegisterHelper<PipeUpgradeType> register) {
        register.register(STACK_UPGRADE_ID, STACK_UPGRADE);
        register.register(SPEED_UPGRADE_ID, SPEED_UPGRADE);
        register.register(ENERGY_UPGRADE_ID, ENERGY_UPGRADE);
        register.register(ITEM_STACK_FILTER_ID, ITEM_STACK_FILTER);
    }

    private static void registerItem(RegisterEvent.RegisterHelper<Item> register, Identifier id, PipeUpgradeType upgrade) {
        ResourceKey<Item> itemId = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
        PipeUpgradeItem item = new PipeUpgradeItem(new Item.Properties().setId(itemId).component(ItemComponentRegistry.PIPE_UPGRADE, upgrade.instantiate()));
        register.register(itemId.identifier(), item);
        upgrade.receiveItem(item);
    }

    private static void registerItemWithMenu(RegisterEvent.RegisterHelper<Item> register, Identifier id, PipeUpgradeType upgrade, SimpleMenuProvider provider) {
        ResourceKey<Item> itemId = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
        PipeUpgradeItem item = new PipeUpgradeItem(new Item.Properties().setId(itemId).component(ItemComponentRegistry.PIPE_UPGRADE, upgrade.instantiate())) {
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

    public static void registerUpgradeItems(RegisterEvent.RegisterHelper<Item> register) {
        registerItem(register, STACK_UPGRADE_ID, STACK_UPGRADE);
        registerItem(register, SPEED_UPGRADE_ID, SPEED_UPGRADE);
        registerItem(register, ENERGY_UPGRADE_ID, ENERGY_UPGRADE);
        registerItemWithMenu(register, ITEM_STACK_FILTER_ID, ITEM_STACK_FILTER,
                new SimpleMenuProvider((containerId, playerInventory, player) -> new ItemStackFilterMenu(containerId, playerInventory, player.getInventory().getSelectedItem()),
                        Component.literal("Item Filter")));
    }
}

