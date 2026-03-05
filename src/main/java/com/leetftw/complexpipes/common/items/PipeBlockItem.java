package com.leetftw.complexpipes.common.items;

import com.leetftw.complexpipes.common.cards.PipeCard;
import com.leetftw.complexpipes.common.cards.PipeCardRegistry;
import com.leetftw.complexpipes.common.pipe.types.PipeTypeRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;
import com.leetftw.complexpipes.common.blocks.PipeBlock;

import java.util.function.Consumer;

public class PipeBlockItem extends BlockItem {
    PipeBlock pipeBlock;

    public PipeBlockItem(Block block, Properties properties) {
        super(block, properties);
        if (!(block instanceof PipeBlock pipeBlock))
            throw new RuntimeException();

        this.pipeBlock = pipeBlock;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        Style style = Style.EMPTY.withColor(TextColor.fromLegacyFormat(ChatFormatting.DARK_GRAY));
        tooltipAdder.accept(Component.translatable("tooltip.complexpipes.supportedCards")
                .setStyle(style));
        PipeCardRegistry.PIPE_CARD_TYPE_REGISTRY.forEach(cardType -> {
            PipeCard instance = cardType.instantiate();
            if (pipeBlock.getType().supportsCard(instance)) {
                tooltipAdder.accept(Component.translatable("tooltip.complexpipes.supportedCardEntry", cardType.getItem().getName()).setStyle(style));
            }
        });

        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
    }
}
