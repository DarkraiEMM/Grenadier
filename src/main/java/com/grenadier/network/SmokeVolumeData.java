package com.grenadier.network;

import com.grenadier.smoke.SmokeCloud;
import net.minecraft.network.RegistryFriendlyByteBuf;

import java.util.UUID;

public record SmokeVolumeData(
        UUID id,
        double x,
        double y,
        double z,
        float radius,
        int color,
        long createdGameTime,
        long expiresGameTime,
        int deployTicks,
        float cascadeDirectionX,
        float cascadeDirectionZ,
        float cascadeEdgeDistance,
        float cascadeDropDistance,
        float cascadeCurtainWidth,
        float cascadePoolRadius
) {
    public static final int MAX_SNAPSHOT_SMOKES = 24;
    public static final float MAX_RADIUS = 32.0F;
    public static final int MAX_DEPLOY_TICKS = 20 * 30;
    public static final long MAX_LIFETIME_TICKS = 20L * 60L * 10L;

    public static SmokeVolumeData fromCloud(SmokeCloud cloud, int deployTicks) {
        return new SmokeVolumeData(
                cloud.id(),
                cloud.center().x,
                cloud.center().y,
                cloud.center().z,
                (float) cloud.radius(),
                cloud.color(),
                cloud.createdGameTime(),
                cloud.expiresGameTime(),
                deployTicks,
                cloud.cascade().directionX(),
                cloud.cascade().directionZ(),
                cloud.cascade().edgeDistance(),
                cloud.cascade().dropDistance(),
                cloud.cascade().curtainWidth(),
                cloud.cascade().poolRadius()
        );
    }

    public boolean valid() {
        long lifetime = this.expiresGameTime - this.createdGameTime;
        boolean cascadeValid = Float.isFinite(this.cascadeDirectionX)
                && Float.isFinite(this.cascadeDirectionZ)
                && Float.isFinite(this.cascadeEdgeDistance)
                && Float.isFinite(this.cascadeDropDistance)
                && Float.isFinite(this.cascadeCurtainWidth)
                && Float.isFinite(this.cascadePoolRadius)
                && this.cascadeEdgeDistance >= 0.0F
                && this.cascadeEdgeDistance <= this.radius
                && this.cascadeDropDistance >= 0.0F
                && this.cascadeDropDistance <= com.grenadier.smoke.SmokeCascade.MAX_DROP
                && this.cascadeCurtainWidth >= 0.0F
                && this.cascadeCurtainWidth <= com.grenadier.smoke.SmokeCascade.MAX_CURTAIN_WIDTH
                && this.cascadePoolRadius >= 0.0F
                && this.cascadePoolRadius <= com.grenadier.smoke.SmokeCascade.MAX_POOL_RADIUS;
        if (this.cascadeDropDistance >= 2.5F) {
            float directionLengthSquared = this.cascadeDirectionX * this.cascadeDirectionX
                    + this.cascadeDirectionZ * this.cascadeDirectionZ;
            cascadeValid &= directionLengthSquared >= 0.64F && directionLengthSquared <= 1.44F;
            cascadeValid &= this.cascadeCurtainWidth >= 1.0F && this.cascadePoolRadius >= 0.5F;
        }
        return this.id != null
                && Double.isFinite(this.x)
                && Double.isFinite(this.y)
                && Double.isFinite(this.z)
                && Float.isFinite(this.radius)
                && this.radius > 0.0F
                && this.radius <= MAX_RADIUS
                && this.deployTicks >= 0
                && this.deployTicks <= MAX_DEPLOY_TICKS
                && lifetime > 0L
                && lifetime <= MAX_LIFETIME_TICKS
                && cascadeValid;
    }

    public static boolean validSnapshotSize(int count) {
        return count >= 0 && count <= MAX_SNAPSHOT_SMOKES;
    }

    static void encode(RegistryFriendlyByteBuf buffer, SmokeVolumeData data) {
        buffer.writeUUID(data.id);
        buffer.writeDouble(data.x);
        buffer.writeDouble(data.y);
        buffer.writeDouble(data.z);
        buffer.writeFloat(data.radius);
        buffer.writeInt(data.color);
        buffer.writeVarLong(data.createdGameTime);
        buffer.writeVarLong(data.expiresGameTime);
        buffer.writeVarInt(data.deployTicks);
        buffer.writeFloat(data.cascadeDirectionX);
        buffer.writeFloat(data.cascadeDirectionZ);
        buffer.writeFloat(data.cascadeEdgeDistance);
        buffer.writeFloat(data.cascadeDropDistance);
        buffer.writeFloat(data.cascadeCurtainWidth);
        buffer.writeFloat(data.cascadePoolRadius);
    }

    static SmokeVolumeData decode(RegistryFriendlyByteBuf buffer) {
        return new SmokeVolumeData(
                buffer.readUUID(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readFloat(),
                buffer.readInt(),
                buffer.readVarLong(),
                buffer.readVarLong(),
                buffer.readVarInt(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat()
        );
    }
}
