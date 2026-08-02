package com.grenadier.incendiary;

import com.grenadier.GrenadierMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class IncendiaryGrenadeProjectile extends ThrowableItemProjectile {
    private static final int ARM_TICKS = 6;
    private static final int FUSE_TICKS = 36;
    private static final double RESTITUTION = 0.12D;
    private static final double TANGENTIAL_DAMPING = 0.58D;
    private static final int GROUND_SEARCH_DEPTH = 12;

    private int fuseAge;
    private boolean deployed;

    public IncendiaryGrenadeProjectile(EntityType<? extends IncendiaryGrenadeProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public IncendiaryGrenadeProjectile(Level level, LivingEntity owner) {
        super(GrenadierMod.INCENDIARY_GRENADE_PROJECTILE.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return GrenadierMod.INCENDIARY_GRENADE.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel) || this.isRemoved() || this.deployed) {
            return;
        }
        this.fuseAge++;
        if (this.fuseAge >= FUSE_TICKS) {
            this.deploy(serverLevel, findGround(serverLevel, this.position()));
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!(this.level() instanceof ServerLevel serverLevel) || this.deployed) {
            return;
        }
        if (this.fuseAge >= ARM_TICKS) {
            this.deploy(serverLevel, findGround(serverLevel, result.getLocation()));
            return;
        }
        this.bounce(result.getDirection(), result.getLocation());
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!(this.level() instanceof ServerLevel serverLevel) || this.deployed) {
            return;
        }
        if (this.fuseAge >= ARM_TICKS) {
            this.deploy(serverLevel, findGround(serverLevel, result.getEntity().position()));
        } else {
            this.setDeltaMovement(this.getDeltaMovement().scale(-RESTITUTION));
            this.hasImpulse = true;
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("FuseAge", this.fuseAge);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.fuseAge = tag.getInt("FuseAge");
    }

    private void bounce(Direction normal, Vec3 contact) {
        Vec3 velocity = this.getDeltaMovement();
        double x = normal.getAxis() == Direction.Axis.X
                ? -velocity.x * RESTITUTION : velocity.x * TANGENTIAL_DAMPING;
        double y = normal.getAxis() == Direction.Axis.Y
                ? -velocity.y * RESTITUTION : velocity.y * TANGENTIAL_DAMPING;
        double z = normal.getAxis() == Direction.Axis.Z
                ? -velocity.z * RESTITUTION : velocity.z * TANGENTIAL_DAMPING;
        this.setDeltaMovement(x, y, z);
        this.setPos(contact.add(normal.getStepX() * 0.04D, normal.getStepY() * 0.04D, normal.getStepZ() * 0.04D));
        this.hasImpulse = true;
    }

    private void deploy(ServerLevel level, Vec3 center) {
        if (this.deployed) {
            return;
        }
        this.deployed = true;
        if (!level.getFluidState(BlockPos.containing(center)).is(net.minecraft.tags.FluidTags.WATER)) {
            Entity owner = this.getOwner();
            IncendiaryFieldEntity field = new IncendiaryFieldEntity(
                    level, center, owner instanceof LivingEntity living ? living : null);
            level.addFreshEntity(field);
            level.sendParticles(GrenadierMod.INCENDIARY_FLAME.get(), center.x, center.y + 0.22D, center.z,
                    24, 1.7D, 0.42D, 1.7D, 0.045D);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y + 0.32D, center.z,
                    18, 1.2D, 0.35D, 1.2D, 0.025D);
            level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE.value(),
                    SoundSource.PLAYERS, 0.75F, 1.25F + level.random.nextFloat() * 0.15F);
            level.playSound(null, center.x, center.y, center.z, SoundEvents.FIRECHARGE_USE,
                    SoundSource.PLAYERS, 1.0F, 0.82F + level.random.nextFloat() * 0.12F);
        } else {
            level.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 0.1D, center.z,
                    12, 0.45D, 0.15D, 0.45D, 0.025D);
            level.playSound(null, center.x, center.y, center.z, SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.PLAYERS, 0.8F, 1.0F);
        }
        this.discard();
    }

    private static Vec3 findGround(ServerLevel level, Vec3 origin) {
        int x = BlockPos.containing(origin).getX();
        int z = BlockPos.containing(origin).getZ();
        int startY = BlockPos.containing(origin.add(0.0D, 1.0D, 0.0D)).getY();
        for (int y = startY; y >= startY - GROUND_SEARCH_DEPTH; y--) {
            BlockPos feet = new BlockPos(x, y, z);
            BlockPos below = feet.below();
            BlockState floor = level.getBlockState(below);
            if (floor.isFaceSturdy(level, below, Direction.UP)
                    && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()) {
                return new Vec3(origin.x, y + 0.04D, origin.z);
            }
        }
        return origin;
    }
}
