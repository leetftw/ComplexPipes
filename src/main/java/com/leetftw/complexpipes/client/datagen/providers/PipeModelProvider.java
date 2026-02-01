package com.leetftw.complexpipes.client.datagen.providers;

import com.leetftw.complexpipes.common.blocks.BlockRegistry;
import com.leetftw.complexpipes.common.blocks.PipeBlock;
import com.leetftw.complexpipes.common.items.ItemRegistry;
import com.leetftw.complexpipes.common.pipe.types.PipeType;
import com.leetftw.complexpipes.common.pipe.types.PipeTypeRegistry;
import com.leetftw.complexpipes.common.pipe.upgrades.PipeUpgradeRegistry;
import com.mojang.math.Quadrant;
import net.minecraft.client.data.models.*;
import net.minecraft.client.data.models.blockstates.*;
import net.minecraft.client.data.models.model.*;
import net.minecraft.client.renderer.block.model.Variant;
import net.minecraft.client.renderer.block.model.VariantMutator;
import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.leetftw.complexpipes.common.ComplexPipes.MODID;

public class PipeModelProvider extends ModelProvider
{
    public PipeModelProvider(PackOutput output)
    {
        super(output, MODID);
    }

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

        private void createPipeFrame(PipeType<?> pipeType)
        {
            Identifier quarryFrameBase = Identifier.fromNamespaceAndPath(MODID, "block/pipe_base");
            Identifier quarryFrameExtension = Identifier.fromNamespaceAndPath(MODID, "block/pipe_extension");

            // Create block models
            TextureSlot slot = TextureSlot.create("0");
            quarryFrameBase = ExtendedModelTemplateBuilder.builder()
                    .requiredTextureSlot(slot)
                    .parent(quarryFrameBase)
                    .suffix("_base")
                    .build().create(pipeType.getBlock(), new TextureMapping().put(slot, pipeType.getTexturePath().withPrefix("block/")), modelOutput);
            quarryFrameExtension = ExtendedModelTemplateBuilder.builder()
                    .requiredTextureSlot(slot)
                    .parent(quarryFrameExtension)
                    .suffix("_extension")
                    .build().create(pipeType.getBlock(), new TextureMapping().put(slot, pipeType.getTexturePath().withPrefix("block/")), modelOutput);

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
            //blockStateOutput.accept(MultiVariantGenerator.dispatch(pipeType.getBlock(), BlockModelGenerators.variant(baseVariant)));
            blockStateOutput.accept(generator);
        }

        @Override
        public void run()
        {
            createTrivialCube(BlockRegistry.EXAMPLE_BLOCK.get());

            PipeTypeRegistry.forEach(type -> {
                createPipeFrame(type);
            });
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
            for (var upgrade : PipeUpgradeRegistry.PIPE_UPGRADE_REGISTRY.entrySet())
                generateFlatItem(upgrade.getValue().getItem(), ModelTemplates.FLAT_ITEM);

            generateFlatItem(ItemRegistry.DEBUG_ITEM.get(), ModelTemplates.FLAT_ITEM);
            generateFlatItem(ItemRegistry.EXTRACTION_CARD.get(), ModelTemplates.FLAT_ITEM);
            generateFlatItem(ItemRegistry.INSERTION_CARD.get(), ModelTemplates.FLAT_ITEM);
            generateFlatItem(ItemRegistry.ROUND_ROBIN_CARD.get(), ModelTemplates.FLAT_ITEM);
        }
    }
}