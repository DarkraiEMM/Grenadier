package com.grenadier.signal;

import com.grenadier.smoke.SmokeCloudService;
import com.grenadier.GrenadierConfig;
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

public class SignalFlareProjectile extends ThrowableItemProjectile {
    private static final int FUSE_TICKS = 32;
    private static final int MAX_BOUNCES = 5;
    private static final double NORMAL_RESTITUTION = 0.56D;
    private static final double TANGENTIAL_DAMPING = 0.80D;

    private int smokeColor = 0xDC6F716F;
    private int fuseAge;
    private int bounces;
    private boolean sticky;
    private boolean deployed;

    public SignalFlareProjectile(EntityType<? extends SignalFlareProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public SignalFlareProjectile(Level level, LivingEntity owner) {
        super(GrenadierMod.SIGNAL_FLARE_PROJECTILE.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return GrenadierMod.SIGNAL_FLARE.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (!(this.level() instanceof ServerLevel serverLevel) || this.isRemoved() || this.deployed) {
            return;
        }
        this.fuseAge++;
        if (this.fuseAge >= FUSE_TICKS) {
            this.deploy(serverLevel, this.position(), true);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!(this.level() instanceof ServerLevel serverLevel) || this.deployed) {
            return;
        }
        if (this.sticky) {
            this.deploy(serverLevel, stickyAnchor(result), true);
            return;
        }

        Direction normal = result.getDirection();
        Vec3 velocity = this.getDeltaMovement();
        double x = normal.getAxis() == Direction.Axis.X
                ? -velocity.x * NORMAL_RESTITUTION : velocity.x * TANGENTIAL_DAMPING;
        double y = normal.getAxis() == Direction.Axis.Y
                ? -velocity.y * NORMAL_RESTITUTION : velocity.y * TANGENTIAL_DAMPING;
        double z = normal.getAxis() == Direction.Axis.Z
                ? -velocity.z * NORMAL_RESTITUTION : velocity.z * TANGENTIAL_DAMPING;
        this.setDeltaMovement(x, y, z);
        this.setPos(result.getLocation().add(
                normal.getStepX() * 0.04D,
                normal.getStepY() * 0.04D,
                normal.getStepZ() * 0.04D
        ));
        this.hasImpulse = true;
        this.bounces++;
        if (this.bounces >= MAX_BOUNCES || this.getDeltaMovement().lengthSqr() < 0.012D) {
            this.deploy(serverLevel, result.getLocation(), true);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level() instanceof ServerLevel serverLevel && !this.deployed) {
            if (this.sticky) {
                this.deploy(serverLevel, result.getLocation(), true);
            } else {
                this.setDeltaMovement(this.getDeltaMovement().scale(-0.34D));
                this.hasImpulse = true;
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SmokeColor", this.smokeColor);
        tag.putInt("FuseAge", this.fuseAge);
        tag.putInt("Bounces", this.bounces);
        tag.putBoolean("Sticky", this.sticky);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.smokeColor = tag.getInt("SmokeColor");
        this.fuseAge = tag.getInt("FuseAge");
        this.bounces = tag.getInt("Bounces");
        this.sticky = tag.getBoolean("Sticky");
    }

    public void setSmokeColor(int smokeColor) {
        this.smokeColor = smokeColor;
    }

    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }

    private Vec3 stickyAnchor(BlockHitResult result) {
        Direction face = result.getDirection();
        double radius = GrenadierConfig.RADIUS.get();
        if (face == Direction.DOWN) {
            return result.getLocation().add(0.0D, -radius * 0.96D, 0.0D);
        }
        if (face.getAxis().isHorizontal()) {
            return result.getLocation().add(
                    face.getStepX() * radius * 0.24D,
                    -radius * 0.12D,
                    face.getStepZ() * radius * 0.24D
            );
        }
        return result.getLocation();
    }

    private void deploy(ServerLevel level, Vec3 center, boolean allowCascade) {
        if (this.deployed) {
            return;
        }
        this.deployed = true;
        SmokeCloudService.INSTANCE.activate(level, center, this.smokeColor, allowCascade);
        this.discard();
    }
}
