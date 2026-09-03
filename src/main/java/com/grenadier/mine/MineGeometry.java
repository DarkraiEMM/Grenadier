package com.grenadier.mine;

public final class MineGeometry {
    private MineGeometry() {
    }

    public static boolean isInsideSquare(double dx, double dz, double halfWidth) {
        return Math.abs(dx) <= halfWidth && Math.abs(dz) <= halfWidth;
    }

    public static boolean isInsideHorizontalRadius(double dx, double dz, double radius) {
        return dx * dx + dz * dz <= radius * radius;
    }

    public static boolean isInHorizontalCone(double dx, double dz, double forwardX, double forwardZ,
                                             double range, double width, double halfAngleDegrees) {
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance <= 1.0E-6D || distance > range) return false;
        double along = dx * forwardX + dz * forwardZ;
        if (along <= 0.0D) return false;
        double lateral = Math.abs(dx * forwardZ - dz * forwardX);
        double angleCos = along / distance;
        return lateral <= width * 0.5D && angleCos >= Math.cos(Math.toRadians(halfAngleDegrees));
    }

    public static boolean canArm(long now, long armedAt, boolean ownerHasCleared) {
        return ownerHasCleared && now >= armedAt;
    }

    public static boolean isPressureContact(double dx, double dz, double feetDeltaY,
                                            double horizontalRadius, double verticalTolerance) {
        return dx * dx + dz * dz <= horizontalRadius * horizontalRadius
                && feetDeltaY >= -0.08D && feetDeltaY <= verticalTolerance;
    }
}
