package com.grenadier.mine;

import com.grenadier.GrenadierConfig;
import com.grenadier.GrenadierMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class MineEffects {
    public static final TagKey<EntityType<?>> HEAVY_TARGETS = TagKey.create(
            Registries.ENTITY_TYPE, GrenadierMod.path("mine_heavy_targets"));

    private MineEffects() {
    }

    public static void trigger(ServerLevel level, BlockPos pos, BlockState state, MineKind kind, LivingEntity owner) {
        Vec3 center = Vec3.atBottomCenterOf(pos).add(0.0D, 0.18D, 0.0D);
        level.removeBlock(pos, false);
        switch (kind) {
            case ANTI_PERSONNEL -> explodeAntiPersonnel(level, center, owner);
            case DIRECTIONAL -> {
                Direction facing = state.getValue(DirectionalMineBlock.FACING);
                triggerDirectional(level, center, new Vec3(facing.getStepX(), 0.0D, facing.getStepZ()), owner);
            }
            case THERMITE -> triggerThermite(level, center, owner);
        }
    }

    public static void triggerDeployed(ServerLevel level, Vec3 position, MineKind kind,
                                       float yaw, LivingEntity owner) {
        Vec3 center = position.add(0.0D, 0.18D, 0.0D);
        switch (kind) {
            case ANTI_PERSONNEL -> explodeAntiPersonnel(level, center, owner);
            case DIRECTIONAL -> triggerDirectional(level, center,
                    Vec3.directionFromRotation(0.0F, yaw), owner);
            case THERMITE -> triggerThermite(level, center, owner);
        }
    }

    private static void triggerDirectional(ServerLevel level, Vec3 center, Vec3 forward, LivingEntity owner) {
        explode(level, center, GrenadierConfig.DIRECTIONAL_MINE_BACKBLAST_RADIUS.get().floatValue(),
                GrenadierConfig.DIRECTIONAL_MINE_DESTROY_BLOCKS.get());
        double range = GrenadierConfig.DIRECTIONAL_MINE_RANGE.get();
        double width = GrenadierConfig.DIRECTIONAL_MINE_WIDTH.get();
        AABB bounds = new AABB(center, center).inflate(range, 2.0D, range);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive)) {
            if (!MineMath.isInHorizontalCone(center, forward, target.position(), range, width,
                    GrenadierConfig.DIRECTIONAL_MINE_HALF_ANGLE.get()) || !hasClearPath(level, center, target.getEyePosition(), target)) {
                continue;
            }
            double distanceFactor = Math.max(0.2D, 1.0D - Math.sqrt(target.distanceToSqr(center)) / range);
            double maximumDamage = GrenadierConfig.DIRECTIONAL_MINE_DAMAGE.get();
            if (target.getType().is(HEAVY_TARGETS)) {
                maximumDamage += GrenadierConfig.DIRECTIONAL_MINE_HEAVY_BONUS_DAMAGE.get();
            }
            target.hurt(level.damageSources().explosion(owner, null), (float) (maximumDamage * distanceFactor));
        }
        level.sendParticles(ParticleTypes.CRIT, center.x, center.y + 0.2D, center.z, 42,
                Math.abs(forward.z) * 1.5D + 0.2D, 0.55D,
                Math.abs(forward.x) * 1.5D + 0.2D, 0.28D);
    }

    private static void triggerThermite(ServerLevel level, Vec3 center, LivingEntity owner) {
        damageThermiteBurst(level, center, owner);
        ThermiteBurstEntity.emitIgnition(level, center);
        ThermiteBurstEntity burst = new ThermiteBurstEntity(level, center, owner);
        if (!level.addFreshEntity(burst)) {
            GrenadierMod.LOGGER.error("Failed to spawn thermite burst at {}", center);
        }
    }

    private static void damageThermiteBurst(ServerLevel level, Vec3 center, LivingEntity owner) {
        double radius = GrenadierConfig.THERMITE_MINE_EXPLOSION_RADIUS.get();
        double coreHalfWidth = GrenadierConfig.THERMITE_MINE_CORE_HALF_WIDTH.get();
        AABB bounds = new AABB(center, center).inflate(radius, radius, radius);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive)) {
            if (!GrenadierConfig.MINES_DAMAGE_OWNER.get() && owner != null && owner.getUUID().equals(target.getUUID())) {
                continue;
            }
            double distance = target.position().distanceTo(center);
            if (distance > radius || !hasClearPath(level, center, target.getEyePosition(), target)) {
                continue;
            }
            double dx = target.getX() - center.x;
            double dz = target.getZ() - center.z;
            boolean coreHit = MineGeometry.isInsideSquare(dx, dz, coreHalfWidth);
            float damage = coreHit
                    ? GrenadierConfig.THERMITE_MINE_BURST_DAMAGE.get().floatValue()
                    : GrenadierConfig.THERMITE_MINE_SPLASH_DAMAGE.get().floatValue();
            if (target.getType().is(HEAVY_TARGETS)) {
                damage += coreHit
                        ? GrenadierConfig.THERMITE_MINE_BURST_HEAVY_BONUS_DAMAGE.get().floatValue()
                        : GrenadierConfig.THERMITE_MINE_SPLASH_HEAVY_BONUS_DAMAGE.get().floatValue();
            }
            hurtWithPartialArmorPiercing(target, level.damageSources().explosion(owner, null), damage,
                    GrenadierConfig.THERMITE_MINE_ARMOR_PIERCE_RATIO.get().floatValue());
            if (!coreHit) {
                target.igniteForSeconds(GrenadierConfig.THERMITE_MINE_SPLASH_IGNITE_SECONDS.get());
            }
        }
    }

    private static void explodeAntiPersonnel(ServerLevel level, Vec3 center, LivingEntity owner) {
        ExplosionDamageCalculator calculator = new ExplosionDamageCalculator() {
            @Override
            public float getEntityDamageAmount(Explosion explosion, Entity entity) {
                float damage = super.getEntityDamageAmount(explosion, entity);
                return entity.getType().is(HEAVY_TARGETS)
                        ? damage + GrenadierConfig.ANTI_PERSONNEL_MINE_HEAVY_BONUS_DAMAGE.get().floatValue()
                        : damage;
            }
        };
        level.explode(null, level.damageSources().explosion(owner, null), calculator, center,
                GrenadierConfig.ANTI_PERSONNEL_MINE_RADIUS.get().floatValue(), false,
                GrenadierConfig.ANTI_PERSONNEL_MINE_DESTROY_BLOCKS.get()
                        ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);
    }

    private static void hurtWithPartialArmorPiercing(LivingEntity target, DamageSource source,
                                                       float damage, float armorPierceRatio) {
        float armor = target.getArmorValue();
        float toughness = (float) target.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
        float fullyArmored = CombatRules.getDamageAfterAbsorb(target, damage, source, armor, toughness);
        float desiredAfterArmor = fullyArmored + (damage - fullyArmored) * armorPierceRatio;

        // LivingEntity will run the ordinary armor calculation once more in hurt(). Find the
        // incoming value that produces the blended result, retaining armor wear and normal hooks.
        float low = desiredAfterArmor;
        float high = Math.max(damage * 5.0F, desiredAfterArmor);
        for (int i = 0; i < 14; i++) {
            float candidate = (low + high) * 0.5F;
            float result = CombatRules.getDamageAfterAbsorb(target, candidate, source, armor, toughness);
            if (result < desiredAfterArmor) {
                low = candidate;
            } else {
                high = candidate;
            }
        }
        target.hurt(source, (low + high) * 0.5F);
    }

    private static void explode(ServerLevel level, Vec3 center, float radius, boolean destroyBlocks) {
        level.explode(null, center.x, center.y, center.z, radius, false,
                destroyBlocks ? Level.ExplosionInteraction.BLOCK : Level.ExplosionInteraction.NONE);
    }

    public static boolean hasClearPath(ServerLevel level, Vec3 start, Vec3 end, Entity context) {
        HitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, context));
        return hit.getType() == HitResult.Type.MISS || hit.getLocation().distanceToSqr(end) < 0.36D;
    }
}
