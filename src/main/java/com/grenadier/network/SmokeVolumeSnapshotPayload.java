package com.grenadier.network;

import com.grenadier.GrenadierMod;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;

public record SmokeVolumeSnapshotPayload(long serverGameTime, List<SmokeVolumeData> smokes)
        implements CustomPacketPayload {
    public static final int MAX_SMOKES = SmokeVolumeData.MAX_SNAPSHOT_SMOKES;
    public static final Type<SmokeVolumeSnapshotPayload> TYPE =
            new Type<>(GrenadierMod.path("smoke_volume_snapshot"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SmokeVolumeSnapshotPayload> STREAM_CODEC =
            StreamCodec.of(SmokeVolumeSnapshotPayload::encode, SmokeVolumeSnapshotPayload::decode);

    public SmokeVolumeSnapshotPayload {
        smokes = List.copyOf(smokes);
        if (!SmokeVolumeData.validSnapshotSize(smokes.size())) {
            throw new IllegalArgumentException("Smoke snapshot exceeds " + MAX_SMOKES + " records");
        }
    }

    private static void encode(RegistryFriendlyByteBuf buffer, SmokeVolumeSnapshotPayload payload) {
        buffer.writeVarLong(payload.serverGameTime);
        buffer.writeVarInt(payload.smokes.size());
        for (SmokeVolumeData smoke : payload.smokes) {
            SmokeVolumeData.encode(buffer, smoke);
        }
    }

    private static SmokeVolumeSnapshotPayload decode(RegistryFriendlyByteBuf buffer) {
        long serverGameTime = buffer.readVarLong();
        int count = buffer.readVarInt();
        if (!SmokeVolumeData.validSnapshotSize(count)) {
            throw new DecoderException("Invalid smoke snapshot size: " + count);
        }
        List<SmokeVolumeData> smokes = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            smokes.add(SmokeVolumeData.decode(buffer));
        }
        return new SmokeVolumeSnapshotPayload(serverGameTime, smokes);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
