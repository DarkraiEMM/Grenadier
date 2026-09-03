package com.grenadier.client;

import com.grenadier.GrenadierMod;
import com.grenadier.grenade.ImpactGrenadeProjectile;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class ImpactGrenadeProjectileModel extends HierarchicalModel<ImpactGrenadeProjectile> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            GrenadierMod.path("impact_grenade_projectile"),
            "main"
    );

    private final ModelPart root;

    public ImpactGrenadeProjectileModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "main",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -4.0F, -3.5F, 8.0F, 8.0F, 7.0F)
                        .texOffs(0, 16).addBox(-3.5F, -5.0F, -3.0F, 7.0F, 1.0F, 6.0F)
                        .texOffs(28, 0).addBox(-3.5F, 4.0F, -3.0F, 7.0F, 1.0F, 6.0F)
                        .texOffs(28, 8).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 1.0F, 4.0F),
                PartPose.ZERO
        );
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(ImpactGrenadeProjectile entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
