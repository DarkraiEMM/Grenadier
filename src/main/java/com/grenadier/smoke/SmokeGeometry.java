package com.grenadier.smoke;

import net.minecraft.world.phys.Vec3;

public final class SmokeGeometry {
    private SmokeGeometry() {
    }

    public static boolean segmentIntersectsSphere(Vec3 start, Vec3 end, Vec3 center, double radius) {
        return SmokeGeometryMath.segmentIntersectsSphere(
                start.x, start.y, start.z, end.x, end.y, end.z,
                center.x, center.y, center.z, radius);
    }

    public static boolean segmentIntersectsAabb(Vec3 start, Vec3 end, double halfX, double halfY, double halfZ) {
        return SmokeGeometryMath.segmentIntersectsAabb(
                start.x, start.y, start.z, end.x, end.y, end.z,
                halfX, halfY, halfZ);
    }

    public static boolean segmentIntersectsEllipsoid(
            Vec3 start,
            Vec3 end,
            Vec3 center,
            double radiusX,
            double radiusY,
            double radiusZ
    ) {
        return SmokeGeometryMath.segmentIntersectsEllipsoid(
                start.x, start.y, start.z, end.x, end.y, end.z,
                center.x, center.y, center.z, radiusX, radiusY, radiusZ);
    }
}
