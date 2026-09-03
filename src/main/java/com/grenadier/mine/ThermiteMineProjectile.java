package com.grenadier.mine;

import com.grenadier.GrenadierMod;
import com.grenadier.util.SurfaceLocator;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class ThermiteMineProjectile extends ThrowableItemProjectile {
    private boolean deployed;

    public ThermiteMineProjectile(EntityType<? extends ThermiteMineProjectile> type, Level level) { super(type, level); }
    public ThermiteMineProjectile(Level level, LivingEntity owner) { super(GrenadierMod.THERMITE_MINE_PROJECTILE.get(), owner, level); }
    @Override protected Item getDefaultItem() { return GrenadierMod.THERMITE_MINE_ITEM.get(); }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount > 200 && !deployed) {
            spawnAtLocation(getItem());
            discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!(level() instanceof ServerLevel level) || deployed) return;
        var deployment = SurfaceLocator.findSurfaceBelow(level,
                result.getLocation().add(0.0D, 0.8D, 0.0D), 3.0D, this);
        if (deployment.isPresent()) {
            deployed = true;
            Vec3 position = deployment.get();
            LivingEntity owner = getOwner() instanceof LivingEntity living ? living : null;
            float yaw = owner == null ? getYRot() : owner.getYRot();
            level.addFreshEntity(new DeployedMineEntity(level, position, MineKind.THERMITE,
                    owner, yaw, false));
            level.playSound(null, BlockPos.containing(position), SoundEvents.IRON_TRAPDOOR_CLOSE,
                    SoundSource.BLOCKS, 0.55F, 1.45F);
            discard();
            return;
        }
        setDeltaMovement(getDeltaMovement().multiply(0.45D, -0.25D, 0.45D));
        hasImpulse = true;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!deployed) {
            setDeltaMovement(getDeltaMovement().scale(-0.35D));
            hasImpulse = true;
        }
    }

    @Override public void addAdditionalSaveData(CompoundTag tag) { super.addAdditionalSaveData(tag); tag.putBoolean("Deployed", deployed); }
    @Override public void readAdditionalSaveData(CompoundTag tag) { super.readAdditionalSaveData(tag); deployed = tag.getBoolean("Deployed"); }
}
