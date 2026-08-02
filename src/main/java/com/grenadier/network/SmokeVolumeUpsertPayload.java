package com.grenadier.network;

import com.grenadier.GrenadierMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SmokeVolumeUpsertPayload(long serverGameTime, SmokeVolumeData smoke) implements CustomPacketPayload {
    public static final Type<SmokeVolumeUpsertPayload> TYPE =
            new Type<>(GrenadierMod.path("smoke_volume_upsert"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SmokeVolumeUpsertPayload> STREAM_CODEC =
            StreamCodec.of(SmokeVolumeUpsertPayload::encode, SmokeVolumeUpsertPayload::decode);

    private static void encode(RegistryFriendlyByteBuf buffer, SmokeVolumeUpsertPayload payload) {
        buffer.writeVarLong(payload.serverGameTime);
        SmokeVolumeData.encode(buffer, payload.smoke);
    }

    private static SmokeVolumeUpsertPayload decode(RegistryFriendlyByteBuf buffer) {
        return new SmokeVolumeUpsertPayload(buffer.readVarLong(), SmokeVolumeData.decode(buffer));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
