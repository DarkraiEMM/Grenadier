package com.grenadier.mine;

import com.grenadier.GrenadierConfig;
import com.grenadier.GrenadierMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;
import org.joml.Vector3f;

public final class ThermiteBurstEntity extends Entity {
    private static final DustColorTransitionOptions MAGNESIUM_DUST = new DustColorTransitionOptions(
            new Vector3f(1.0F, 0.98F, 0.78F), new Vector3f(1.0F, 0.28F, 0.015F), 1.35F);
    private UUID ownerUuid;
    private int age;

    public ThermiteBurstEntity(EntityType<? extends ThermiteBurstEntity> type, Level level) { super(type, level); }

    public ThermiteBurstEntity(ServerLevel level, Vec3 center, LivingEntity owner) {
        this(GrenadierMod.THERMITE_BURST.get(), level);
        setPos(center);
        ownerUuid = owner == null ? null : owner.getUUID();
    }

    public static void emitIgnition(ServerLevel level, Vec3 center) {
        // The ignition is deliberately layered: a one-tick white-hot core, a pressure ring,
        // then fast incandescent fragments. Large lingering flame is only the supporting layer.
        level.sendParticles(ParticleTypes.FLASH, center.x, center.y + 0.3D, center.z,
                3, 0.12D, 0.08D, 0.12D, 0.0D);
        level.sendParticles(ParticleTypes.GUST_EMITTER_SMALL, center.x, center.y + 0.08D, center.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        emitIgnitionCone(level, center, 54, 0.18D, 0.62D, GrenadierMod.THERMITE_SPARK.get());
        emitIgnitionCone(level, center, 30, 0.30D, 0.48D, GrenadierMod.THERMITE_SPARK.get());
        emitIgnitionCone(level, center, 18, 0.14D, 0.42D, ParticleTypes.LAVA);
        level.sendParticles(GrenadierMod.INCENDIARY_FLAME.get(), center.x, center.y + 0.2D, center.z,
                18, 0.42D, 0.16D, 0.42D, 0.11D);
        level.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.18D, center.z,
                16, 0.6D, 0.2D, 0.6D, 0.12D);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.FIREWORK_ROCKET_LARGE_BLAST,
                SoundSource.BLOCKS, 1.45F, 0.72F);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.FIRECHARGE_USE,
                SoundSource.BLOCKS, 1.7F, 0.58F);
    }

    private static void emitIgnitionCone(ServerLevel level, Vec3 center, int count,
                                         double horizontalSpeed, double verticalSpeed,
                                         net.minecraft.core.particles.ParticleOptions particle) {
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2.0D * i / count;
            double spread = horizontalSpeed * (0.55D + (i % 6) * 0.11D);
            double lift = verticalSpeed * (0.72D + (i % 5) * 0.12D);
            level.sendParticles(particle, center.x, center.y + 0.16D, center.z,
                    0, Math.cos(angle) * spread, lift,
                    Math.sin(angle) * spread, 1.0D);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            spawnClientSpray();
            return;
        }
        if (!(level() instanceof ServerLevel level)) return;
        age++;
        int burstTicks = GrenadierConfig.THERMITE_MINE_BURST_TICKS.get();
        int totalTicks = burstTicks + GrenadierConfig.THERMITE_MINE_RESIDUAL_TICKS.get();
        boolean burst = age <= burstTicks;
        emitSpray(level, burst);
        if (age >= 6 && age % 4 == 0) level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.4D, getZ(),
                1, 0.55D, 0.24D, 0.55D, 0.025D);
        if (age % 5 == 0) level.sendParticles(ParticleTypes.ASH, getX(), getY() + 0.22D, getZ(),
                burst ? 5 : 2, 0.9D, 0.2D, 0.9D, 0.02D);
        if (age == 1 || age % GrenadierConfig.THERMITE_MINE_DAMAGE_INTERVAL.get() == 0) burn(level, burst);
        if (age >= totalTicks) discard();
    }

    /**
     * Spawn the core visual directly on tracking clients. This matches the working incendiary-field
     * path and prevents small vanilla particles being reduced to an unreadable smoke-only effect.
     */
    private void spawnClientSpray() {
        int burstTicks = GrenadierConfig.THERMITE_MINE_BURST_TICKS.get();
        int totalTicks = burstTicks + GrenadierConfig.THERMITE_MINE_RESIDUAL_TICKS.get();
        if (tickCount > totalTicks) return;

        boolean burst = tickCount <= burstTicks;
        if (tickCount <= 9 && tickCount % 3 == 1) {
            level().addParticle(ParticleTypes.FLASH, getX(), getY() + 0.3D, getZ(),
                    0.0D, 0.0D, 0.0D);
        }
        int sprayCount = burst ? (tickCount <= 12 ? 16 : 8) : 2;
        double phase = tickCount * 0.51D;
        for (int i = 0; i < sprayCount; i++) {
            double angle = phase + Math.PI * 2.0D * i / sprayCount
                    + (random.nextDouble() - 0.5D) * 0.18D;
            double radialSpeed = burst
                    ? 0.10D + random.nextDouble() * 0.20D
                    : 0.12D + random.nextDouble() * 0.16D;
            double spawnRadius = random.nextDouble() * (burst ? 0.12D : 0.18D);
            double sx = getX() + Math.cos(angle) * spawnRadius;
            double sz = getZ() + Math.sin(angle) * spawnRadius;
            double sy = getY() + 0.18D + random.nextDouble() * 0.12D;
            double vx = Math.cos(angle) * radialSpeed;
            double vz = Math.sin(angle) * radialSpeed;
            double vy = burst ? 0.30D + random.nextDouble() * 0.34D : 0.025D;

            if (burst) {
                level().addParticle(GrenadierMod.THERMITE_SPARK.get(), sx, sy + 0.06D, sz,
                        vx, vy, vz);
                if (i % 4 == 0) {
                    level().addParticle(GrenadierMod.INCENDIARY_FLAME.get(), sx, sy, sz,
                            vx * 0.28D, vy * 0.65D, vz * 0.28D);
                }
            } else {
                level().addParticle(GrenadierMod.INCENDIARY_FLAME.get(), sx, sy, sz, vx, vy, vz);
            }
            if (burst && i % 2 == 0) {
                level().addParticle(ParticleTypes.LAVA, sx, sy + 0.05D, sz,
                        vx * 0.42D, vy * 1.15D, vz * 0.42D);
            }
            if (burst && i % 5 == 0) {
                level().addParticle(ParticleTypes.FALLING_LAVA, sx, sy + 0.12D, sz,
                        vx * 0.08D, 0.02D, vz * 0.08D);
            }
        }
        if (tickCount >= 6 && tickCount % 6 == 0) {
            level().addParticle(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.42D, getZ(),
                    0.0D, 0.025D, 0.0D);
        }
    }

    private void emitSpray(ServerLevel level, boolean burst) {
        int jets = burst ? 16 : 3;
        double phase = age * 0.47D;
        for (int i = 0; i < jets; i++) {
            double angle = phase + Math.PI * 2.0D * i / jets;
            double speed = burst ? 0.09D + (i % 4) * 0.045D : 0.045D;
            double vx = Math.cos(angle) * speed;
            double vz = Math.sin(angle) * speed;
            double vy = burst ? 0.32D + (i % 4) * 0.075D : 0.035D;
            level.sendParticles(burst ? GrenadierMod.THERMITE_SPARK.get() : ParticleTypes.SMALL_FLAME,
                    getX(), getY() + 0.18D, getZ(),
                    0, vx, vy, vz, 1.0D);
            if (burst && i % 2 == 0) {
                level.sendParticles(ParticleTypes.LAVA, getX(), getY() + 0.22D, getZ(),
                        0, vx * 0.68D, vy * 1.15D, vz * 0.68D, 1.0D);
            }
        }
        level.sendParticles(burst ? GrenadierMod.THERMITE_SPARK.get() : ParticleTypes.SMALL_FLAME,
                getX(), getY() + 0.14D, getZ(), burst ? 5 : 2,
                burst ? 0.5D : 0.32D, 0.15D, burst ? 0.5D : 0.32D, 0.025D);
    }

    private void burn(ServerLevel level, boolean burst) {
        double radius = GrenadierConfig.THERMITE_MINE_FLAME_RADIUS.get();
        AABB bounds = getBoundingBox().inflate(radius, 1.5D, radius);
        LivingEntity owner = ownerUuid == null ? null : level.getServer().getPlayerList().getPlayer(ownerUuid);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive)) {
            if (!GrenadierConfig.MINES_DAMAGE_OWNER.get() && ownerUuid != null && ownerUuid.equals(target.getUUID())) continue;
            double dx = target.getX() - getX();
            double dz = target.getZ() - getZ();
            if (dx * dx + dz * dz > radius * radius || !MineEffects.hasClearPath(level, position().add(0, 0.25D, 0), target.getEyePosition(), this)) continue;
            float damage = GrenadierConfig.THERMITE_MINE_FIRE_DAMAGE.get().floatValue() * (burst ? 1.0F : 0.55F);
            if (target.getType().is(MineEffects.HEAVY_TARGETS)) damage += GrenadierConfig.THERMITE_MINE_HEAVY_BONUS_DAMAGE.get().floatValue();
            target.hurt(level.damageSources().inFire(), damage);
            target.igniteForSeconds(GrenadierConfig.THERMITE_MINE_IGNITE_SECONDS.get());
        }
    }

    @Override protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) { }
    @Override protected void readAdditionalSaveData(CompoundTag tag) { ownerUuid = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null; age = tag.getInt("Age"); }
    @Override protected void addAdditionalSaveData(CompoundTag tag) { if (ownerUuid != null) tag.putUUID("Owner", ownerUuid); tag.putInt("Age", age); }
}
