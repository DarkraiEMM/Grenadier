package com.grenadier.network;

import com.grenadier.GrenadierMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record SmokeFogPayload(int durationTicks) implements CustomPacketPayload {
    public static final Type<SmokeFogPayload> TYPE = new Type<>(GrenadierMod.path("smoke_fog"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SmokeFogPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            SmokeFogPayload::durationTicks,
            SmokeFogPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
