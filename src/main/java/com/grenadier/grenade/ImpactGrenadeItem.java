package com.grenadier.grenade;

import com.grenadier.GrenadierMod;
import com.grenadier.smoke.UseContext;
import com.grenadier.smoke.UseDecision;
import com.grenadier.smoke.UsePolicyRegistry;
import com.grenadier.util.ProjectileLaunch;
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

public final class ImpactGrenadeItem extends Item {
    public ImpactGrenadeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel && player instanceof ServerPlayer serverPlayer) {
            UseDecision decision = UsePolicyRegistry.authorize(new UseContext(
                    serverPlayer, GrenadierMod.path("impact_grenade"), hand, stack, Optional.empty()));
            if (!decision.allowed()) {
                serverPlayer.displayClientMessage(decision.denial(), true);
                return InteractionResultHolder.fail(stack);
            }

            ImpactGrenadeProjectile projectile = new ImpactGrenadeProjectile(serverLevel, serverPlayer);
            ProjectileLaunch.placeAtHand(projectile, serverPlayer, hand);
            projectile.setItem(stack);
            projectile.shootFromRotation(serverPlayer, serverPlayer.getXRot(), serverPlayer.getYRot(),
                    0.0F, 1.28F, 0.30F);
            serverLevel.addFreshEntity(projectile);
            serverLevel.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                    SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.58F,
                    0.90F + serverLevel.random.nextFloat() * 0.14F);

            if (!serverPlayer.getAbilities().instabuild) {
                stack.shrink(1);
            }
            serverPlayer.getCooldowns().addCooldown(this, 20);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
