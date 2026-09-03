package com.grenadier.mine;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public final class DirectionalMineItem extends BlockItem {
    public DirectionalMineItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (context.getLevel() instanceof ServerLevel level) {
            Vec3 position = context.getClickLocation().add(0.0D, 0.006D, 0.0D);
            DeployedMineEntity mine = new DeployedMineEntity(level, position, MineKind.DIRECTIONAL,
                    player, player.getYRot(), false);
            level.addFreshEntity(mine);
            level.playSound(null, position.x, position.y, position.z, SoundEvents.IRON_TRAPDOOR_CLOSE,
                    SoundSource.BLOCKS, 0.55F, 1.45F);
            if (!player.getAbilities().instabuild) context.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
}
