package com.grenadier.incendiary;

import com.grenadier.GrenadierConfig;
import com.grenadier.GrenadierMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class IncendiaryFieldEntity extends AreaEffectCloud {
    private static final int VISUAL_PATCHES = 14;

    public IncendiaryFieldEntity(EntityType<? extends IncendiaryFieldEntity> entityType, Level level) {
        super(entityType, level);
    }

    public IncendiaryFieldEntity(ServerLevel level, Vec3 center, LivingEntity owner) {
        this(GrenadierMod.INCENDIARY_FIELD.get(), level);
        this.setPos(center);
        this.setOwner(owner);
        this.setRadius(GrenadierConfig.INCENDIARY_RADIUS.get().floatValue());
        this.setDuration(GrenadierConfig.INCENDIARY_DURATION_SECONDS.get() * 20);
        this.setWaitTime(0);
        this.setRadiusPerTick(0.0F);
        this.setRadiusOnUse(0.0F);
        this.setDurationOnUse(0);
        this.setParticle(GrenadierMod.INCENDIARY_FLAME.get());
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) {
            return;
        }
        if (this.level().isClientSide) {
            this.spawnClientFlames();
            return;
        }

        ServerLevel level = (ServerLevel) this.level();
        BlockPos center = this.blockPosition();
        if (level.getFluidState(center).is(net.minecraft.tags.FluidTags.WATER)) {
            this.discard();
            return;
        }
        if (level.isRainingAt(center.above())) {
            int normalDuration = GrenadierConfig.INCENDIARY_DURATION_SECONDS.get() * 20;
            this.setDuration(Math.min(this.getDuration(), IncendiaryWeatherRules.rainDurationTicks(normalDuration)));
        }
        int interval = GrenadierConfig.INCENDIARY_CHECK_INTERVAL_TICKS.get();
        if (this.tickCount % interval == 0) {
            this.burnNearby(level);
        }
    }

    private void burnNearby(ServerLevel level) {
        float radius = this.getRadius();
        AABB bounds = new AABB(
                this.getX() - radius, this.getY() - 1.0D, this.getZ() - radius,
                this.getX() + radius, this.getY() + 2.75D, this.getZ() + radius
        );
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive)) {
            if (target.fireImmune() || target.isInWaterOrBubble() || !this.insideFootprint(target, radius)
                    || !this.hasClearPath(level, target)) {
                continue;
            }
            target.hurt(level.damageSources().inFire(), GrenadierConfig.INCENDIARY_DAMAGE_PER_PULSE.get().floatValue());
            int igniteSeconds = GrenadierConfig.INCENDIARY_IGNITE_SECONDS.get();
            boolean exposedToRain = target.isInWaterRainOrBubble();
            if (igniteSeconds > 0 && IncendiaryWeatherRules.canIgnite(exposedToRain)) {
                target.igniteForSeconds(igniteSeconds);
            }
        }
    }

    private boolean insideFootprint(LivingEntity target, float radius) {
        double dx = target.getX() - this.getX();
        double dz = target.getZ() - this.getZ();
        double vertical = target.getY() - this.getY();
        if (vertical < -1.0D || vertical > 2.75D) {
            return false;
        }
        double angle = Math.atan2(dz, dx);
        double seedPhase = this.getId() * 0.37D;
        double edge = radius * (0.83D + 0.11D * Math.sin(angle * 3.0D + seedPhase)
                + 0.06D * Math.sin(angle * 5.0D - seedPhase * 0.7D));
        return dx * dx + dz * dz <= edge * edge;
    }

    private boolean hasClearPath(ServerLevel level, LivingEntity target) {
        Vec3 start = this.position().add(0.0D, 0.38D, 0.0D);
        Vec3 end = target.getEyePosition();
        HitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        return hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceToSqr(end) < 0.36D;
    }

    private void spawnClientFlames() {
        Player viewer = this.level().getNearestPlayer(this, 64.0D);
        if (viewer == null) {
            return;
        }
        boolean distant = viewer.distanceToSqr(this) > 48.0D * 48.0D;
        int interval = distant ? 4 : 2;
        if (this.tickCount % interval != 0) {
            return;
        }
        int count = distant ? 1 : 3;
        for (int sample = 0; sample < count; sample++) {
            int patch = Mth.positiveModulo(this.tickCount / interval * count + sample, VISUAL_PATCHES);
            Vec3 point = this.patchPosition(patch);
            double flicker = 0.02D + this.random.nextDouble() * 0.035D;
            this.level().addParticle(GrenadierMod.INCENDIARY_FLAME.get(), point.x, point.y, point.z,
                    0.0D, flicker, 0.0D);
            if ((patch + this.tickCount) % 4 == 0) {
                this.level().addParticle(ParticleTypes.LARGE_SMOKE, point.x, point.y + 0.18D, point.z,
                        0.0D, 0.018D, 0.0D);
            }
        }
    }

    private Vec3 patchPosition(int patch) {
        long seed = (long) this.getId() * 0x9E3779B97F4A7C15L + (long) patch * 0xC2B2AE3D27D4EB4FL;
        RandomSource patchRandom = RandomSource.create(seed);
        double angle = patchRandom.nextDouble() * Mth.TWO_PI;
        double distance = Math.sqrt(patchRandom.nextDouble()) * this.getRadius() * 0.88D;
        double x = this.getX() + Math.cos(angle) * distance;
        double z = this.getZ() + Math.sin(angle) * distance;
        double y = findVisualGround(this.level(), x, this.getY(), z);
        return new Vec3(x, y + 0.06D, z);
    }

    private static double findVisualGround(Level level, double x, double originY, double z) {
        int blockX = Mth.floor(x);
        int blockZ = Mth.floor(z);
        int startY = Mth.floor(originY + 2.0D);
        for (int y = startY; y >= startY - 5; y--) {
            BlockPos feet = new BlockPos(blockX, y, blockZ);
            BlockPos below = feet.below();
            BlockState floor = level.getBlockState(below);
            if (floor.isFaceSturdy(level, below, Direction.UP)
                    && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()) {
                return y;
            }
        }
        return originY;
    }
}
