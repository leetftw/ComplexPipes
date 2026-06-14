package com.leetftw.complexpipes.client.datagen.providers;

import com.leetftw.complexpipes.common.blocks.BlockRegistry;
import com.leetftw.complexpipes.common.blocks.PipeBlock;
import com.leetftw.complexpipes.common.fluids.FluidRegistry;
import com.leetftw.complexpipes.common.items.ItemRegistry;
import com.leetftw.complexpipes.common.pipe.types.BuiltinPipeTypes;
import com.leetftw.complexpipes.common.pipe.types.PipeType;
import com.leetftw.complexpipes.common.pipe.types.PipeTypeRegistry;
import com.leetftw.complexpipes.common.cards.PipeCardRegistry;
import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.*;
import net.minecraft.client.data.models.blockstates.*;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.VariantMutator;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class BlockModelProvider extends ModelProvider
{
    public BlockModelProvider(PackOutput output)
    {
        super(output, MODID);
    }

    private static final TextureSlot FRAME_SLOT = TextureSlot.create("frame");
    private static final TextureSlot CORE_SLOT = TextureSlot.create("core");

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels)
    {
        new PipeBlockModelGenerator(blockModels.blockStateOutput, itemModels.itemModelOutput, blockModels.modelOutput).run();
        new PipeItemModelGenerator(itemModels.itemModelOutput, itemModels.modelOutput).run();
    }

    private class PipeBlockModelGenerator extends BlockModelGenerators
    {
        public PipeBlockModelGenerator(Consumer<BlockModelDefinitionGenerator> blockStateOutput, ItemModelOutput itemModelOutput, BiConsumer<Identifier, ModelInstance> modelOutput)
        {
            super(blockStateOutput, itemModelOutput, modelOutput);
        }

        private void createPipeFrame(Block block, Identifier texture) {
            Identifier quarryFrameBase = Identifier.fromNamespaceAndPath(MODID, "block/pipe_frame_base");
            Identifier quarryFrameExtension = Identifier.fromNamespaceAndPath(MODID, "block/pipe_frame_extension");

            TextureMapping mapping = new TextureMapping().put(FRAME_SLOT, new Material(texture.withPrefix("block/"), true));

            // Create block models
            quarryFrameBase = ExtendedModelTemplateBuilder.builder()
                    .requiredTextureSlot(FRAME_SLOT)
                    .parent(quarryFrameBase)
                    .suffix("_base")
                    .build().create(block, mapping, modelOutput);
            quarryFrameExtension = ExtendedModelTemplateBuilder.builder()
                    .requiredTextureSlot(FRAME_SLOT)
                    .parent(quarryFrameExtension)
                    .suffix("_extension")
                    .build().create(block, mapping, modelOutput);

            // Create block model definition
            Variant baseVariant = new Variant(quarryFrameBase);
            Variant extensionVariant = new Variant(quarryFrameExtension);

            MultiPartGenerator generator = MultiPartGenerator.multiPart(block)
                    .with(BlockModelGenerators.variant(baseVariant))
                    .with(BlockModelGenerators.condition().term(PipeBlock.NORTH_CON, true),
                            BlockModelGenerators.variant(extensionVariant))
                    .with(BlockModelGenerators.condition().term(PipeBlock.EAST_CON, true),
                            BlockModelGenerators.variant(extensionVariant)
                                    .with(VariantMutator.Y_ROT.withValue(Quadrant.R90)))
                    .with(BlockModelGenerators.condition().term(PipeBlock.SOUTH_CON, true),
                            BlockModelGenerators.variant(extensionVariant)
                                    .with(VariantMutator.Y_ROT.withValue(Quadrant.R180)))
                    .with(BlockModelGenerators.condition().term(PipeBlock.WEST_CON, true),
                            BlockModelGenerators.variant(extensionVariant)
                                    .with(VariantMutator.Y_ROT.withValue(Quadrant.R270)))
                    .with(BlockModelGenerators.condition().term(PipeBlock.UP_CON, true),
                            BlockModelGenerators.variant(extensionVariant)
                                    .with(VariantMutator.X_ROT.withValue(Quadrant.R270)))
                    .with(BlockModelGenerators.condition().term(PipeBlock.DOWN_CON, true),
                            BlockModelGenerators.variant(extensionVariant)
                                    .with(VariantMutator.X_ROT.withValue(Quadrant.R90)));

            itemModelOutput.register(block.asItem(), new ClientItem(ItemModelUtils.plainModel(quarryFrameBase), ClientItem.Properties.DEFAULT));
            blockStateOutput.accept(generator);
        }

        private void createFilledPipe(PipeType<?> pipeType)
        {
            Identifier quarryFrameBase = Identifier.fromNamespaceAndPath(MODID, "block/pipe_base");
            Identifier quarryFrameExtension = Identifier.fromNamespaceAndPath(MODID, "block/pipe_extension");

            TextureMapping mapping = new TextureMapping().put(FRAME_SLOT, new Material(pipeType.getFrameTexturePath().withPrefix("block/"), true))
                    .put(CORE_SLOT, new Material(pipeType.getCoreTexturePath().withPrefix("block/"), true));

            // Create block models
            quarryFrameBase = ExtendedModelTemplateBuilder.builder()
                    .requiredTextureSlot(FRAME_SLOT)
                    .requiredTextureSlot(CORE_SLOT)
                    .parent(quarryFrameBase)
                    .suffix("_base")
                    .build().create(pipeType.getBlock(), mapping, modelOutput);
            quarryFrameExtension = ExtendedModelTemplateBuilder.builder()
                    .requiredTextureSlot(FRAME_SLOT)
                    .requiredTextureSlot(CORE_SLOT)
                    .parent(quarryFrameExtension)
                    .suffix("_extension")
                    .build().create(pipeType.getBlock(), mapping, modelOutput);

            // Create block model definition
            Variant baseVariant = new Variant(quarryFrameBase);
            Variant extensionVariant = new Variant(quarryFrameExtension);

            MultiPartGenerator generator = MultiPartGenerator.multiPart(pipeType.getBlock())
                    .with(BlockModelGenerators.variant(baseVariant))
                    .with(BlockModelGenerators.condition().term(PipeBlock.NORTH_CON, true),
                            BlockModelGenerators.variant(extensionVariant))
                    .with(BlockModelGenerators.condition().term(PipeBlock.EAST_CON, true),
                            BlockModelGenerators.variant(extensionVariant)
                                    .with(VariantMutator.Y_ROT.withValue(Quadrant.R90)))
                    .with(BlockModelGenerators.condition().term(PipeBlock.SOUTH_CON, true),
                            BlockModelGenerators.variant(extensionVariant)
                                    .with(VariantMutator.Y_ROT.withValue(Quadrant.R180)))
                    .with(BlockModelGenerators.condition().term(PipeBlock.WEST_CON, true),
                            BlockModelGenerators.variant(extensionVariant)
                                    .with(VariantMutator.Y_ROT.withValue(Quadrant.R270)))
                    .with(BlockModelGenerators.condition().term(PipeBlock.UP_CON, true),
                            BlockModelGenerators.variant(extensionVariant)
                                    .with(VariantMutator.X_ROT.withValue(Quadrant.R270)))
                    .with(BlockModelGenerators.condition().term(PipeBlock.DOWN_CON, true),
                            BlockModelGenerators.variant(extensionVariant)
                                    .with(VariantMutator.X_ROT.withValue(Quadrant.R90)));

            itemModelOutput.register(pipeType.getBlock().asItem(), new ClientItem(ItemModelUtils.plainModel(quarryFrameBase), ClientItem.Properties.DEFAULT));
            blockStateOutput.accept(generator);
        }

        @Override
        public void run()
        {
            createTrivialCube(BlockRegistry.EXAMPLE_BLOCK.get());
            createTrivialCube(BlockRegistry.ITEM_MELTER.get());


            createNonTemplateModelBlock(FluidRegistry.LIQUID_ENDER.getLegacyBlock(), Blocks.WATER);
            createNonTemplateModelBlock(FluidRegistry.LIQUID_REDSTONE.getLegacyBlock(), Blocks.WATER);
            createNonTemplateModelBlock(FluidRegistry.LIQUID_GLASS.getLegacyBlock(), Blocks.WATER);
            createNonTemplateModelBlock(FluidRegistry.ENDER_REDSTONE_ALLOY.getLegacyBlock(), Blocks.WATER);
            createNonTemplateModelBlock(FluidRegistry.ENDER_GLASS_ALLOY.getLegacyBlock(), Blocks.WATER);

            createPipeFrame(BlockRegistry.BASIC_PIPE_FRAME.get(), BuiltinPipeTypes.BASE_FRAME_TEXTURE);
            createPipeFrame(BlockRegistry.ENHANCED_PIPE_FRAME.get(), BuiltinPipeTypes.ENHANCED_FRAME_TEXTURE);
            createPipeFrame(BlockRegistry.ADVANCED_PIPE_FRAME.get(), BuiltinPipeTypes.ADVANCED_FRAME_TEXTURE);
            createPipeFrame(BlockRegistry.EXTREME_PIPE_FRAME.get(), BuiltinPipeTypes.EXTREME_FRAME_TEXTURE);

            PipeTypeRegistry.forEach(this::createFilledPipe);
        }
    }

    public class PipeItemModelGenerator extends ItemModelGenerators
    {
        public PipeItemModelGenerator(ItemModelOutput itemModelOutput, BiConsumer<Identifier, ModelInstance> modelOutput)
        {
            super(itemModelOutput, modelOutput);
        }

        @Override
        public void run()
        {
            for (var upgrade : PipeCardRegistry.PIPE_CARD_TYPE_REGISTRY.entrySet())
                generateFlatItem(upgrade.getValue().getItem(), ModelTemplates.FLAT_ITEM);

            generateFlatItem(ItemRegistry.DEBUG_ITEM.get(), ModelTemplates.FLAT_ITEM);
            generateFlatItem(ItemRegistry.EXTRACTION_CARD.get(), ModelTemplates.FLAT_ITEM);
            generateFlatItem(ItemRegistry.INSERTION_CARD.get(), ModelTemplates.FLAT_ITEM);

            generateFlatItem(FluidRegistry.LIQUID_ENDER.getBucket(), ModelTemplates.FLAT_ITEM);
            generateFlatItem(FluidRegistry.LIQUID_REDSTONE.getBucket(), ModelTemplates.FLAT_ITEM);
            generateFlatItem(FluidRegistry.LIQUID_GLASS.getBucket(), ModelTemplates.FLAT_ITEM);
            generateFlatItem(FluidRegistry.ENDER_REDSTONE_ALLOY.getBucket(), ModelTemplates.FLAT_ITEM);
            generateFlatItem(FluidRegistry.ENDER_GLASS_ALLOY.getBucket(), ModelTemplates.FLAT_ITEM);
        }
    }
}