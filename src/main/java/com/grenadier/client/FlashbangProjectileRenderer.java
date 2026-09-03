package com.grenadier.client;

import com.grenadier.GrenadierMod;
import com.grenadier.flashbang.FlashbangProjectile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class FlashbangProjectileRenderer extends EntityRenderer<FlashbangProjectile> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            GrenadierMod.MODID, "textures/entity/flashbang.png"
    );

    private final FlashbangProjectileModel model;

    public FlashbangProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new FlashbangProjectileModel(context.bakeLayer(FlashbangProjectileModel.LAYER_LOCATION));
        this.shadowRadius = 0.14F;
    }

    @Override
    public void render(FlashbangProjectile entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.23F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees((entity.tickCount + partialTicks) * 20.0F));
        poseStack.scale(-0.68F, -0.68F, 0.68F);

        this.model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(FlashbangProjectile entity) {
        return TEXTURE;
    }
}
