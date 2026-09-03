package com.grenadier.client;

import com.grenadier.GrenadierMod;
import com.grenadier.signal.SignalFlareProjectile;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class SmokeGrenadeProjectileModel extends HierarchicalModel<SignalFlareProjectile> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            GrenadierMod.path("smoke_grenade_projectile"),
            "main"
    );

    private final ModelPart root;
    private final ModelPart base;
    private final ModelPart stripe;

    public SmokeGrenadeProjectileModel(ModelPart root) {
        this.root = root;
        this.base = root.getChild("base");
        this.stripe = root.getChild("stripe");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild(
                "base",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -5.0F, -3.0F, 8.0F, 10.0F, 6.0F)
                        .texOffs(0, 18).addBox(-4.5F, 4.0F, -3.5F, 9.0F, 1.0F, 7.0F)
                        .texOffs(32, 0).addBox(-3.0F, -7.0F, -2.5F, 6.0F, 2.0F, 5.0F)
                        .texOffs(32, 8).addBox(-2.5F, -8.0F, -2.0F, 5.0F, 1.0F, 4.0F)
                        .texOffs(32, 14).addBox(3.65F, -6.0F, -2.4F, 1.0F, 8.0F, 2.0F)
                        .texOffs(40, 26).addBox(3.45F, -6.5F, -3.25F, 1.0F, 1.0F, 1.0F),
                PartPose.ZERO
        );

        root.addOrReplaceChild(
                "stripe",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -1.0F, -3.08F, 8.0F, 2.0F, 0.16F)
                        .texOffs(0, 3).addBox(-4.0F, -1.0F, 2.92F, 8.0F, 2.0F, 0.16F)
                        .texOffs(0, 6).addBox(-4.08F, -1.0F, -3.0F, 0.16F, 2.0F, 6.0F)
                        .texOffs(7, 6).addBox(3.92F, -1.0F, -3.0F, 0.16F, 2.0F, 6.0F),
                PartPose.ZERO
        );

        return LayerDefinition.create(mesh, 64, 64);
    }

    public void showBase() {
        this.base.visible = true;
        this.stripe.visible = false;
    }

    public void showStripe() {
        this.base.visible = false;
        this.stripe.visible = true;
    }

    @Override
    public void setupAnim(SignalFlareProjectile entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
