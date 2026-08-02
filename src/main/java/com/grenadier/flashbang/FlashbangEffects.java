package com.grenadier.flashbang;

import com.grenadier.network.FlashbangPayload;
import com.grenadier.GrenadierConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public final class FlashbangEffects {
    private FlashbangEffects() {
    }

    public static void detonate(ServerLevel level, Vec3 center, Entity owner) {
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(ParticleTypes.END_ROD, center.x, center.y, center.z, 18, 0.45D, 0.45D, 0.45D, 0.09D);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, 0.82F, 1.65F);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.FIREWORK_ROCKET_BLAST,
                SoundSource.PLAYERS, 0.72F, 1.8F);

        double radius = GrenadierConfig.FLASHBANG_RADIUS.get();
        AABB bounds = new AABB(center, center).inflate(radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive)) {
            double intensity = exposure(level, center, target, radius);
            if (intensity < 0.08D) {
                continue;
            }
            if (target instanceof ServerPlayer player) {
                flashPlayer(player, intensity);
            } else if (target instanceof Mob mob) {
                stunMob(mob, intensity, owner);
            }
        }
    }

    static double exposure(ServerLevel level, Vec3 center, LivingEntity target, double radius) {
        Vec3 eye = target.getEyePosition();
        double distance = eye.distanceTo(center);
        if (distance > radius || !hasClearPath(level, center, target, eye)) {
            return 0.0D;
        }
        double distanceFactor = Mth.clamp(1.0D - distance / radius, 0.0D, 1.0D);
        Vec3 towardFlash = center.subtract(eye).normalize();
        double facingDot = target.getViewVector(1.0F).dot(towardFlash);
        double facingFactor = Mth.clamp((facingDot + 0.35D) / 1.0D, 0.18D, 1.0D);
        double closeFactor = Mth.clamp(1.0D - distance / 4.0D, 0.0D, 1.0D) * 0.72D;
        return Mth.clamp(Math.pow(distanceFactor, 0.72D) * Math.max(facingFactor, closeFactor) * 1.35D, 0.0D, 1.0D);
    }

    private static boolean hasClearPath(ServerLevel level, Vec3 center, LivingEntity target, Vec3 eye) {
        HitResult hit = level.clip(new ClipContext(center, eye, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, target));
        return hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceToSqr(eye) < 0.36D;
    }

    private static void flashPlayer(ServerPlayer player, double intensity) {
        int maxWhite = GrenadierConfig.FLASHBANG_MAX_WHITE_TICKS.get();
        int maxFade = GrenadierConfig.FLASHBANG_MAX_FADE_TICKS.get();
        int minFade = Math.min(8, maxFade);
        int whiteTicks = intensity <= 0.30D ? 0
                : Mth.clamp((int) Math.round((intensity - 0.30D) / 0.70D * maxWhite), 0, maxWhite);
        int fadeTicks = Mth.clamp((int) Math.round(minFade + intensity * (maxFade - minFade)), minFade, maxFade);
        PacketDistributor.sendToPlayer(player, new FlashbangPayload(
                Mth.clamp((int) Math.round(intensity * 1000.0D), 0, 1000), whiteTicks, fadeTicks));
    }

    private static void stunMob(Mob mob, double intensity, Entity owner) {
        int maxStun = GrenadierConfig.FLASHBANG_MAX_MOB_STUN_TICKS.get();
        if (maxStun <= 0) {
            return;
        }
        int duration = Mth.clamp((int) Math.round(intensity * maxStun), Math.min(4, maxStun), maxStun);
        mob.setTarget(null);
        mob.getNavigation().stop();
        mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 4,
                false, false, false), owner);
        mob.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 2,
                false, false, false), owner);
    }
}
