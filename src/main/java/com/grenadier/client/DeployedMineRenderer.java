package com.grenadier.client;

import com.grenadier.mine.DeployedMineEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public final class DeployedMineRenderer extends EntityRenderer<DeployedMineEntity> {
    private final BlockRenderDispatcher blocks;

    public DeployedMineRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.blocks = context.getBlockRenderDispatcher();
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(DeployedMineEntity mine, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - mine.getYRot()));
        poseStack.translate(-0.5D, 0.0D, -0.5D);
        blocks.renderSingleBlock(mine.visualState(), poseStack, buffers, packedLight,
                OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(mine, entityYaw, partialTick, poseStack, buffers, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(DeployedMineEntity entity) {
        return InventoryMenu.BLOCK_ATLAS;
    }
}
