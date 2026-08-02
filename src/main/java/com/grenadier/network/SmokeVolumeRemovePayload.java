package com.grenadier.network;

import com.grenadier.GrenadierMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

public record SmokeVolumeRemovePayload(long serverGameTime, UUID id) implements CustomPacketPayload {
    public static final Type<SmokeVolumeRemovePayload> TYPE =
            new Type<>(GrenadierMod.path("smoke_volume_remove"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SmokeVolumeRemovePayload> STREAM_CODEC =
            StreamCodec.of(SmokeVolumeRemovePayload::encode, SmokeVolumeRemovePayload::decode);

    private static void encode(RegistryFriendlyByteBuf buffer, SmokeVolumeRemovePayload payload) {
        buffer.writeVarLong(payload.serverGameTime);
        buffer.writeUUID(payload.id);
    }

    private static SmokeVolumeRemovePayload decode(RegistryFriendlyByteBuf buffer) {
        return new SmokeVolumeRemovePayload(buffer.readVarLong(), buffer.readUUID());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
