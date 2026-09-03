package com.grenadier.smoke;

import com.grenadier.GrenadierConfig;
import com.grenadier.GrenadierMod;
import com.grenadier.network.SmokeFogPayload;
import com.grenadier.network.SmokeVolumeData;
import com.grenadier.network.SmokeVolumeRemovePayload;
import com.grenadier.network.SmokeVolumeSnapshotPayload;
import com.grenadier.network.SmokeVolumeUpsertPayload;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class SmokeCloudService {
    public static final SmokeCloudService INSTANCE = new SmokeCloudService();
    public static final TagKey<EntityType<?>> SMOKE_IMMUNE = TagKey.create(
            Registries.ENTITY_TYPE, GrenadierMod.path("smoke_immune"));
    public static final TagKey<EntityType<?>> SMOKE_ALWAYS_AFFECTED = TagKey.create(
            Registries.ENTITY_TYPE, GrenadierMod.path("smoke_always_affected"));

    private final Map<ResourceKey<Level>, Deque<SmokeCloud>> clouds = new HashMap<>();
    private final Map<UUID, Integer> obscuredTicks = new HashMap<>();
    private int ticks;

    private SmokeCloudService() {
    }

    public synchronized SmokeCloud activate(ServerLevel level, Vec3 center, int color) {
        return this.activate(level, center, color, true);
    }

    public synchronized SmokeCloud activate(ServerLevel level, Vec3 center, int color, boolean allowCascade) {
        long now = level.getGameTime();
        double radius = GrenadierConfig.RADIUS.get();
        SmokeCascade cascade = allowCascade
                ? SmokeCascadeDetector.detect(level, center, radius)
                : SmokeCascade.NONE;
        SmokeCloud cloud = new SmokeCloud(UUID.randomUUID(), level.dimension(), center,
                radius, GrenadierConfig.SMOKE_DENSITY.get(), now,
                now + GrenadierConfig.DURATION_SECONDS.get() * 20L, color, cascade);
        Deque<SmokeCloud> dimensionClouds = this.clouds.computeIfAbsent(level.dimension(), ignored -> new ArrayDeque<>());
        dimensionClouds.addLast(cloud);
        while (dimensionClouds.size() > GrenadierConfig.MAX_CLOUDS_PER_DIMENSION.get()) {
            SmokeCloud removed = dimensionClouds.removeFirst();
            this.sendRemove(level, removed.id(), now);
        }
        this.spawnBurst(level, cloud);
        PacketDistributor.sendToPlayersInDimension(level, new SmokeVolumeUpsertPayload(
                now,
                SmokeVolumeData.fromCloud(cloud, GrenadierConfig.DEPLOY_TICKS.get())
        ));
        return cloud;
    }

    public synchronized List<SmokeCloud> activeClouds(ResourceKey<Level> level) {
        Deque<SmokeCloud> dimensionClouds = this.clouds.get(level);
        return dimensionClouds == null ? List.of() : List.copyOf(dimensionClouds);
    }

    @SubscribeEvent
    public void onTargetChange(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || mob.level().isClientSide()) {
            return;
        }
        LivingEntity target = event.getNewAboutToBeSetTarget();
        if (target == null || !this.affected(mob) || this.closeEnough(mob, target) || this.recentlyHurtBy(mob, target)) {
            return;
        }
        if (this.occluded(mob, target)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        this.ticks++;
        int interval = GrenadierConfig.CHECK_INTERVAL_TICKS.get();
        if (this.ticks % interval == 0) {
            this.tick(event.getServer(), interval);
        }
        if (this.ticks % 100 == 0) {
            for (net.minecraft.server.level.ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
                this.sendSnapshot(player);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            this.sendSnapshot(player);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            this.sendSnapshot(player);
        }
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player) {
            this.sendSnapshot(player);
        }
    }

    private void tick(MinecraftServer server, int interval) {
        Set<UUID> checked = new HashSet<>();
        Set<UUID> obscuredNow = new HashSet<>();
        for (ServerLevel level : server.getAllLevels()) {
            List<SmokeCloud> active = this.removeExpired(level);
            Set<UUID> foggedPlayers = new HashSet<>();
            for (SmokeCloud cloud : active) {
                this.affectPlayers(level, cloud, foggedPlayers);
                AABB scan = new AABB(cloud.center(), cloud.center()).inflate(cloud.radius() + 24.0D);
                for (Mob mob : level.getEntitiesOfClass(Mob.class, scan, candidate -> candidate.getTarget() != null)) {
                    if (!checked.add(mob.getUUID()) || !this.affected(mob)) {
                        continue;
                    }
                    LivingEntity target = mob.getTarget();
                    if (target != null && !this.closeEnough(mob, target) && !this.recentlyHurtBy(mob, target)
                            && this.occluded(mob, target, active)) {
                        obscuredNow.add(mob.getUUID());
                        int total = this.obscuredTicks.merge(mob.getUUID(), interval, Integer::sum);
                        if (total >= GrenadierConfig.TARGET_LOSS_TICKS.get()) {
                            mob.setTarget(null);
                            this.obscuredTicks.remove(mob.getUUID());
                        }
                    } else {
                        this.obscuredTicks.remove(mob.getUUID());
                    }
                }
            }
        }
        this.obscuredTicks.keySet().removeIf(id -> !obscuredNow.contains(id));
    }

    private void affectPlayers(ServerLevel level, SmokeCloud cloud, Set<UUID> foggedPlayers) {
        if (!GrenadierConfig.AFFECT_PLAYERS.get()) {
            return;
        }
        long gameTime = level.getGameTime();
        double radius = cloud.effectiveRadius(gameTime);
        double progress = cloud.deployProgress(gameTime);
        double drop = cloud.cascade().dropDistance() * progress;
        Vec3 lowerCenter = cascadePoolCenter(cloud, drop, progress);
        AABB bounds = new AABB(cloud.center(), lowerCenter).inflate(radius);
        for (net.minecraft.server.level.ServerPlayer player : level.getEntitiesOfClass(
                net.minecraft.server.level.ServerPlayer.class,
                bounds,
                candidate -> !candidate.isSpectator()
                        && this.pointInsideCloud(candidate.getEyePosition(), cloud, gameTime))) {
            if (foggedPlayers.add(player.getUUID())) {
                PacketDistributor.sendToPlayer(player, new SmokeFogPayload(14));
            }
        }
    }

    private synchronized List<SmokeCloud> removeExpired(ServerLevel level) {
        Deque<SmokeCloud> dimensionClouds = this.clouds.get(level.dimension());
        if (dimensionClouds == null) {
            return List.of();
        }
        long now = level.getGameTime();
        var iterator = dimensionClouds.iterator();
        while (iterator.hasNext()) {
            SmokeCloud cloud = iterator.next();
            if (cloud.expired(now)) {
                iterator.remove();
                this.sendRemove(level, cloud.id(), now);
            }
        }
        if (dimensionClouds.isEmpty()) {
            this.clouds.remove(level.dimension());
            return List.of();
        }
        return new ArrayList<>(dimensionClouds);
    }

    private synchronized void sendSnapshot(net.minecraft.server.level.ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        List<SmokeCloud> active = this.activeClouds(level.dimension());
        int start = Math.max(0, active.size() - SmokeVolumeSnapshotPayload.MAX_SMOKES);
        List<SmokeVolumeData> records = active.subList(start, active.size()).stream()
                .map(cloud -> SmokeVolumeData.fromCloud(cloud, GrenadierConfig.DEPLOY_TICKS.get()))
                .toList();
        PacketDistributor.sendToPlayer(player, new SmokeVolumeSnapshotPayload(level.getGameTime(), records));
    }

    private void sendRemove(ServerLevel level, UUID id, long serverGameTime) {
        PacketDistributor.sendToPlayersInDimension(level, new SmokeVolumeRemovePayload(serverGameTime, id));
    }

    private void spawnBurst(ServerLevel level, SmokeCloud cloud) {
        ColorParticleOption smoke = ColorParticleOption.create(GrenadierMod.COLORED_SIGNAL_SMOKE.get(), cloud.color());
        level.sendParticles(smoke, cloud.center().x, cloud.center().y + 0.35D, cloud.center().z,
                10, 0.55D, 0.28D, 0.55D, 0.055D);
        level.playSound(null, cloud.center().x, cloud.center().y, cloud.center().z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 0.7F, 1.55F);
    }

    private boolean affected(Mob mob) {
        if (mob.getType().is(SMOKE_IMMUNE)) {
            return false;
        }
        return mob.getType().is(SMOKE_ALWAYS_AFFECTED) || mob instanceof Enemy;
    }

    private boolean closeEnough(Mob mob, LivingEntity target) {
        double range = GrenadierConfig.CLOSE_DETECTION_RANGE.get();
        return mob.distanceToSqr(target) <= range * range;
    }

    private boolean recentlyHurtBy(Mob mob, LivingEntity target) {
        return mob.getLastHurtByMob() == target && mob.tickCount - mob.getLastHurtByMobTimestamp() <= 40;
    }

    private boolean occluded(Mob mob, LivingEntity target) {
        return this.occluded(mob, target, this.activeClouds(mob.level().dimension()));
    }

    private boolean occluded(Mob mob, LivingEntity target, List<SmokeCloud> active) {
        Vec3 start = mob.getEyePosition();
        Vec3 end = target.getEyePosition();
        long gameTime = mob.level().getGameTime();
        for (SmokeCloud cloud : active) {
            double radius = cloud.effectiveRadius(gameTime);
            double progress = cloud.deployProgress(gameTime);
            if (segmentIntersectsMainBody(start, end, cloud, radius, progress)) {
                return true;
            }
            if (cloud.cascade().active()) {
                double drop = cloud.cascade().dropDistance() * progress;
                if (segmentIntersectsCascade(start, end, cloud, progress, drop, radius)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean pointInsideCloud(Vec3 point, SmokeCloud cloud, long gameTime) {
        double radius = cloud.effectiveRadius(gameTime);
        double progress = cloud.deployProgress(gameTime);
        if (pointInsideMainBody(point, cloud, radius, progress)) {
            return true;
        }
        if (!cloud.cascade().active()) {
            return false;
        }
        double drop = cloud.cascade().dropDistance() * progress;
        Vec3 edge = cascadeEdge(cloud, progress);
        double width = cloud.cascade().curtainWidth() * progress * 1.20D;
        double thickness = cascadeThickness(cloud, progress, radius);
        Vec3 offset = point.subtract(edge);
        double downward = -offset.y;
        double lateral = -cloud.cascade().directionZ() * offset.x
                + cloud.cascade().directionX() * offset.z;
        double forward = cloud.cascade().directionX() * offset.x
                + cloud.cascade().directionZ() * offset.z;
        if (pointInsideCascadePlumes(
                cloud,
                gameTime,
                downward,
                lateral,
                forward,
                drop,
                width,
                thickness
        )) {
            return true;
        }
        double poolRadius = cloud.cascade().poolRadius() * progress * 1.08D;
        Vec3 poolCenter = cascadePoolCenter(cloud, drop, progress);
        return pointInsideCascadePool(point, poolCenter, cloud.cascade(), poolRadius);
    }

    private static boolean pointInsideMainBody(Vec3 point, SmokeCloud cloud, double radius, double progress) {
        Vec3 center = mainBodyCenter(cloud, radius, progress);
        double horizontalRadius = cloud.cascade().active() ? radius * 0.744D : radius;
        double verticalRadius = cloud.cascade().active() ? radius * 0.504D : radius * 0.66D;
        return pointInsideEllipsoid(
                point,
                center,
                horizontalRadius,
                verticalRadius,
                horizontalRadius
        );
    }

    private static boolean segmentIntersectsMainBody(
            Vec3 start,
            Vec3 end,
            SmokeCloud cloud,
            double radius,
            double progress
    ) {
        Vec3 center = mainBodyCenter(cloud, radius, progress);
        double horizontalRadius = cloud.cascade().active() ? radius * 0.744D : radius;
        double verticalRadius = cloud.cascade().active() ? radius * 0.504D : radius * 0.66D;
        return SmokeGeometry.segmentIntersectsEllipsoid(
                start,
                end,
                center,
                horizontalRadius,
                verticalRadius,
                horizontalRadius
        );
    }

    private static Vec3 mainBodyCenter(SmokeCloud cloud, double radius, double progress) {
        double verticalOffset = cloud.cascade().active() ? radius * 0.18D : radius * 0.30D;
        double cascadeDescent = cloud.cascade().active()
                ? cloud.cascade().dropDistance() * progress
                : 0.0D;
        return cloud.center().add(0.0D, verticalOffset - cascadeDescent, 0.0D);
    }

    private static Vec3 cascadeEdge(SmokeCloud cloud, double progress) {
        return cloud.center();
    }

    private static boolean segmentIntersectsCascade(
            Vec3 start,
            Vec3 end,
            SmokeCloud cloud,
            double progress,
            double drop,
            double radius
    ) {
        SmokeCascade cascade = cloud.cascade();
        Vec3 edge = cascadeEdge(cloud, progress);
        double width = cascade.curtainWidth() * progress * 1.20D;
        double thickness = cascadeThickness(cloud, progress, radius);
        Vec3 localStart = curtainLocal(start, edge, cascade, drop, thickness);
        Vec3 localEnd = curtainLocal(end, edge, cascade, drop, thickness);
        if (SmokeGeometry.segmentIntersectsAabb(
                localStart,
                localEnd,
                width * 0.52D,
                drop * 0.5D + 0.42D,
                width * 0.52D
        )) {
            return true;
        }
        double poolRadius = cascade.poolRadius() * progress * 1.08D;
        double poolHeight = Math.max(0.28D, poolRadius * 0.14D);
        double tangentX = -cascade.directionZ();
        double tangentZ = cascade.directionX();
        Vec3 poolCenter = cascadePoolCenter(cloud, drop, progress);
        if (SmokeGeometry.segmentIntersectsEllipsoid(
                start,
                end,
                poolCenter,
                poolRadius * 0.72D,
                poolHeight,
                poolRadius * 0.76D
        )) {
            return true;
        }
        Vec3 left = poolCenter.add(tangentX * poolRadius * 0.42D, 0.04D, tangentZ * poolRadius * 0.42D);
        Vec3 right = poolCenter.add(-tangentX * poolRadius * 0.38D, 0.02D, -tangentZ * poolRadius * 0.38D);
        return SmokeGeometry.segmentIntersectsEllipsoid(
                start, end, left, poolRadius * 0.48D, poolHeight * 0.86D, poolRadius * 0.52D
        ) || SmokeGeometry.segmentIntersectsEllipsoid(
                start, end, right, poolRadius * 0.52D, poolHeight * 0.92D, poolRadius * 0.46D
        );
    }

    private static Vec3 curtainLocal(
            Vec3 point,
            Vec3 edge,
            SmokeCascade cascade,
            double drop,
            double thickness
    ) {
        Vec3 offset = point.subtract(edge);
        double lateral = -cascade.directionZ() * offset.x + cascade.directionX() * offset.z;
        double forward = cascade.directionX() * offset.x + cascade.directionZ() * offset.z;
        return new Vec3(lateral, offset.y + drop * 0.5D, forward);
    }

    private static double cascadeThickness(SmokeCloud cloud, double progress, double radius) {
        double width = cloud.cascade().curtainWidth() * progress * 1.20D;
        return Math.min(Math.max(width * 0.14D, 0.78D), Math.max(radius * 0.32D, 0.08D));
    }

    private static boolean pointInsideCascadePlumes(
            SmokeCloud cloud,
            long gameTime,
            double downward,
            double lateral,
            double forward,
            double drop,
            double width,
            double thickness
    ) {
        if (drop <= 0.0D || downward < -0.65D || downward > drop + 0.52D) {
            return false;
        }
        double descent = Math.max(0.0D, Math.min(1.0D, downward / drop));
        double seedPhase = cascadePhase(cloud);
        double breathing = 0.96D
                + 0.10D * Math.sin(downward * 0.82D + seedPhase + gameTime * 0.010D)
                + 0.045D * Math.sin(downward * 1.91D - seedPhase * 0.73D);
        double lowerExpansion = 0.94D + 0.14D * descent;
        double volumeRadius = width * 0.40D * breathing * lowerExpansion;
        double lateralDistance = lateral / Math.max(volumeRadius, 0.001D);
        double depthDistance = forward / Math.max(volumeRadius, 0.001D);
        return lateralDistance * lateralDistance + depthDistance * depthDistance <= 1.0D;
    }

    private static boolean pointInsideCascadePool(
            Vec3 point,
            Vec3 center,
            SmokeCascade cascade,
            double poolRadius
    ) {
        double poolHeight = Math.max(0.28D, poolRadius * 0.14D);
        if (pointInsideEllipsoid(point, center, poolRadius * 0.72D, poolHeight, poolRadius * 0.76D)) {
            return true;
        }
        double tangentX = -cascade.directionZ();
        double tangentZ = cascade.directionX();
        Vec3 left = center.add(tangentX * poolRadius * 0.42D, 0.04D, tangentZ * poolRadius * 0.42D);
        Vec3 right = center.add(-tangentX * poolRadius * 0.38D, 0.02D, -tangentZ * poolRadius * 0.38D);
        return pointInsideEllipsoid(point, left, poolRadius * 0.48D, poolHeight * 0.86D, poolRadius * 0.52D)
                || pointInsideEllipsoid(point, right, poolRadius * 0.52D, poolHeight * 0.92D, poolRadius * 0.46D);
    }

    private static boolean pointInsideEllipsoid(
            Vec3 point,
            Vec3 center,
            double radiusX,
            double radiusY,
            double radiusZ
    ) {
        double x = (point.x - center.x) / Math.max(radiusX, 0.001D);
        double y = (point.y - center.y) / Math.max(radiusY, 0.001D);
        double z = (point.z - center.z) / Math.max(radiusZ, 0.001D);
        return x * x + y * y + z * z <= 1.0D;
    }

    private static double cascadePhase(SmokeCloud cloud) {
        long seedBits = cloud.id().getMostSignificantBits() ^ cloud.id().getLeastSignificantBits();
        return (seedBits & 0xFFFFL) / 65535.0D * Math.PI * 2.0D;
    }

    private static Vec3 cascadePoolCenter(SmokeCloud cloud, double drop, double progress) {
        SmokeCascade cascade = cloud.cascade();
        Vec3 edge = cascadeEdge(cloud, progress);
        double radius = cloud.radius() * progress;
        double thickness = cascadeThickness(cloud, progress, radius);
        double poolRadius = cascade.poolRadius() * progress * 1.08D;
        return edge.add(
                0.0D,
                -drop + Math.max(0.22D, poolRadius * 0.12D),
                0.0D
        );
    }
}
