package com.grenadier.smoke;

public record SmokeCascade(
        float directionX,
        float directionZ,
        float edgeDistance,
        float dropDistance,
        float curtainWidth,
        float poolRadius
) {
    public static final float MAX_DROP = 12.0F;
    public static final float MAX_CURTAIN_WIDTH = 24.0F;
    public static final float MAX_POOL_RADIUS = 16.0F;
    public static final SmokeCascade NONE = new SmokeCascade(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);

    public boolean active() {
        return this.dropDistance >= 2.5F;
    }

    public boolean verticalAirburst() {
        return this.active() && this.edgeDistance <= 0.05F;
    }
}
