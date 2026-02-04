package com.leetftw.complexpipes.client.gui;

import com.leetftw.complexpipes.common.gui.PipeConnectionMenu;
import com.leetftw.complexpipes.common.network.PipeScreenNumericSyncPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

public class PipeConnectionScreen extends AbstractContainerScreen<PipeConnectionMenu> implements ContainerListener {
    private static final Identifier CONTAINER_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private static final int containerRows = 2;
    private EditBox ratio;
    int lastValue = Integer.MAX_VALUE;

    public PipeConnectionScreen(PipeConnectionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageHeight = 114 + containerRows * 18;
        this.inventoryLabelY = this.imageHeight - 94;

        this.ratio = new EditBox(this.font, 10, this.inventoryLabelY - 18, 103, 12, Component.literal("ratio"));
        this.ratio.setCanLoseFocus(false);
        this.ratio.setTextColor(-1);
        this.ratio.setTextColorUneditable(-1);
        this.ratio.setInvertHighlightedTextColor(false);
        this.ratio.setBordered(true);
        this.ratio.setMaxLength(50);
        this.ratio.setResponder(this::onNameChanged);
        this.ratio.setValue("");
        this.addRenderableWidget(this.ratio);
        this.ratio.setEditable(true);
        this.ratio.setFilter(PipeConnectionScreen::acceptsString);

        menu.addSlotListener(this);
    }

    private static boolean acceptsString(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c < '0' || c > '9') {
                return false;
            }

        }
        return true;
    }

    private void onNameChanged(String s) {
        int value;
        if (s.isEmpty()) {
            value = 0;
        } else {
            value = Integer.parseInt(s);
        }
        lastValue = value;

        ClientPacketDistributor.sendToServer(new PipeScreenNumericSyncPayload(
                //menu.getDimension(),
                ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath("", "")),
                menu.getPipeConnection().getPipePos(),
                menu.getPipeConnection().getSide(),
                -1, // TODO: Implement priority
                value
        ));
        //menu.ratioSlot.set(value);
    }

    @Override
    public void dataChanged(AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
        // 1: priority
        // 2: ratio
        if (dataSlotIndex == 2) {
            if (value != lastValue) {
                ratio.setValue(String.valueOf(value));
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, i, j, 0.0F, 0.0F, this.imageWidth, containerRows * 18 + 17, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, i, j + containerRows * 18 + 17, 0.0F, 126.0F, this.imageWidth, 96, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);

        guiGraphics.drawString(font, "Current r/op: " + menu.getPipeConnection().calculateTransferRate(), 80, 36, -12566464, false);
        guiGraphics.drawString(font, "Current t/op: " + menu.getPipeConnection().calculateOperationTime(), 80, 0, -12566464, false);
    }

    @Override
    public void slotChanged(AbstractContainerMenu containerToSend, int dataSlotIndex, ItemStack stack) {

    }
}
