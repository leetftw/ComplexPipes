package com.leetftw.complexpipes.common.pipe.types;

import com.leetftw.complexpipes.common.pipe.upgrade.BuiltinPipeUpgrades;
import com.leetftw.complexpipes.common.pipe.upgrade.PipeUpgradeType;
import com.leetftw.complexpipes.common.util.PipeHandlerWrapper;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static com.leetftw.complexpipes.common.PipeMod.MODID;

public class BuiltinPipeTypes {
    static final PipeType<ResourceHandler<ItemResource>> ITEM_PIPE = new PipeType<>() {
        private PipeHandlerWrapper<ResourceHandler<ItemResource>> wrapper = null;
        private final List<PipeUpgradeType> supportedUpgrades = List.of(
                BuiltinPipeUpgrades.SPEED_UPGRADE,
                BuiltinPipeUpgrades.STACK_UPGRADE
        );

        @Override
        public BlockCapability<ResourceHandler<ItemResource>, Direction> getBlockCapability() {
            return Capabilities.Item.BLOCK;
        }

        @Override
        public PipeHandlerWrapper<ResourceHandler<ItemResource>> getHandlerWrapper() {
            if (wrapper == null) {
                wrapper = new PipeHandlerWrapper<>() {
                    @Override
                    public int move(ResourceHandler<ItemResource> from, ResourceHandler<ItemResource> to, int amount, Predicate<Object> filter, TransactionContext transaction) {
                        return ResourceHandlerUtil.move(from, to, filter::test, amount, transaction);
                    }
                };
            }
            return wrapper;
        }

        @Override
        public int getDefaultTransferAmount() {
            // TODO: Move this to config
            return 4;
        }

        @Override
        public int getDefaultTransferSpeed() {
            return 20;
        }

        @Override
        public Identifier getTexturePath() {
            return Identifier.fromNamespaceAndPath(MODID, "item_pipe_frame");
        }

        @Override
        public boolean supportsUpgrade(PipeUpgradeType upgradeType) {
            return supportedUpgrades.contains(upgradeType);
        }
    };

    static final PipeType<ResourceHandler<FluidResource>> FLUID_PIPE = new PipeType<>() {
        private PipeHandlerWrapper<ResourceHandler<FluidResource>> wrapper = null;
        private final List<PipeUpgradeType> supportedUpgrades = List.of(
                BuiltinPipeUpgrades.SPEED_UPGRADE,
                BuiltinPipeUpgrades.STACK_UPGRADE
        );

        @Override
        public BlockCapability<ResourceHandler<FluidResource>, Direction> getBlockCapability() {
            return Capabilities.Fluid.BLOCK;
        }

        @Override
        public PipeHandlerWrapper<ResourceHandler<FluidResource>> getHandlerWrapper() {
            if (wrapper == null) {
                wrapper = new PipeHandlerWrapper<>() {
                    @Override
                    public int move(ResourceHandler<FluidResource> from, ResourceHandler<FluidResource> to, int amount, Predicate<Object> filter, TransactionContext transaction) {
                        return ResourceHandlerUtil.move(from, to, filter::test, amount, transaction);
                    }
                };
            }
            return wrapper;
        }

        @Override
        public int getDefaultTransferAmount() {
            // TODO: Move this to config
            return 250;
        }

        @Override
        public int getDefaultTransferSpeed() {
            return 20;
        }

        @Override
        public Identifier getTexturePath() {
            return Identifier.fromNamespaceAndPath(MODID, "fluid_pipe_frame");
        }

        @Override
        public boolean supportsUpgrade(PipeUpgradeType upgradeType) {
            return supportedUpgrades.contains(upgradeType);
        }
    };

    static final PipeType<EnergyHandler> ENERGY_PIPE = new PipeType<>() {
        private PipeHandlerWrapper<EnergyHandler> wrapper = null;
        private final List<PipeUpgradeType> supportedUpgrades = List.of(
                BuiltinPipeUpgrades.ENERGY_UPGRADE
        );

        @Override
        public BlockCapability<EnergyHandler, Direction> getBlockCapability() {
            return Capabilities.Energy.BLOCK;
        }

        @Override
        public PipeHandlerWrapper<EnergyHandler> getHandlerWrapper() {
            if (wrapper == null) {
                wrapper = new PipeHandlerWrapper<>() {
                    @Override
                    public int move(EnergyHandler from, EnergyHandler to, int amount, Predicate<Object> filter, TransactionContext transaction) {
                        return EnergyHandlerUtil.move(from, to, amount, transaction);
                    }
                };
            }
            return wrapper;
        }

        @Override
        public int getDefaultTransferAmount() {
            // TODO: Move this to config
            return 128;
        }

        @Override
        public int getDefaultTransferSpeed() {
            return 1;
        }

        @Override
        public Identifier getTexturePath() {
            return Identifier.fromNamespaceAndPath(MODID, "energy_pipe_frame");
        }

        @Override
        public boolean supportsUpgrade(PipeUpgradeType upgradeType) {
            return supportedUpgrades.contains(upgradeType);
        }
    };

    public static void registerTypes() {
        PipeTypeRegistry.registerType("item", ITEM_PIPE);
        PipeTypeRegistry.registerType("fluid", FLUID_PIPE);
        PipeTypeRegistry.registerType("energy", ENERGY_PIPE);
    }
}
