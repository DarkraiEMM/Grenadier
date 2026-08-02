package com.grenadier.incendiary;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IncendiaryWeatherRulesTest {
    @Test
    void rainHalvesTheConfiguredFieldDuration() {
        assertEquals(90, IncendiaryWeatherRules.rainDurationTicks(180));
        assertEquals(1, IncendiaryWeatherRules.rainDurationTicks(1));
    }

    @Test
    void rainSuppressesIgnitionButNotTheDamagePulse() {
        assertFalse(IncendiaryWeatherRules.canIgnite(true));
        assertTrue(IncendiaryWeatherRules.canIgnite(false));
    }
}
