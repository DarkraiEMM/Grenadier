package com.grenadier.mine;

import com.grenadier.GrenadierConfig;
import com.grenadier.GrenadierMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ThermiteMineItem extends Item {
    public ThermiteMineItem(Properties properties) { super(properties); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            ThermiteMineProjectile projectile = new ThermiteMineProjectile(serverLevel, serverPlayer);
            projectile.setItem(stack);
            projectile.shootFromRotation(serverPlayer, serverPlayer.getXRot(), serverPlayer.getYRot(),
                    0.0F, GrenadierConfig.THERMITE_MINE_THROW_SPEED.get().floatValue(), 0.25F);
            serverLevel.addFreshEntity(projectile);
            serverLevel.playSound(null, player.blockPosition(), SoundEvents.SNOWBALL_THROW,
                    SoundSource.PLAYERS, 0.55F, 0.8F);
            if (!player.getAbilities().instabuild) stack.shrink(1);
            player.getCooldowns().addCooldown(this, 20);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
