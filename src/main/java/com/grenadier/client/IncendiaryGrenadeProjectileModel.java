package com.grenadier.client;

import com.grenadier.GrenadierMod;
import com.grenadier.incendiary.IncendiaryGrenadeProjectile;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public final class IncendiaryGrenadeProjectileModel extends HierarchicalModel<IncendiaryGrenadeProjectile> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            GrenadierMod.path("incendiary_grenade_projectile"),
            "main"
    );

    private final ModelPart root;

    public IncendiaryGrenadeProjectileModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(
                "main",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-5.0F, -3.5F, -3.5F, 10.0F, 7.0F, 7.0F)
                        .texOffs(0, 18).addBox(-5.5F, 3.0F, -4.0F, 11.0F, 1.0F, 8.0F)
                        .texOffs(32, 0).addBox(-3.0F, -5.0F, -2.5F, 6.0F, 1.5F, 5.0F)
                        .texOffs(32, 8).addBox(-2.0F, -7.0F, -2.0F, 4.0F, 2.0F, 4.0F)
                        .texOffs(32, 16).addBox(4.65F, -4.5F, -2.4F, 1.0F, 6.5F, 2.0F)
                        .texOffs(40, 28).addBox(4.45F, -5.0F, -3.25F, 1.0F, 1.0F, 1.0F),
                PartPose.ZERO
        );
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void setupAnim(IncendiaryGrenadeProjectile entity, float limbSwing, float limbSwingAmount,
                          float ageInTicks, float netHeadYaw, float headPitch) {
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
