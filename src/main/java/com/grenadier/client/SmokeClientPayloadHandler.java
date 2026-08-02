package com.grenadier.client;

import com.grenadier.network.SmokeVolumeRemovePayload;
import com.grenadier.network.SmokeVolumeSnapshotPayload;
import com.grenadier.network.SmokeVolumeUpsertPayload;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public final class SmokeClientPayloadHandler {
    private SmokeClientPayloadHandler() {
    }

    public static void handle(SmokeVolumeUpsertPayload payload) {
        ClientSmokeVolumeManager.upsert(payload.serverGameTime(), payload.smoke());
    }

    public static void handle(SmokeVolumeRemovePayload payload) {
        ClientSmokeVolumeManager.remove(payload.serverGameTime(), payload.id());
    }

    public static void handle(SmokeVolumeSnapshotPayload payload) {
        ClientSmokeVolumeManager.acceptSnapshot(payload.serverGameTime(), payload.smokes());
    }
}
