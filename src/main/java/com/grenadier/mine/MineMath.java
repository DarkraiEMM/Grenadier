package com.grenadier.mine;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class MineMath {
    private MineMath() {
    }

    public static boolean isInHorizontalCone(Vec3 origin, Direction facing, Vec3 target,
                                             double range, double width, double halfAngleDegrees) {
        return MineGeometry.isInHorizontalCone(target.x - origin.x, target.z - origin.z,
                facing.getStepX(), facing.getStepZ(), range, width, halfAngleDegrees);
    }

    public static boolean isInHorizontalCone(Vec3 origin, Vec3 forward, Vec3 target,
                                             double range, double width, double halfAngleDegrees) {
        return MineGeometry.isInHorizontalCone(target.x - origin.x, target.z - origin.z,
                forward.x, forward.z, range, width, halfAngleDegrees);
    }

    public static boolean canArm(long now, long armedAt, boolean ownerHasCleared) {
        return MineGeometry.canArm(now, armedAt, ownerHasCleared);
    }
}
