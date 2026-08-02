package com.grenadier.client;

import com.grenadier.GrenadierMod;
import com.grenadier.signal.SignalFlareProjectile;
import com.grenadier.smoke.SmokeGrenadeColors;
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

public final class SmokeGrenadeProjectileRenderer extends EntityRenderer<SignalFlareProjectile> {
    private static final ResourceLocation BASE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            GrenadierMod.MODID, "textures/entity/smoke_grenade.png"
    );
    private static final ResourceLocation STRIPE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            GrenadierMod.MODID, "textures/entity/smoke_grenade_stripe.png"
    );

    private final SmokeGrenadeProjectileModel model;

    public SmokeGrenadeProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SmokeGrenadeProjectileModel(context.bakeLayer(SmokeGrenadeProjectileModel.LAYER_LOCATION));
        this.shadowRadius = 0.14F;
    }

    @Override
    public void render(SignalFlareProjectile entity, float entityYaw, float partialTicks,
                       PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.23F, 0.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees((entity.tickCount + partialTicks) * 21.0F));
        poseStack.scale(-0.68F, -0.68F, 0.68F);

        this.model.setupAnim(entity, 0.0F, 0.0F, entity.tickCount + partialTicks, 0.0F, 0.0F);
        this.model.showBase();
        VertexConsumer baseBuffer = buffer.getBuffer(RenderType.entityCutoutNoCull(BASE_TEXTURE));
        this.model.renderToBuffer(poseStack, baseBuffer, packedLight, OverlayTexture.NO_OVERLAY);

        this.model.showStripe();
        VertexConsumer stripeBuffer = buffer.getBuffer(RenderType.entityCutoutNoCull(STRIPE_TEXTURE));
        this.model.renderToBuffer(
                poseStack,
                stripeBuffer,
                packedLight,
                OverlayTexture.NO_OVERLAY,
                SmokeGrenadeColors.displayArgb(entity.getItem())
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SignalFlareProjectile entity) {
        return BASE_TEXTURE;
    }
}
