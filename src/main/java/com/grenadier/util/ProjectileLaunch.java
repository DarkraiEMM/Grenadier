package com.grenadier.util;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class ProjectileLaunch {
    private static final double FORWARD_OFFSET = 0.25D;
    private static final double SIDE_OFFSET = 0.28D;
    private static final double DOWN_OFFSET = 0.32D;

    private ProjectileLaunch() {
    }

    public static void placeAtHand(Entity projectile, Player player, InteractionHand hand) {
        Vec3 forward = player.getViewVector(1.0F);
        double yaw = Math.toRadians(player.getYRot());
        Vec3 screenRight = new Vec3(-Math.cos(yaw), 0.0D, -Math.sin(yaw));
        boolean heldOnRight = (hand == InteractionHand.MAIN_HAND)
                == (player.getMainArm() == HumanoidArm.RIGHT);
        double side = heldOnRight ? SIDE_OFFSET : -SIDE_OFFSET;
        Vec3 origin = player.getEyePosition()
                .add(forward.scale(FORWARD_OFFSET))
                .add(screenRight.scale(side))
                .add(0.0D, -DOWN_OFFSET, 0.0D);
        projectile.setPos(origin);
    }
}
