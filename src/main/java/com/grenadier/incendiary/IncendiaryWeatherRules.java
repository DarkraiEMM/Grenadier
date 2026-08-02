package com.grenadier.incendiary;

final class IncendiaryWeatherRules {
    private IncendiaryWeatherRules() {
    }

    static int rainDurationTicks(int normalDurationTicks) {
        return Math.max(1, normalDurationTicks / 2);
    }

    static boolean canIgnite(boolean exposedToRain) {
        return !exposedToRain;
    }
}
