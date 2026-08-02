package com.grenadier.network;

import com.grenadier.GrenadierMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record FlashbangPayload(int intensityMilli, int whiteTicks, int fadeTicks) implements CustomPacketPayload {
    public static final Type<FlashbangPayload> TYPE = new Type<>(GrenadierMod.path("flashbang"));
    public static final StreamCodec<RegistryFriendlyByteBuf, FlashbangPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FlashbangPayload::intensityMilli,
            ByteBufCodecs.VAR_INT, FlashbangPayload::whiteTicks,
            ByteBufCodecs.VAR_INT, FlashbangPayload::fadeTicks,
            FlashbangPayload::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
