package com.grenadier.mine;

import com.grenadier.GrenadierConfig;
import com.grenadier.GrenadierMod;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public final class DeployedMineEntity extends Entity {
    private static final EntityDataAccessor<Integer> KIND = SynchedEntityData.defineId(
            DeployedMineEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> SOFT_GROUND = SynchedEntityData.defineId(
            DeployedMineEntity.class, EntityDataSerializers.BOOLEAN);

    private UUID ownerUuid;
    private long armedAt;
    private boolean ownerHasCleared;
    private boolean armed;
    private boolean triggered;

    public DeployedMineEntity(EntityType<? extends DeployedMineEntity> type, Level level) {
        super(type, level);
        setNoGravity(true);
        // KIND defaults to ANTI_PERSONNEL. Because a synced value of 0 does not count as a
        // data change, clients need an explicit initial refresh for the default mine kind.
        refreshDimensions();
    }

    public DeployedMineEntity(ServerLevel level, Vec3 position, MineKind kind,
                              LivingEntity owner, float yaw, boolean softGround) {
        this(GrenadierMod.DEPLOYED_MINE.get(), level);
        setPos(position);
        setYRot(yaw);
        yRotO = yaw;
        entityData.set(KIND, kind.ordinal());
        refreshDimensions();
        entityData.set(SOFT_GROUND, softGround);
        ownerUuid = owner == null ? null : owner.getUUID();
        ownerHasCleared = owner == null;
        armedAt = level.getGameTime() + armTicks(kind);
    }

    public MineKind kind() {
        int ordinal = entityData.get(KIND);
        MineKind[] values = MineKind.values();
        return values[Math.max(0, Math.min(values.length - 1, ordinal))];
    }

    public boolean isSoftGround() {
        return entityData.get(SOFT_GROUND);
    }

    public boolean isArmed() {
        return armed;
    }

    public BlockState visualState() {
        return switch (kind()) {
            case ANTI_PERSONNEL -> GrenadierMod.ANTI_PERSONNEL_MINE.get().defaultBlockState()
                    .setValue(AntiPersonnelMineBlock.SOFT_GROUND, isSoftGround());
            case DIRECTIONAL -> GrenadierMod.DIRECTIONAL_MINE.get().defaultBlockState()
                    .setValue(DirectionalMineBlock.FACING, Direction.NORTH);
            case THERMITE -> GrenadierMod.THERMITE_MINE.get().defaultBlockState();
        };
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);
        if (!(level() instanceof ServerLevel serverLevel) || triggered) {
            return;
        }
        if (!armed) {
            updateOwnerClearance(serverLevel);
            if (MineMath.canArm(serverLevel.getGameTime(), armedAt, ownerHasCleared)) {
                armed = true;
                serverLevel.playSound(null, blockPosition(), SoundEvents.LEVER_CLICK,
                        SoundSource.BLOCKS, 0.35F, 1.65F);
            }
            return;
        }
        if (tickCount % 2 != 0) {
            return;
        }
        LivingEntity target = switch (kind()) {
            case ANTI_PERSONNEL -> findPressureTarget(serverLevel);
            case DIRECTIONAL -> findDirectionalTarget(serverLevel);
            case THERMITE -> findNearbyTarget(serverLevel, GrenadierConfig.THERMITE_MINE_TRIGGER_RADIUS.get());
        };
        if (target != null) {
            triggerBy(serverLevel, target);
        }
    }

    private LivingEntity findPressureTarget(ServerLevel level) {
        AABB pressureArea = new AABB(getX() - 0.38D, getY() - 0.06D, getZ() - 0.38D,
                getX() + 0.38D, getY() + 0.34D, getZ() + 0.38D);
        return level.getEntitiesOfClass(LivingEntity.class, pressureArea, this::validTarget)
                .stream().filter(target -> MineGeometry.isPressureContact(
                        target.getX() - getX(), target.getZ() - getZ(),
                        target.getBoundingBox().minY - getY(), 0.48D, 0.28D))
                .findFirst().orElse(null);
    }

    private LivingEntity findNearbyTarget(ServerLevel level, double radius) {
        AABB bounds = getBoundingBox().inflate(radius, 1.25D, radius);
        return level.getEntitiesOfClass(LivingEntity.class, bounds, target -> {
                    double dx = target.getX() - getX();
                    double dz = target.getZ() - getZ();
                    return validTarget(target) && MineGeometry.isInsideHorizontalRadius(dx, dz, radius);
                })
                .stream().findFirst().orElse(null);
    }

    private LivingEntity findDirectionalTarget(ServerLevel level) {
        double range = GrenadierConfig.DIRECTIONAL_MINE_RANGE.get();
        double width = GrenadierConfig.DIRECTIONAL_MINE_WIDTH.get();
        Vec3 origin = position().add(0.0D, 0.3D, 0.0D);
        Vec3 forward = Vec3.directionFromRotation(0.0F, getYRot());
        AABB bounds = getBoundingBox().inflate(range, 1.8D, range);
        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, bounds,
                target -> validTarget(target)
                        && MineMath.isInHorizontalCone(origin, forward, target.position(), range, width,
                        GrenadierConfig.DIRECTIONAL_MINE_HALF_ANGLE.get()));
        for (LivingEntity target : candidates) {
            if (MineEffects.hasClearPath(level, origin, target.getEyePosition(), target)) {
                return target;
            }
        }
        return null;
    }

    private boolean validTarget(LivingEntity target) {
        return target.isAlive() && !target.isSpectator()
                && (GrenadierConfig.MINES_DAMAGE_OWNER.get() || ownerUuid == null
                || !ownerUuid.equals(target.getUUID()));
    }

    private void updateOwnerClearance(ServerLevel level) {
        if (ownerHasCleared) {
            return;
        }
        ServerPlayer owner = ownerUuid == null ? null : level.getServer().getPlayerList().getPlayer(ownerUuid);
        if (owner == null || owner.level() != level || owner.distanceToSqr(position()) > 2.25D) {
            ownerHasCleared = true;
        }
    }

    private void triggerBy(ServerLevel level, LivingEntity triggeringEntity) {
        if (triggered || (!GrenadierConfig.MINES_DAMAGE_OWNER.get() && ownerUuid != null
                && ownerUuid.equals(triggeringEntity.getUUID()))) {
            return;
        }
        trigger(level);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level() instanceof ServerLevel serverLevel && !triggered
                && (source.is(DamageTypeTags.IS_PROJECTILE) || source.getDirectEntity() instanceof Projectile)) {
            trigger(serverLevel);
            return true;
        }
        return super.hurt(source, amount);
    }

    private void trigger(ServerLevel level) {
        if (triggered) {
            return;
        }
        triggered = true;
        LivingEntity owner = ownerUuid == null ? null : level.getServer().getPlayerList().getPlayer(ownerUuid);
        MineEffects.triggerDeployed(level, position(), kind(), getYRot(), owner);
        discard();
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (armed) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide) {
            ItemStack recovered = pickupStack();
            if (!player.addItem(recovered)) {
                spawnAtLocation(recovered);
            }
            discard();
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    private ItemStack pickupStack() {
        return switch (kind()) {
            case ANTI_PERSONNEL -> new ItemStack(GrenadierMod.ANTI_PERSONNEL_MINE_ITEM.get());
            case DIRECTIONAL -> new ItemStack(GrenadierMod.DIRECTIONAL_MINE_ITEM.get());
            case THERMITE -> new ItemStack(GrenadierMod.THERMITE_MINE_ITEM.get());
        };
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return switch (kind()) {
            case ANTI_PERSONNEL -> EntityDimensions.fixed(0.42F, 0.10F);
            case DIRECTIONAL -> EntityDimensions.fixed(0.50F, 0.48F);
            case THERMITE -> EntityDimensions.fixed(0.40F, 0.09F);
        };
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (KIND.equals(key)) {
            refreshDimensions();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(KIND, MineKind.ANTI_PERSONNEL.ordinal());
        builder.define(SOFT_GROUND, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        entityData.set(KIND, tag.getInt("Kind"));
        refreshDimensions();
        entityData.set(SOFT_GROUND, tag.getBoolean("SoftGround"));
        ownerUuid = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        armedAt = tag.getLong("ArmedAt");
        ownerHasCleared = tag.getBoolean("OwnerHasCleared");
        armed = tag.getBoolean("Armed");
        triggered = tag.getBoolean("Triggered");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Kind", kind().ordinal());
        tag.putBoolean("SoftGround", isSoftGround());
        if (ownerUuid != null) tag.putUUID("Owner", ownerUuid);
        tag.putLong("ArmedAt", armedAt);
        tag.putBoolean("OwnerHasCleared", ownerHasCleared);
        tag.putBoolean("Armed", armed);
        tag.putBoolean("Triggered", triggered);
    }

    private static int armTicks(MineKind kind) {
        return switch (kind) {
            case ANTI_PERSONNEL -> GrenadierConfig.ANTI_PERSONNEL_MINE_ARM_TICKS.get();
            case DIRECTIONAL -> GrenadierConfig.DIRECTIONAL_MINE_ARM_TICKS.get();
            case THERMITE -> GrenadierConfig.THERMITE_MINE_ARM_TICKS.get();
        };
    }
}
