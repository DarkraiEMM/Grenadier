package com.grenadier.smoke;

import com.grenadier.GrenadierConfig;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public record SmokeCloud(UUID id, ResourceKey<Level> level, Vec3 center, double radius, double density,
                         long createdGameTime, long expiresGameTime, int color, SmokeCascade cascade) {
    public boolean expired(long gameTime) {
        return gameTime >= this.expiresGameTime;
    }

    public long age(long gameTime) {
        return Math.max(0L, gameTime - this.createdGameTime);
    }

    public long remaining(long gameTime) {
        return Math.max(0L, this.expiresGameTime - gameTime);
    }

    public double effectiveRadius(long gameTime) {
        double progress = Math.min(1.0D, this.age(gameTime) / (double) GrenadierConfig.DEPLOY_TICKS.get());
        double eased = 1.0D - Math.pow(1.0D - progress, 3.0D);
        return Math.max(0.6D, this.radius * eased);
    }

    public double deployProgress(long gameTime) {
        if (GrenadierConfig.DEPLOY_TICKS.get() <= 0) {
            return 1.0D;
        }
        double progress = Math.min(1.0D, this.age(gameTime) / (double) GrenadierConfig.DEPLOY_TICKS.get());
        return 1.0D - Math.pow(1.0D - progress, 3.0D);
    }
}
