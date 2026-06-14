package com.leetftw.complexpipes.client.gui;

import com.leetftw.complexpipes.common.gui.PipeConnectionMenu;
import com.leetftw.complexpipes.common.network.PipeScreenNumericSyncPayload;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jspecify.annotations.NonNull;

public class PipeConnectionScreen extends AbstractContainerScreen<PipeConnectionMenu> implements ContainerListener {
    private static final Identifier CONTAINER_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/container/generic_54.png");
    private final int containerRows;
    private EditBox priority;
    // TODO: Hide ratio when not supported by pipe type
    private EditBox ratio;
    int lastPriority = Integer.MAX_VALUE;
    int lastRatio = Integer.MAX_VALUE;

    public PipeConnectionScreen(PipeConnectionMenu menu, Inventory playerInventory, Component title) {
        int contRows = (menu.getPipeConnection().getMaxCards() + 8) / 9;
        super(menu, playerInventory, title, 176, 114 + contRows * 18);

        this.containerRows = contRows;
        this.inventoryLabelY = this.imageHeight - 94;
    }

    private void initEditBox(EditBox box) {
        box.setCanLoseFocus(true);
        box.setTextColor(-1);
        box.setTextColorUneditable(-1);
        box.setInvertHighlightedTextColor(false);
        box.setBordered(true);
        box.setMaxLength(50);
        box.setValue("");
        box.setResponder(this::onNameChanged);
    }

    @Override
    protected void init() {
        this.inventoryLabelY = this.imageHeight - 94 + 27;
        super.init();

        this.priority = new EditBox(this.font, this.leftPos + 67, this.topPos + containerRows * 18 + 19, 103, 12, Component.literal("ratio"));
        this.ratio = new EditBox(this.font, this.leftPos + 67, this.topPos + containerRows * 18 + 19 + 12, 103, 12, Component.literal("ratio"));
        initEditBox(this.priority);
        initEditBox(this.ratio);

        addRenderableWidget(this.priority);
        addRenderableWidget(this.ratio);

        this.priority.setEditable(true);
        this.priority.setFilter(PipeConnectionScreen::acceptsString);

        this.ratio.setEditable(true);
        this.ratio.setFilter(PipeConnectionScreen::acceptsStringStrictlyPositive);

        menu.removeSlotListener(this);
        menu.addSlotListener(this);
    }

    @Override
    public void resize(int width, int height) {
        String priorityValue = this.priority.getValue();
        String ratioValue = this.ratio.getValue();
        this.init(width, height);
        this.priority.setValue(priorityValue);
        this.ratio.setValue(ratioValue);
    }

    private static boolean acceptsString(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c < '0' || c > '9') && !(c == '-' && i == 0)) {
                return false;
            }
        }
        return true;
    }

    private static boolean acceptsStringStrictlyPositive(String s) {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if ((c < '0' || c > '9')) {
                return false;
            }
        }
        return true;
    }

    private static int parseSignedIntOrZero(String s) {
        if (s == null || s.isEmpty() || s.equals("-")) {
            return 0;
        }
        return Integer.parseInt(s);
    }

    private void onNameChanged(String s) {
        int priorityValue = parseSignedIntOrZero(priority.getValue());
        int ratioValue = parseSignedIntOrZero(ratio.getValue());

        if (lastPriority != priorityValue || lastRatio != ratioValue) {
            ClientPacketDistributor.sendToServer(new PipeScreenNumericSyncPayload(
                    menu.getPipeConnection().getPipePos(),
                    menu.getPipeConnection().getSide(),
                    priorityValue,
                    ratioValue
            ));
        }

        lastPriority = priorityValue;
        lastRatio = ratioValue;
    }

    @Override
    public void dataChanged(@NonNull AbstractContainerMenu containerMenu, int dataSlotIndex, int value) {
        if (menu.getPipeConnection().getPriority() != lastPriority) {
            priority.setValue(String.valueOf(menu.getPipeConnection().getPriority()));
        }
        if (menu.getPipeConnection().getRatio() != lastRatio) {
            ratio.setValue(String.valueOf(menu.getPipeConnection().getRatio()));
        }
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick)
    {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTick);
        this.extractTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTick) {
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;

        // Render top slots
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, i, j, 0.0F, 0.0F, this.imageWidth, containerRows * 18 + 17, 256, 256);
        // 'Unrender' some top slots in last row
        int remainder = menu.getPipeConnection().getMaxCards() % 9;
        if (remainder > 0) {
            int disabledSlots = 9 - remainder;
            int lastRowY = j + (containerRows - 1) * 18 + 17;
            int x = remainder * 18 + i + 7;
            int width = disabledSlots * 18;
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, x, lastRowY, 7.0F, 126.0F, width, 9, 256, 256);
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, x, lastRowY + 9, 7.0F, 126.0F, width, 9, 256, 256);
        }

        // Render empty middle part
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, i, j + containerRows * 18 + 17, 0.0F, 126.0F, this.imageWidth, 9, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, i, j + containerRows * 18 + 17 + 9, 0.0F, 126.0F, this.imageWidth, 9, 256, 256);
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, i, j + containerRows * 18 + 17 + 18, 0.0F, 126.0F, this.imageWidth, 9, 256, 256);

        // Render inventory
        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, CONTAINER_BACKGROUND, i, j + containerRows * 18 + 17 + 27, 0.0F, 126.0F, this.imageWidth, 96, 256, 256);


    }

    @Override
    protected void extractLabels(@NonNull GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
        super.extractLabels(guiGraphics, mouseX, mouseY);

        guiGraphics.text(font, "Priority:", inventoryLabelX, containerRows * 18 + 20, -12566464, false);
        guiGraphics.text(font, "Ratio:", inventoryLabelX, containerRows * 18 + 20 + 12, -12566464, false);
        guiGraphics.text(font, "Current transfer per operation: " + menu.getPipeConnection().calculateTransferRate(), 0, -24, 0xFFFFFFFF, false);
        guiGraphics.text(font, "Current operation time: " + menu.getPipeConnection().calculateOperationTime() + " ticks", 0, -12, 0xFFFFFFFF, false);
    }

    @Override
    public void slotChanged(@NonNull AbstractContainerMenu containerToSend, int dataSlotIndex, @NonNull ItemStack stack) {

    }
}
