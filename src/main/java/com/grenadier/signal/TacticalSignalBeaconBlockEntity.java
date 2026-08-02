package com.grenadier.signal;

import com.grenadier.GrenadierConfig;
import com.grenadier.smoke.SmokeGrenadeColors;
import com.grenadier.GrenadierMod;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TacticalSignalBeaconBlockEntity extends BlockEntity {
    private static final int LEGACY_DEFAULT_COLOR = 0x55D957;
    private static final int DEFAULT_BEAM_COLOR = 0xFFFFFF;
    private static final int DEFAULT_SMOKE_COLOR = SmokeGrenadeColors.DEFAULT_RGB;
    private int baseColor = DEFAULT_BEAM_COLOR;
    private int baseSmokeColor = DEFAULT_SMOKE_COLOR;
    private int beamColor = DEFAULT_BEAM_COLOR;
    private int smokeColor = DEFAULT_SMOKE_COLOR;
    private long activeUntil;
    private long cooldownUntil;

    public TacticalSignalBeaconBlockEntity(BlockPos pos, BlockState blockState) {
        super(GrenadierMod.TACTICAL_SIGNAL_BEACON_BLOCK_ENTITY.get(), pos, blockState);
    }

    public int baseColor() {
        return this.baseColor;
    }

    public int beamColor() {
        return this.beamColor;
    }

    /**
     * Compatibility hook used by the optional event core to apply a team color
     * to an already active tactical signal. Event colors drive both outputs.
     */
    public void setBeamColor(int color) {
        this.beamColor = color & 0xFFFFFF;
        this.smokeColor = this.beamColor;
        this.sync();
    }

    public int baseSmokeColor() {
        return this.baseSmokeColor;
    }

    public void setBaseColor(int color) {
        this.baseColor = color & 0xFFFFFF;
        this.baseSmokeColor = this.baseColor;
        if (!this.getBlockState().getValue(TacticalSignalBeaconBlock.ACTIVE)) {
            this.beamColor = this.baseColor;
            this.smokeColor = this.baseSmokeColor;
        }
        this.sync();
    }

    public boolean canActivate(long gameTime) {
        return gameTime >= this.cooldownUntil && !this.getBlockState().getValue(TacticalSignalBeaconBlock.ACTIVE);
    }

    public long cooldownSecondsRemaining(long gameTime) {
        return Math.max(0L, (this.cooldownUntil - gameTime + 19L) / 20L);
    }

    public void activate(int beamColor, int smokeColor) {
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        long now = this.level.getGameTime();
        this.beamColor = beamColor & 0xFFFFFF;
        this.smokeColor = smokeColor & 0xFFFFFF;
        this.activeUntil = now + GrenadierConfig.SIGNAL_DURATION_SECONDS.get() * 20L;
        this.cooldownUntil = now + GrenadierConfig.SIGNAL_COOLDOWN_SECONDS.get() * 20L;
        this.level.setBlock(this.worldPosition, this.getBlockState().setValue(TacticalSignalBeaconBlock.ACTIVE, true), 3);
        this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
        this.level.playSound(null, this.worldPosition, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 0.8F, 1.1F);
        this.sync();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TacticalSignalBeaconBlockEntity beacon) {
        if (!state.getValue(TacticalSignalBeaconBlock.ACTIVE)) {
            return;
        }
        if (level.getGameTime() < beacon.activeUntil) {
            if (level instanceof ServerLevel serverLevel && level.getGameTime() % 4L == 0L) {
                serverLevel.sendParticles(
                        ColorParticleOption.create(GrenadierMod.COLORED_SIGNAL_SMOKE.get(), 0xF0000000 | beacon.smokeColor),
                        pos.getX() + 0.5D,
                        pos.getY() + 2.1D,
                        pos.getZ() + 0.5D,
                        5,
                        0.28D,
                        1.05D,
                        0.28D,
                        0.022D
                );
            }
            return;
        }
        beacon.beamColor = beacon.baseColor;
        beacon.smokeColor = beacon.baseSmokeColor;
        level.setBlock(pos, state.setValue(TacticalSignalBeaconBlock.ACTIVE, false), 3);
        level.updateNeighborsAt(pos, state.getBlock());
        level.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.BLOCKS, 0.65F, 0.9F);
        beacon.sync();
    }

    private void sync() {
        this.setChanged();
        if (this.level != null) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("baseColor", this.baseColor);
        tag.putInt("baseSmokeColor", this.baseSmokeColor);
        tag.putInt("beamColor", this.beamColor);
        tag.putInt("smokeColor", this.smokeColor);
        tag.putLong("activeUntil", this.activeUntil);
        tag.putLong("cooldownUntil", this.cooldownUntil);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        boolean hasLegacyColor = tag.contains("baseColor") || tag.contains("beamColor");
        int storedBaseColor = tag.contains("baseColor") ? tag.getInt("baseColor") : tag.getInt("beamColor");
        boolean migrateLegacyDefault = hasLegacyColor
                && !tag.contains("baseSmokeColor")
                && storedBaseColor == LEGACY_DEFAULT_COLOR;

        this.baseColor = !hasLegacyColor || migrateLegacyDefault ? DEFAULT_BEAM_COLOR : storedBaseColor & 0xFFFFFF;
        this.baseSmokeColor = tag.contains("baseSmokeColor")
                ? tag.getInt("baseSmokeColor") & 0xFFFFFF
                : migrateLegacyDefault || !hasLegacyColor ? DEFAULT_SMOKE_COLOR : this.baseColor;

        int storedBeamColor = tag.contains("beamColor") ? tag.getInt("beamColor") & 0xFFFFFF : this.baseColor;
        this.beamColor = migrateLegacyDefault && storedBeamColor == LEGACY_DEFAULT_COLOR
                ? DEFAULT_BEAM_COLOR
                : storedBeamColor;
        this.smokeColor = tag.contains("smokeColor")
                ? tag.getInt("smokeColor") & 0xFFFFFF
                : migrateLegacyDefault || !hasLegacyColor ? DEFAULT_SMOKE_COLOR : this.beamColor;
        this.activeUntil = tag.getLong("activeUntil");
        this.cooldownUntil = tag.getLong("cooldownUntil");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return this.saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
