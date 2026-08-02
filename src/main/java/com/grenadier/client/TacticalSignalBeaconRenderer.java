package com.grenadier.client;

import com.grenadier.signal.TacticalSignalBeaconBlock;
import com.grenadier.signal.TacticalSignalBeaconBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TacticalSignalBeaconRenderer implements BlockEntityRenderer<TacticalSignalBeaconBlockEntity> {
    public TacticalSignalBeaconRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            TacticalSignalBeaconBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        if (blockEntity.getLevel() == null || !blockEntity.getBlockState().getValue(TacticalSignalBeaconBlock.ACTIVE)) {
            return;
        }

        BeaconRenderer.renderBeaconBeam(
                poseStack,
                bufferSource,
                BeaconRenderer.BEAM_LOCATION,
                partialTick,
                1.0F,
                blockEntity.getLevel().getGameTime(),
                0,
                1024,
                blockEntity.beamColor(),
                0.18F,
                0.42F
        );
    }

    @Override
    public boolean shouldRenderOffScreen(TacticalSignalBeaconBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(TacticalSignalBeaconBlockEntity blockEntity, Vec3 cameraPos) {
        return Vec3.atCenterOf(blockEntity.getBlockPos())
                .multiply(1.0D, 0.0D, 1.0D)
                .closerThan(cameraPos.multiply(1.0D, 0.0D, 1.0D), this.getViewDistance());
    }

    @Override
    public AABB getRenderBoundingBox(TacticalSignalBeaconBlockEntity blockEntity) {
        var pos = blockEntity.getBlockPos();
        return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1.0D, 1024.0D, pos.getZ() + 1.0D);
    }
}
