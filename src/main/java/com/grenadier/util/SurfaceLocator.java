package com.grenadier.util;

import com.grenadier.compat.SableCompatibility;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class SurfaceLocator {
    private SurfaceLocator() {
    }

    /** Finds the exact top collision surface beneath a point, including paths, stairs and leaves. */
    public static Optional<Vec3> findSurfaceBelow(Level level, Vec3 start, double maxDrop, Entity context) {
        Vec3 end = start.add(0.0D, -Math.max(0.05D, maxDrop), 0.0D);
        HitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, context));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return Optional.empty();
        }
        BlockHitResult blockHit = (BlockHitResult) hit;
        Vec3 worldLocation = SableCompatibility.projectToGlobal(level, blockHit.getLocation());
        return Optional.of(worldLocation.add(0.0D, 0.006D, 0.0D));
    }
}
