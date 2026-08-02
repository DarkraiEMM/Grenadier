package com.grenadier.incendiary;

import com.grenadier.GrenadierMod;
import com.grenadier.smoke.UseContext;
import com.grenadier.smoke.UseDecision;
import com.grenadier.smoke.UsePolicyRegistry;
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

import java.util.Optional;

public final class IncendiaryGrenadeItem extends Item {
    public IncendiaryGrenadeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            UseDecision decision = UsePolicyRegistry.authorize(new UseContext(
                    serverPlayer, GrenadierMod.path("incendiary_grenade"), hand, stack, Optional.empty()));
            if (!decision.allowed()) {
                serverPlayer.displayClientMessage(decision.denial(), true);
                return InteractionResultHolder.fail(stack);
            }

            IncendiaryGrenadeProjectile projectile = new IncendiaryGrenadeProjectile(serverLevel, serverPlayer);
            projectile.setItem(stack);
            projectile.shootFromRotation(serverPlayer, serverPlayer.getXRot(), serverPlayer.getYRot(),
                    0.0F, 1.10F, 0.45F);
            serverLevel.addFreshEntity(projectile);
            serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.55F,
                    0.72F + serverLevel.random.nextFloat() * 0.16F);

            if (!serverPlayer.getAbilities().instabuild) {
                stack.shrink(1);
            }
            serverPlayer.getCooldowns().addCooldown(this, 30);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
