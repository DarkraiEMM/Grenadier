package com.grenadier.mine;

import com.grenadier.GrenadierConfig;
import com.grenadier.GrenadierMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public final class MineBlockEntity extends BlockEntity {
    private UUID ownerUuid;
    private long armedAt;
    private boolean ownerHasCleared;
    private boolean armed;
    private boolean triggered;

    public MineBlockEntity(BlockPos pos, BlockState state) {
        super(GrenadierMod.MINE_BLOCK_ENTITY.get(), pos, state);
    }

    public void initialize(LivingEntity owner) {
        this.ownerUuid = owner == null ? null : owner.getUUID();
        long now = this.level == null ? 0L : this.level.getGameTime();
        this.armedAt = now + armTicks(kind());
        this.ownerHasCleared = owner == null;
        this.armed = false;
        this.triggered = false;
        setChanged();
    }

    public boolean isArmed() {
        return armed;
    }

    public void triggerBy(LivingEntity triggeringEntity) {
        if (!armed || triggered || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!GrenadierConfig.MINES_DAMAGE_OWNER.get() && ownerUuid != null
                && ownerUuid.equals(triggeringEntity.getUUID())) {
            return;
        }
        trigger(serverLevel);
    }

    public void triggerByProjectile() {
        if (!triggered && level instanceof ServerLevel serverLevel) {
            trigger(serverLevel);
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MineBlockEntity mine) {
        if (!(level instanceof ServerLevel serverLevel) || mine.triggered) {
            return;
        }
        MineKind kind = mine.kind();
        if (!mine.armed) {
            mine.updateOwnerClearance(serverLevel);
            if (MineMath.canArm(level.getGameTime(), mine.armedAt, mine.ownerHasCleared)) {
                mine.armed = true;
                mine.setChanged();
                serverLevel.playSound(null, pos, net.minecraft.sounds.SoundEvents.LEVER_CLICK,
                        net.minecraft.sounds.SoundSource.BLOCKS, 0.35F, 1.65F);
            }
            return;
        }
        if (level.getGameTime() % 2L != 0L) {
            return;
        }
        LivingEntity target = switch (kind) {
            // Legacy block mines use AbstractMineBlock.entityInside and only trigger underfoot.
            case ANTI_PERSONNEL -> null;
            case DIRECTIONAL -> mine.findDirectionalTarget(serverLevel, state);
            case THERMITE -> mine.findNearbyTarget(serverLevel,
                    GrenadierConfig.THERMITE_MINE_TRIGGER_RADIUS.get());
        };
        if (target != null) {
            mine.triggerBy(target);
        }
    }

    private void updateOwnerClearance(ServerLevel level) {
        if (ownerHasCleared) return;
        ServerPlayer owner = ownerUuid == null ? null : level.getServer().getPlayerList().getPlayer(ownerUuid);
        if (owner == null || owner.level() != level || owner.distanceToSqr(Vec3.atCenterOf(worldPosition)) > 2.25D) {
            ownerHasCleared = true;
            setChanged();
        }
    }

    private LivingEntity findNearbyTarget(ServerLevel level, double radius) {
        Vec3 center = Vec3.atCenterOf(worldPosition);
        AABB bounds = new AABB(worldPosition).inflate(radius, 1.25D, radius);
        return level.getEntitiesOfClass(LivingEntity.class, bounds, target -> validTarget(target, center, radius))
                .stream().findFirst().orElse(null);
    }

    private LivingEntity findDirectionalTarget(ServerLevel level, BlockState state) {
        double range = GrenadierConfig.DIRECTIONAL_MINE_RANGE.get();
        double width = GrenadierConfig.DIRECTIONAL_MINE_WIDTH.get();
        Direction facing = state.getValue(DirectionalMineBlock.FACING);
        Vec3 origin = Vec3.atBottomCenterOf(worldPosition).add(0.0D, 0.3D, 0.0D);
        AABB bounds = new AABB(worldPosition).inflate(range, 1.8D, range);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, bounds,
                target -> validTarget(target, origin, range)
                        && MineMath.isInHorizontalCone(origin, facing, target.position(), range, width,
                        GrenadierConfig.DIRECTIONAL_MINE_HALF_ANGLE.get()));
        for (LivingEntity target : candidates) {
            if (MineEffects.hasClearPath(level, origin, target.getEyePosition(), target)) return target;
        }
        return null;
    }

    private boolean validTarget(LivingEntity target, Vec3 center, double radius) {
        if (!target.isAlive() || target.isSpectator()) return false;
        if (!GrenadierConfig.MINES_DAMAGE_OWNER.get() && ownerUuid != null && ownerUuid.equals(target.getUUID())) return false;
        return MineGeometry.isInsideHorizontalRadius(target.getX() - center.x, target.getZ() - center.z, radius);
    }

    private void trigger(ServerLevel level) {
        if (triggered) return;
        triggered = true;
        setChanged();
        LivingEntity owner = ownerUuid == null ? null : level.getServer().getPlayerList().getPlayer(ownerUuid);
        MineEffects.trigger(level, worldPosition, getBlockState(), kind(), owner);
    }

    private MineKind kind() {
        return getBlockState().getBlock() instanceof AbstractMineBlock block ? block.kind() : MineKind.ANTI_PERSONNEL;
    }

    private static int armTicks(MineKind kind) {
        return switch (kind) {
            case ANTI_PERSONNEL -> GrenadierConfig.ANTI_PERSONNEL_MINE_ARM_TICKS.get();
            case DIRECTIONAL -> GrenadierConfig.DIRECTIONAL_MINE_ARM_TICKS.get();
            case THERMITE -> GrenadierConfig.THERMITE_MINE_ARM_TICKS.get();
        };
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (ownerUuid != null) tag.putUUID("Owner", ownerUuid);
        tag.putLong("ArmedAt", armedAt);
        tag.putBoolean("OwnerHasCleared", ownerHasCleared);
        tag.putBoolean("Armed", armed);
        tag.putBoolean("Triggered", triggered);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ownerUuid = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        armedAt = tag.getLong("ArmedAt");
        ownerHasCleared = tag.getBoolean("OwnerHasCleared");
        armed = tag.getBoolean("Armed");
        triggered = tag.getBoolean("Triggered");
    }
}
