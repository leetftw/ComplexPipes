package com.leetftw.complexpipes.client.render.block_entity;

import com.leetftw.complexpipes.common.blocks.PipeBlockEntity;
import com.leetftw.complexpipes.common.pipe.network.ClientPipeConnection;
import com.leetftw.complexpipes.common.pipe.network.PipeConnectionMode;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MaterialMapper;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class PipeRenderer implements BlockEntityRenderer<PipeBlockEntity, PipeRenderState> {
    public PipeRenderer(BlockEntityRendererProvider.Context context) {

        //Identifier baseModel = Identifier.fromNamespaceAndPath(MODID, "quarry_frame");
    }


    @Override
    public PipeRenderState createRenderState() {
        return new PipeRenderState();
    }

    @Override
    public void extractRenderState(PipeBlockEntity blockEntity, PipeRenderState renderState, float partialTick, @NonNull Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPosition, breakProgress);
        renderState.pipeBlockEntity = blockEntity;
    }

    @Override
    public void submit(PipeRenderState renderState, PoseStack poseStack, @NonNull SubmitNodeCollector nodeCollector, @NonNull CameraRenderState cameraRenderState) {
        int overlay = OverlayTexture.NO_OVERLAY;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        Material pipeMaterial = new MaterialMapper(TextureAtlas.LOCATION_BLOCKS, "block").apply(renderState.pipeBlockEntity.TYPE.getTexturePath());
        TextureAtlasSprite sprite = Minecraft.getInstance().getAtlasManager().get(pipeMaterial);
        // This variable needs to be renamed
        float pipeRadius = 8f / 16f / 2f;

        for (ClientPipeConnection connection : renderState.pipeBlockEntity.getClientPipeConnections()) {
            if (connection.mode() == PipeConnectionMode.PASSIVE)
                continue;

            float U0 = sprite.getU(0.5f);
            float U1 = sprite.getU(1);
            float V1 = sprite.getV(connection.mode() == PipeConnectionMode.INSERT ? 0 : 0.25f);
            float V0 = sprite.getV(connection.mode() == PipeConnectionMode.INSERT ? 0.25f : 0.5f);

            // Rotate pose stack so that pipe faces towards positive y
            poseStack.pushPose();
            poseStack.mulPose(connection.side().getRotation());

            nodeCollector.submitCustomGeometry(poseStack, pipeMaterial.renderType(RenderTypes::entitySolid), (PoseStack.Pose pose, VertexConsumer consumer) -> {
                BlockColors blockColors = Minecraft.getInstance().getBlockColors();
                /*int color = blockColors.getColor(
                        renderState.blockState,
                        renderState.pipeBlockEntity.getLevel(),
                        renderState.blockPos
                );*/

                int color = 0xFFFFFFFF;

                Vector3f normal = new Vector3f(-1, 0, 0);

                consumer.addVertex(pose, -pipeRadius, pipeRadius, -pipeRadius)
                        .setUv(U0, V1).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);
                consumer.addVertex(pose, -pipeRadius, pipeRadius,  pipeRadius)
                        .setUv(U1, V1).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);
                consumer.addVertex(pose, -pipeRadius, 0.5f,        pipeRadius)
                        .setUv(U1, V0).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);
                consumer.addVertex(pose, -pipeRadius, 0.5f,       -pipeRadius)
                        .setUv(U0, V0).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);

                normal.set(1, 0, 0);

                consumer.addVertex(pose,  pipeRadius, pipeRadius,  pipeRadius)
                        .setUv(U0, V1).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);
                consumer.addVertex(pose,  pipeRadius, pipeRadius, -pipeRadius)
                        .setUv(U1, V1).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);
                consumer.addVertex(pose,  pipeRadius, 0.5f,       -pipeRadius)
                        .setUv(U1, V0).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);
                consumer.addVertex(pose,  pipeRadius, 0.5f,        pipeRadius)
                        .setUv(U0, V0).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);

                normal.set(0, 0, -1);

                consumer.addVertex(pose,  pipeRadius, pipeRadius, -pipeRadius)
                        .setUv(U0, V1).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);
                consumer.addVertex(pose, -pipeRadius, pipeRadius, -pipeRadius)
                        .setUv(U1, V1).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);
                consumer.addVertex(pose, -pipeRadius, 0.5f,       -pipeRadius)
                        .setUv(U1, V0).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);
                consumer.addVertex(pose,  pipeRadius, 0.5f,       -pipeRadius)
                        .setUv(U0, V0).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);

                normal.set(0, 0, 1);

                consumer.addVertex(pose, -pipeRadius, pipeRadius,  pipeRadius)
                        .setUv(U0, V1).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);
                consumer.addVertex(pose,  pipeRadius, pipeRadius,  pipeRadius)
                        .setUv(U1, V1).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);
                consumer.addVertex(pose,  pipeRadius, 0.5f,        pipeRadius)
                        .setUv(U1, V0).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);
                consumer.addVertex(pose, -pipeRadius, 0.5f,        pipeRadius)
                        .setUv(U0, V0).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);

                normal.set(0, 1, 0);

                consumer.addVertex(pose,  pipeRadius, pipeRadius,  pipeRadius)
                        .setUv(U0, V1).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);
                consumer.addVertex(pose, -pipeRadius, pipeRadius,  pipeRadius)
                        .setUv(U1, V1).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);
                consumer.addVertex(pose, -pipeRadius, pipeRadius, -pipeRadius)
                        .setUv(U1, V0).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);
                consumer.addVertex(pose,  pipeRadius, pipeRadius, -pipeRadius)
                        .setUv(U0, V0).setNormal(pose, normal).setLight(renderState.lightCoords).setOverlay(overlay).setColor(color);
            });

            poseStack.popPose();
        }

        poseStack.popPose();
    }
}
