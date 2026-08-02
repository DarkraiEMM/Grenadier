package com.grenadier.flashbang;

import com.grenadier.GrenadierMod;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class FlashbangProjectile extends ThrowableItemProjectile {
    private static final int FUSE_TICKS = 30;
    private static final double RESTITUTION = 0.22D;
    private static final double TANGENTIAL_DAMPING = 0.68D;

    private int fuseAge;
    private boolean detonated;

    public FlashbangProjectile(EntityType<? extends FlashbangProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public FlashbangProjectile(Level level, LivingEntity owner) {
        super(GrenadierMod.FLASHBANG_PROJECTILE.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return GrenadierMod.FLASHBANG.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel) || this.isRemoved() || this.detonated) {
            return;
        }
        this.fuseAge++;
        if (this.fuseAge >= FUSE_TICKS) {
            this.detonate(serverLevel);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.detonated) {
            this.bounce(result.getDirection(), result.getLocation());
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.detonated) {
            this.setDeltaMovement(this.getDeltaMovement().scale(-0.28D));
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

    private void detonate(ServerLevel level) {
        if (this.detonated) {
            return;
        }
        this.detonated = true;
        FlashbangEffects.detonate(level, this.position(), this.getOwner());
        this.discard();
    }
}
