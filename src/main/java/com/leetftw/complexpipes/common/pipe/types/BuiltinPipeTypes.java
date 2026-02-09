package com.leetftw.complexpipes.common.pipe.types;

import com.leetftw.complexpipes.common.ServerConfig;
import com.leetftw.complexpipes.common.cards.BuiltinPipeCards;
import com.leetftw.complexpipes.common.cards.PipeCard;
import com.leetftw.complexpipes.common.cards.PipeCardType;
import com.leetftw.complexpipes.common.cards.builtin.RouterPipeCard;
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

import java.util.List;
import java.util.function.Predicate;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class BuiltinPipeTypes {
    private interface TriFunction<A, B, C, R> {
        R apply(A a, B b, C c);
    }

    private static final TriFunction<Integer, Predicate<PipeCard>, Identifier, PipeType<ResourceHandler<ItemResource>>> ITEM_PIPE = (maxCards, supportsCard, frameTexture) -> new PipeType<>() {
        private PipeHandlerWrapper<ResourceHandler<ItemResource>> wrapper = null;

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

                    @Override
                    public int getCount(ResourceHandler<ItemResource> handler, Predicate<Object> filter) {
                        int totalCount = 0;
                        for (int i = 0; i < handler.size(); i++) {
                            if (filter.test(handler.getResource(i))) {
                                totalCount += handler.getAmountAsInt(i);
                            }
                        }
                        return totalCount;
                    }
                };
            }
            return wrapper;
        }

        @Override
        public int getDefaultTransferAmount() {
            return ServerConfig.ITEM_PIPE_TRANSFER.get();
        }

        @Override
        public int getDefaultTransferSpeed() {
            return 20;
        }

        @Override
        public Identifier getFrameTexturePath() {
            return frameTexture;
        }

        @Override
        public Identifier getCoreTexturePath() {
            return Identifier.fromNamespaceAndPath(MODID, "item_pipe_core");
        }

        @Override
        public int getMaxCards() {
            return maxCards;
        }

        @Override
        public boolean supportsCard(PipeCard upgrade) {
            return supportsCard.test(upgrade);
        }
    };

    private static final TriFunction<Integer, Predicate<PipeCard>, Identifier, PipeType<ResourceHandler<FluidResource>>> FLUID_PIPE = (maxCards, supportsCard, frameTexture) -> new PipeType<>() {
        private PipeHandlerWrapper<ResourceHandler<FluidResource>> wrapper = null;

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

                    @Override
                    public int getCount(ResourceHandler<FluidResource> handler, Predicate<Object> filter) {
                        int totalCount = 0;
                        for (int i = 0; i < handler.size(); i++) {
                            if (filter.test(handler.getResource(i))) {
                                totalCount += handler.getAmountAsInt(i);
                            }
                        }
                        return totalCount;
                    }
                };
            }
            return wrapper;
        }

        @Override
        public int getDefaultTransferAmount() {
            return ServerConfig.FLUID_PIPE_TRANSFER.get();
        }

        @Override
        public int getDefaultTransferSpeed() {
            return 20;
        }

        @Override
        public Identifier getFrameTexturePath() {
            return frameTexture;
        }

        @Override
        public Identifier getCoreTexturePath() {
            return Identifier.fromNamespaceAndPath(MODID, "fluid_pipe_core");
        }

        @Override
        public int getMaxCards() {
            return maxCards;
        }

        @Override
        public boolean supportsCard(PipeCard upgrade) {
            return supportsCard.test(upgrade);
        }
    };

    static final TriFunction<Integer, Predicate<PipeCard>, Identifier, PipeType<EnergyHandler>> ENERGY_PIPE = (maxCards, supportsCard, frameTexture) -> new PipeType<>() {
        private PipeHandlerWrapper<EnergyHandler> wrapper = null;

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

                    @Override
                    public int getCount(EnergyHandler handler, Predicate<Object> filter) {
                        return handler.getAmountAsInt();
                    }
                };
            }
            return wrapper;
        }

        @Override
        public int getDefaultTransferAmount() {
            return ServerConfig.ENERGY_PIPE_TRANSFER.get();
        }

        @Override
        public int getDefaultTransferSpeed() {
            return 1;
        }

        @Override
        public Identifier getFrameTexturePath() {
            return frameTexture;
        }

        @Override
        public Identifier getCoreTexturePath() {
            return Identifier.fromNamespaceAndPath(MODID, "energy_pipe_core");
        }

        @Override
        public int getMaxCards() {
            return maxCards;
        }

        @Override
        public boolean supportsCard(PipeCard upgrade) {
            return supportsCard.test(upgrade);
        }
    };

    private static final List<PipeCardType> ITEM_PIPE_SUPPORTED_CARDS = List.of(
            BuiltinPipeCards.SPEED_UPGRADE,
            BuiltinPipeCards.STACK_UPGRADE,
            BuiltinPipeCards.ITEM_STACK_FILTER
    );

    private static final List<PipeCardType> FLUID_PIPE_SUPPORTED_CARDS = List.of(
            BuiltinPipeCards.SPEED_UPGRADE,
            BuiltinPipeCards.STACK_UPGRADE
    );

    private static final List<PipeCardType> ENERGY_PIPE_SUPPORTED_CARDS = List.of(
            BuiltinPipeCards.ENERGY_UPGRADE
    );

    private static final Identifier BASE_FRAME_TEXTURE = Identifier.fromNamespaceAndPath(MODID, "basic_pipe_frame");
    private static final Identifier ENHANCED_FRAME_TEXTURE = Identifier.fromNamespaceAndPath(MODID, "enhanced_pipe_frame");
    private static final Identifier ADVANCED_FRAME_TEXTURE = Identifier.fromNamespaceAndPath(MODID, "advanced_pipe_frame");
    private static final Identifier EXTREME_FRAME_TEXTURE = Identifier.fromNamespaceAndPath(MODID, "extreme_pipe_frame");

    private static final Predicate<PipeCard> BASIC_PIPE_SUPPORTS = PipeCard::isFilter;
    private static final Predicate<PipeCard> ITEM_PIPE_SUPPORTS = pipeCard -> ITEM_PIPE_SUPPORTED_CARDS.contains(pipeCard.getType()) || pipeCard instanceof RouterPipeCard;
    private static final Predicate<PipeCard> FLUID_PIPE_SUPPORTS = pipeCard -> FLUID_PIPE_SUPPORTED_CARDS.contains(pipeCard.getType()) || pipeCard instanceof RouterPipeCard;
    private static final Predicate<PipeCard> ENERGY_PIPE_SUPPORTS = pipeCard -> ENERGY_PIPE_SUPPORTED_CARDS.contains(pipeCard.getType()) || pipeCard instanceof RouterPipeCard;

    public static final PipeType<ResourceHandler<ItemResource>> BASIC_ITEM_PIPE = ITEM_PIPE.apply(1, ITEM_PIPE_SUPPORTS.and(BASIC_PIPE_SUPPORTS), BASE_FRAME_TEXTURE);
    public static final PipeType<ResourceHandler<ItemResource>> ENHANCED_ITEM_PIPE = ITEM_PIPE.apply(3, ITEM_PIPE_SUPPORTS, ENHANCED_FRAME_TEXTURE);
    public static final PipeType<ResourceHandler<ItemResource>> ADVANCED_ITEM_PIPE = ITEM_PIPE.apply(6, ITEM_PIPE_SUPPORTS, ADVANCED_FRAME_TEXTURE);
    public static final PipeType<ResourceHandler<ItemResource>> EXTREME_ITEM_PIPE = ITEM_PIPE.apply(12, ITEM_PIPE_SUPPORTS, EXTREME_FRAME_TEXTURE);

    public static final PipeType<ResourceHandler<FluidResource>> BASIC_FLUID_PIPE = FLUID_PIPE.apply(1, FLUID_PIPE_SUPPORTS.and(BASIC_PIPE_SUPPORTS), BASE_FRAME_TEXTURE);
    public static final PipeType<ResourceHandler<FluidResource>> ENHANCED_FLUID_PIPE = FLUID_PIPE.apply(3, FLUID_PIPE_SUPPORTS, ENHANCED_FRAME_TEXTURE);
    public static final PipeType<ResourceHandler<FluidResource>> ADVANCED_FLUID_PIPE = FLUID_PIPE.apply(6, FLUID_PIPE_SUPPORTS, ADVANCED_FRAME_TEXTURE);
    public static final PipeType<ResourceHandler<FluidResource>> EXTREME_FLUID_PIPE = FLUID_PIPE.apply(12, FLUID_PIPE_SUPPORTS, EXTREME_FRAME_TEXTURE);

    public static final PipeType<EnergyHandler> BASIC_ENERGY_PIPE = ENERGY_PIPE.apply(0, a -> false, BASE_FRAME_TEXTURE);
    public static final PipeType<EnergyHandler> ENHANCED_ENERGY_PIPE = ENERGY_PIPE.apply(3, ENERGY_PIPE_SUPPORTS, ENHANCED_FRAME_TEXTURE);
    public static final PipeType<EnergyHandler> ADVANCED_ENERGY_PIPE = ENERGY_PIPE.apply(6, ENERGY_PIPE_SUPPORTS, ADVANCED_FRAME_TEXTURE);
    public static final PipeType<EnergyHandler> EXTREME_ENERGY_PIPE = ENERGY_PIPE.apply(12, ENERGY_PIPE_SUPPORTS, EXTREME_FRAME_TEXTURE);

    public static void registerTypes() {
        PipeTypeRegistry.registerType("basic_item_pipe", BASIC_ITEM_PIPE);
        PipeTypeRegistry.registerType("enhanced_item_pipe", ENHANCED_ITEM_PIPE);
        PipeTypeRegistry.registerType("advanced_item_pipe", ADVANCED_ITEM_PIPE);
        PipeTypeRegistry.registerType("extreme_item_pipe", EXTREME_ITEM_PIPE);

        PipeTypeRegistry.registerType("basic_fluid_pipe", BASIC_FLUID_PIPE);
        PipeTypeRegistry.registerType("enhanced_fluid_pipe", ENHANCED_FLUID_PIPE);
        PipeTypeRegistry.registerType("advanced_fluid_pipe", ADVANCED_FLUID_PIPE);
        PipeTypeRegistry.registerType("extreme_fluid_pipe", EXTREME_FLUID_PIPE);

        PipeTypeRegistry.registerType("basic_energy_pipe", BASIC_ENERGY_PIPE);
        PipeTypeRegistry.registerType("enhanced_energy_pipe", ENHANCED_ENERGY_PIPE);
        PipeTypeRegistry.registerType("advanced_energy_pipe", ADVANCED_ENERGY_PIPE);
        PipeTypeRegistry.registerType("extreme_energy_pipe", EXTREME_ENERGY_PIPE);
    }
}
