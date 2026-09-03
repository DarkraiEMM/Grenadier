package com.grenadier.mine;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

/** Deploys an entity-backed pressure mine at the exact clicked surface position. */
public final class AntiPersonnelMineItem extends BlockItem {
    public AntiPersonnelMineItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) return InteractionResult.PASS;
        if (context.getLevel() instanceof ServerLevel level) {
            Vec3 position = context.getClickLocation().add(0.0D, 0.006D, 0.0D);
            BlockPos support = BlockPos.containing(position.add(0.0D, -0.04D, 0.0D));
            boolean softGround = MineGround.isSoft(level.getBlockState(support));
            DeployedMineEntity mine = new DeployedMineEntity(level, position, MineKind.ANTI_PERSONNEL,
                    player, player.getYRot(), softGround);
            level.addFreshEntity(mine);
            level.playSound(null, position.x, position.y, position.z, SoundEvents.IRON_TRAPDOOR_CLOSE,
                    SoundSource.BLOCKS, 0.45F, 1.6F);
            if (!player.getAbilities().instabuild) context.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide);
    }
}
