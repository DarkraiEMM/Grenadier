package com.grenadier.smoke;

public final class SmokeGeometryMath {
    private SmokeGeometryMath() {
    }

    public static boolean segmentIntersectsSphere(
            double sx, double sy, double sz, double ex, double ey, double ez,
            double cx, double cy, double cz, double radius) {
        double dx = ex - sx;
        double dy = ey - sy;
        double dz = ez - sz;
        double lengthSqr = dx * dx + dy * dy + dz * dz;
        if (lengthSqr == 0.0D) {
            return distanceSqr(sx, sy, sz, cx, cy, cz) <= radius * radius;
        }
        double projection = ((cx - sx) * dx + (cy - sy) * dy + (cz - sz) * dz) / lengthSqr;
        double clamped = Math.max(0.0D, Math.min(1.0D, projection));
        return distanceSqr(sx + dx * clamped, sy + dy * clamped, sz + dz * clamped, cx, cy, cz)
                <= radius * radius;
    }

    public static boolean segmentIntersectsAabb(
            double sx, double sy, double sz, double ex, double ey, double ez,
            double halfX, double halfY, double halfZ) {
        double[] interval = {0.0D, 1.0D};
        return clipAxis(sx, ex - sx, halfX, interval)
                && clipAxis(sy, ey - sy, halfY, interval)
                && clipAxis(sz, ez - sz, halfZ, interval);
    }

    public static boolean segmentIntersectsEllipsoid(
            double sx, double sy, double sz, double ex, double ey, double ez,
            double cx, double cy, double cz, double radiusX, double radiusY, double radiusZ) {
        if (radiusX <= 0.0D || radiusY <= 0.0D || radiusZ <= 0.0D) {
            return false;
        }
        return segmentIntersectsSphere(
                (sx - cx) / radiusX,
                (sy - cy) / radiusY,
                (sz - cz) / radiusZ,
                (ex - cx) / radiusX,
                (ey - cy) / radiusY,
                (ez - cz) / radiusZ,
                0.0D, 0.0D, 0.0D, 1.0D
        );
    }

    private static boolean clipAxis(double start, double delta, double halfExtent, double[] interval) {
        if (halfExtent < 0.0D) {
            return false;
        }
        if (Math.abs(delta) < 1.0E-9D) {
            return start >= -halfExtent && start <= halfExtent;
        }
        double first = (-halfExtent - start) / delta;
        double second = (halfExtent - start) / delta;
        if (first > second) {
            double swap = first;
            first = second;
            second = swap;
        }
        interval[0] = Math.max(interval[0], first);
        interval[1] = Math.min(interval[1], second);
        return interval[0] <= interval[1];
    }

    private static double distanceSqr(double ax, double ay, double az, double bx, double by, double bz) {
        double x = ax - bx;
        double y = ay - by;
        double z = az - bz;
        return x * x + y * y + z * z;
    }
}
