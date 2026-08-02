package com.grenadier.network;

import net.minecraft.Util;
import net.minecraft.util.Mth;

public final class FlashbangClientState {
    private static float peakIntensity;
    private static long whiteUntilMillis;
    private static long expiresAtMillis;

    private FlashbangClientState() {
    }

    public static void apply(int intensityMilli, int whiteTicks, int fadeTicks) {
        float intensity = Mth.clamp(intensityMilli, 0, 1000) / 1000.0F;
        int safeWhiteTicks = Mth.clamp(whiteTicks, 0, 100);
        int safeFadeTicks = Mth.clamp(fadeTicks, 1, 200);
        if (intensity <= 0.0F) {
            return;
        }
        long now = Util.getMillis();
        peakIntensity = Math.max(strengthAt(now), intensity);
        whiteUntilMillis = Math.max(whiteUntilMillis, now + safeWhiteTicks * 50L);
        expiresAtMillis = Math.max(expiresAtMillis, now + (safeWhiteTicks + safeFadeTicks) * 50L);
    }

    public static float strength() {
        return strengthAt(Util.getMillis());
    }

    private static float strengthAt(long now) {
        if (now >= expiresAtMillis) {
            peakIntensity = 0.0F;
            return 0.0F;
        }
        if (now <= whiteUntilMillis) {
            return peakIntensity;
        }
        long fadeDuration = Math.max(1L, expiresAtMillis - whiteUntilMillis);
        float remaining = Mth.clamp((expiresAtMillis - now) / (float) fadeDuration, 0.0F, 1.0F);
        return peakIntensity * remaining * remaining * (3.0F - 2.0F * remaining);
    }
}
