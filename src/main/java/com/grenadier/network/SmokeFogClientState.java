package com.grenadier.network;

import net.minecraft.Util;
import net.minecraft.util.Mth;

public final class SmokeFogClientState {
    private static final long FADE_MILLIS = 350L;
    private static long expiresAtMillis;

    private SmokeFogClientState() {
    }

    public static void refresh(int durationTicks) {
        int safeDuration = Mth.clamp(durationTicks, 1, 40);
        expiresAtMillis = Util.getMillis() + safeDuration * 50L;
    }

    public static float strength() {
        long remaining = expiresAtMillis - Util.getMillis();
        if (remaining <= 0L) {
            return 0.0F;
        }
        if (remaining >= FADE_MILLIS) {
            return 1.0F;
        }
        return remaining / (float) FADE_MILLIS;
    }
}
