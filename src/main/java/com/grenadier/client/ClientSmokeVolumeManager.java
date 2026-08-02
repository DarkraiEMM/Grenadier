package com.grenadier.client;

import com.grenadier.network.SmokeVolumeData;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ClientSmokeVolumeManager {
    public static final int MAX_RENDERED_SMOKES = 8;
    private static final Map<UUID, SmokeVolumeData> SMOKES = new LinkedHashMap<>();
    private static long serverTimeOffset;

    private ClientSmokeVolumeManager() {
    }

    public static void acceptSnapshot(long serverGameTime, List<SmokeVolumeData> smokes) {
        updateTimeOffset(serverGameTime);
        SMOKES.clear();
        for (SmokeVolumeData smoke : smokes) {
            if (smoke.valid()) {
                SMOKES.put(smoke.id(), smoke);
            }
        }
    }

    public static void upsert(long serverGameTime, SmokeVolumeData smoke) {
        updateTimeOffset(serverGameTime);
        if (smoke.valid()) {
            SMOKES.put(smoke.id(), smoke);
            while (SMOKES.size() > SmokeVolumeData.MAX_SNAPSHOT_SMOKES) {
                var iterator = SMOKES.keySet().iterator();
                iterator.next();
                iterator.remove();
            }
        }
    }

    public static void remove(long serverGameTime, UUID id) {
        updateTimeOffset(serverGameTime);
        SMOKES.remove(id);
    }

    public static void clear() {
        SMOKES.clear();
        serverTimeOffset = 0L;
    }

    public static long serverGameTime() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.level == null ? 0L : minecraft.level.getGameTime() - serverTimeOffset;
    }

    public static List<RenderedSmoke> nearest(Vec3 cameraPosition, float partialTick) {
        double now = serverGameTime() + partialTick;
        SMOKES.values().removeIf(smoke -> smoke.expiresGameTime() <= now);
        List<RenderedSmoke> result = new ArrayList<>(SMOKES.size());
        for (SmokeVolumeData smoke : SMOKES.values()) {
            float radius = effectiveRadius(smoke, now);
            Vec3 center = new Vec3(smoke.x(), smoke.y(), smoke.z());
            double surfaceDistance = Math.max(0.0D, cameraPosition.distanceTo(center) - radius);
            result.add(new RenderedSmoke(smoke, center, radius, surfaceDistance));
        }
        result.sort(Comparator.comparingDouble(RenderedSmoke::surfaceDistance));
        if (result.size() > MAX_RENDERED_SMOKES) {
            return List.copyOf(result.subList(0, MAX_RENDERED_SMOKES));
        }
        return List.copyOf(result);
    }

    public static List<SmokeVolumeData> allActive() {
        long now = serverGameTime();
        SMOKES.values().removeIf(smoke -> smoke.expiresGameTime() <= now);
        return List.copyOf(SMOKES.values());
    }

    static float effectiveRadius(SmokeVolumeData smoke, double gameTime) {
        if (smoke.deployTicks() <= 0) {
            return smoke.radius();
        }
        double progress = Mth.clamp((gameTime - smoke.createdGameTime()) / smoke.deployTicks(), 0.0D, 1.0D);
        double eased = 1.0D - Math.pow(1.0D - progress, 3.0D);
        return (float) Math.max(0.6D, smoke.radius() * eased);
    }

    private static void updateTimeOffset(long serverGameTime) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            serverTimeOffset = minecraft.level.getGameTime() - serverGameTime;
        }
    }

    public record RenderedSmoke(SmokeVolumeData data, Vec3 center, float radius, double surfaceDistance) {
    }
}
