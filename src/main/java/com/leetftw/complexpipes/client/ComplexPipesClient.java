package com.leetftw.complexpipes.client;

import com.leetftw.complexpipes.client.gui.ItemStackFilterScreen;
import com.leetftw.complexpipes.client.gui.PipeConnectionScreen;
import com.leetftw.complexpipes.client.render.block_entity.PipeRenderer;
import com.leetftw.complexpipes.common.ComplexPipes;
import com.leetftw.complexpipes.common.blocks.PipeBlockEntity;
import com.leetftw.complexpipes.common.gui.MenuRegistry;
import com.leetftw.complexpipes.common.network.PipeSyncPayload;
import com.leetftw.complexpipes.common.pipe.types.PipeTypeRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.HandlerThread;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ComplexPipes.MODID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = ComplexPipes.MODID, value = Dist.CLIENT)
public class ComplexPipesClient {
    public ComplexPipesClient(ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
        ComplexPipes.LOGGER.info("HELLO FROM CLIENT SETUP");
        ComplexPipes.LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(MenuRegistry.PIPE_CONNECTION_MENU.get(), PipeConnectionScreen::new);
        event.register(MenuRegistry.ITEM_STACK_FILTER_MENU.get(), ItemStackFilterScreen::new);
    }

    @SubscribeEvent // on the mod event bus only on the physical client
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        PipeTypeRegistry.forEach(pipeType -> {
                    event.registerBlockEntityRenderer(
                            // The block entity type to register the renderer for.
                            pipeType.getBlockEntityType(),
                            // A function of BlockEntityRendererProvider.Context to BlockEntityRenderer.
                            PipeRenderer::new
                    );
                }
        );
    }

    public static void handlePipeSynchronizationPacket(final PipeSyncPayload data, final IPayloadContext context) {
        ClientLevel clientLevel = Minecraft.getInstance().level;
        if (clientLevel == null)
            return;

        if (clientLevel.dimension() != data.dimension())
            return;

        if (!clientLevel.isLoaded(data.position()))
            return;

        BlockEntity be = clientLevel.getBlockEntity(data.position());
        if (!(be instanceof PipeBlockEntity pipeBE))
            return;

        pipeBE.setClientPipeConnections(data.connections());
    }

    @SubscribeEvent
    public static void registerClientPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(
                PipeSyncPayload.TYPE,
                HandlerThread.MAIN,
                ComplexPipesClient::handlePipeSynchronizationPacket
        );
    }
}
