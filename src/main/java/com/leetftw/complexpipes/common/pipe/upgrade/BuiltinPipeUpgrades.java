package com.leetftw.complexpipes.common.pipe.upgrade;

import com.leetftw.complexpipes.common.items.ItemComponentRegistry;
import com.leetftw.complexpipes.common.items.PipeUpgradeItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.RegisterEvent;

import static com.leetftw.complexpipes.common.PipeMod.MODID;

public class BuiltinPipeUpgrades {
    public static final PipeUpgradeType STACK_UPGRADE = new PipeUpgradeType(StackPipeUpgrade.CODEC, StackPipeUpgrade::new);
    private static final Identifier STACK_UPGRADE_ID = Identifier.fromNamespaceAndPath(MODID, "stack_upgrade");

    public static final PipeUpgradeType SPEED_UPGRADE = new PipeUpgradeType(SpeedPipeUpgrade.CODEC, SpeedPipeUpgrade::new);
    private static final Identifier SPEED_UPGRADE_ID = Identifier.fromNamespaceAndPath(MODID, "speed_upgrade");

    public static final PipeUpgradeType ENERGY_UPGRADE = new PipeUpgradeType(EnergyPipeUpgrade.CODEC, EnergyPipeUpgrade::new);
    private static final Identifier ENERGY_UPGRADE_ID = Identifier.fromNamespaceAndPath(MODID, "energy_upgrade");

    public static void registerUpgrades(RegisterEvent.RegisterHelper<PipeUpgradeType> register) {
        register.register(STACK_UPGRADE_ID, STACK_UPGRADE);
        register.register(SPEED_UPGRADE_ID, SPEED_UPGRADE);
        register.register(ENERGY_UPGRADE_ID, ENERGY_UPGRADE);
    }

    private static void registerItemHelper(RegisterEvent.RegisterHelper<Item> register, Identifier id, PipeUpgradeType upgrade) {
        ResourceKey<Item> itemId = ResourceKey.create(BuiltInRegistries.ITEM.key(), id);
        PipeUpgradeItem item = new PipeUpgradeItem(new Item.Properties().setId(itemId).component(ItemComponentRegistry.PIPE_UPGRADE, upgrade.instantiate()));
        register.register(itemId.identifier(), item);
        upgrade.receiveItem(item);
    }

    public static void registerUpgradeItems(RegisterEvent.RegisterHelper<Item> register) {
        registerItemHelper(register, STACK_UPGRADE_ID, STACK_UPGRADE);
        registerItemHelper(register, SPEED_UPGRADE_ID, SPEED_UPGRADE);
        registerItemHelper(register, ENERGY_UPGRADE_ID, ENERGY_UPGRADE);
    }
}

