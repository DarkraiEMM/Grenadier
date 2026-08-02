package com.grenadier.network;

import com.grenadier.client.SmokeClientPayloadHandler;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class SmokeNetwork {
    private SmokeNetwork() {
    }

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("4");
        registrar.playToClient(
                SmokeFogPayload.TYPE,
                SmokeFogPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> SmokeFogClientState.refresh(payload.durationTicks()))
        );
        registrar.playToClient(
                SmokeVolumeUpsertPayload.TYPE,
                SmokeVolumeUpsertPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> SmokeClientPayloadHandler.handle(payload))
        );
        registrar.playToClient(
                SmokeVolumeRemovePayload.TYPE,
                SmokeVolumeRemovePayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> SmokeClientPayloadHandler.handle(payload))
        );
        registrar.playToClient(
                SmokeVolumeSnapshotPayload.TYPE,
                SmokeVolumeSnapshotPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> SmokeClientPayloadHandler.handle(payload))
        );
        registrar.playToClient(
                FlashbangPayload.TYPE,
                FlashbangPayload.STREAM_CODEC,
                (payload, context) -> context.enqueueWork(() -> FlashbangClientState.apply(
                        payload.intensityMilli(), payload.whiteTicks(), payload.fadeTicks()))
        );
    }
}
