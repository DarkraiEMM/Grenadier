package com.grenadier.signal;

import com.grenadier.smoke.SmokeCloudService;
import com.grenadier.GrenadierConfig;
import com.grenadier.GrenadierMod;
import com.grenadier.compat.SableCompatibility;
import com.grenadier.util.SurfaceLocator;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ColorParticleOption;
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
    private static final int MAX_BOUNCES = 5;

    private int smokeColor = 0xDC6F716F;
    private int fuseAge;
    private int bounces;
    private boolean sticky;
    private boolean fuseExpired;
    private boolean resting;
    private boolean deployed;
    private int unsupportedFuseTicks;
    private boolean supportDiagnosticLogged;

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
        if (!this.fuseExpired && this.fuseAge >= GrenadierConfig.SMOKE_FUSE_TICKS.get()) {
            this.fuseExpired = true;
        }
        if (this.fuseExpired) {
            // Sable may support an entity through a tracking sub-level without
            // producing vanilla ground flags or a BlockHitResult.
            boolean trackingStructure = SableCompatibility.isTrackingStructure(this);
            if (this.onGround() || this.verticalCollisionBelow || this.resting
                    || trackingStructure) {
                if (trackingStructure) {
                    GrenadierMod.LOGGER.info("Smoke grenade deployed from Sable structure tracking at {}", this.position());
                }
                Vec3 surface = SurfaceLocator.findSurfaceBelow(
                                serverLevel, this.position().add(0.0D, 0.35D, 0.0D), 1.5D, this)
                        .orElse(this.position());
                this.deploy(serverLevel, surface, !trackingStructure);
            } else if (this.tickCount % 2 == 0) {
                this.spawnVentTrail(serverLevel);
                this.unsupportedFuseTicks++;
                if (!this.supportDiagnosticLogged && this.unsupportedFuseTicks >= 10) {
                    this.supportDiagnosticLogged = true;
                    GrenadierMod.LOGGER.warn(
                            "Smoke grenade still airborne after fuse: pos={}, motion={}, onGround={}, verticalBelow={}, vertical={}, horizontal={}, resting={}, sableBridge={}",
                            this.position(), this.getDeltaMovement(), this.onGround(), this.verticalCollisionBelow,
                            this.verticalCollision, this.horizontalCollision, this.resting,
                            SableCompatibility.isBridgeAvailable());
                }
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!(this.level() instanceof ServerLevel serverLevel) || this.deployed) {
            return;
        }
        Vec3 rawHitLocation = result.getLocation();
        Vec3 worldHitLocation = SableCompatibility.projectToGlobal(serverLevel, rawHitLocation);
        boolean physicalStructureHit = SableCompatibility.wasProjected(rawHitLocation, worldHitLocation);
        if (this.sticky) {
            this.deploy(serverLevel, stickyAnchor(result, worldHitLocation), !physicalStructureHit);
            return;
        }

        Direction normal = result.getDirection();
        Vec3 velocity = this.getDeltaMovement();
        double restitution = GrenadierConfig.SMOKE_RESTITUTION.get();
        double tangentialDamping = GrenadierConfig.SMOKE_TANGENTIAL_DAMPING.get();
        double x = normal.getAxis() == Direction.Axis.X
                ? -velocity.x * restitution : velocity.x * tangentialDamping;
        double y = normal.getAxis() == Direction.Axis.Y
                ? -velocity.y * restitution : velocity.y * tangentialDamping;
        double z = normal.getAxis() == Direction.Axis.Z
                ? -velocity.z * restitution : velocity.z * tangentialDamping;
        this.setDeltaMovement(x, y, z);
        this.setPos(worldHitLocation.add(
                normal.getStepX() * 0.04D,
                normal.getStepY() * 0.04D,
                normal.getStepZ() * 0.04D
        ));
        this.hasImpulse = true;
        this.bounces++;
        this.resting = false;
        if (normal == Direction.UP && (this.fuseExpired
                || this.bounces >= MAX_BOUNCES || this.getDeltaMovement().lengthSqr() < 0.012D)) {
            if (this.fuseExpired) {
                this.deploy(serverLevel, worldHitLocation, !physicalStructureHit);
            } else {
                this.resting = true;
                this.setDeltaMovement(Vec3.ZERO);
                this.setPos(worldHitLocation.add(0.0D, 0.04D, 0.0D));
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level() instanceof ServerLevel serverLevel && !this.deployed) {
            if (this.sticky) {
                this.deploy(serverLevel, result.getLocation(), true);
            } else {
                this.resting = false;
                this.setDeltaMovement(this.getDeltaMovement().scale(-GrenadierConfig.SMOKE_RESTITUTION.get()));
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
        tag.putBoolean("FuseExpired", this.fuseExpired);
        tag.putBoolean("Resting", this.resting);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.smokeColor = tag.getInt("SmokeColor");
        this.fuseAge = tag.getInt("FuseAge");
        this.bounces = tag.getInt("Bounces");
        this.sticky = tag.getBoolean("Sticky");
        this.fuseExpired = tag.getBoolean("FuseExpired");
        this.resting = tag.getBoolean("Resting");
    }

    public void setSmokeColor(int smokeColor) {
        this.smokeColor = smokeColor;
    }

    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }

    private Vec3 stickyAnchor(BlockHitResult result, Vec3 hitLocation) {
        Direction face = result.getDirection();
        double radius = GrenadierConfig.RADIUS.get();
        if (face == Direction.DOWN) {
            return hitLocation.add(0.0D, -radius * 0.96D, 0.0D);
        }
        if (face.getAxis().isHorizontal()) {
            return hitLocation.add(
                    face.getStepX() * radius * 0.24D,
                    -radius * 0.12D,
                    face.getStepZ() * radius * 0.24D
            );
        }
        return hitLocation;
    }

    private void spawnVentTrail(ServerLevel level) {
        ColorParticleOption smoke = ColorParticleOption.create(
                GrenadierMod.COLORED_SIGNAL_SMOKE.get(), this.smokeColor);
        level.sendParticles(smoke, this.getX(), this.getY() + 0.08D, this.getZ(),
                2, 0.06D, 0.04D, 0.06D, 0.008D);
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
