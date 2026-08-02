package com.grenadier.network;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmokeVolumeDataTest {
    @Test
    void acceptsBoundedFiniteSmoke() {
        assertTrue(data(6.0F, 100L, 500L, 30, 0.0D).valid());
    }

    @Test
    void rejectsNonFiniteCoordinateAndInvalidRadius() {
        assertFalse(data(6.0F, 100L, 500L, 30, Double.NaN).valid());
        assertFalse(data(0.0F, 100L, 500L, 30, 0.0D).valid());
        assertFalse(data(33.0F, 100L, 500L, 30, 0.0D).valid());
    }

    @Test
    void rejectsInvalidLifetimeAndDeployDuration() {
        assertFalse(data(6.0F, 500L, 100L, 30, 0.0D).valid());
        assertFalse(data(6.0F, 100L, 500L, SmokeVolumeData.MAX_DEPLOY_TICKS + 1, 0.0D).valid());
    }

    @Test
    void validatesCascadeBoundsAndDirection() {
        SmokeVolumeData validCascade = new SmokeVolumeData(
                UUID.randomUUID(), 0.0D, 64.0D, 0.0D, 9.0F,
                0xFF4C504C, 100L, 500L, 30,
                1.0F, 0.0F, 6.0F, 8.0F, 7.0F, 4.5F
        );
        SmokeVolumeData invalidCascade = new SmokeVolumeData(
                UUID.randomUUID(), 0.0D, 64.0D, 0.0D, 9.0F,
                0xFF4C504C, 100L, 500L, 30,
                0.0F, 0.0F, 6.0F, 8.0F, 7.0F, 4.5F
        );
        assertTrue(validCascade.valid());
        assertFalse(invalidCascade.valid());
        SmokeVolumeData invalidCurtain = new SmokeVolumeData(
                UUID.randomUUID(), 0.0D, 64.0D, 0.0D, 9.0F,
                0xFF4C504C, 100L, 500L, 30,
                1.0F, 0.0F, 6.0F, 8.0F, 0.25F, 4.5F
        );
        assertFalse(invalidCurtain.valid());
    }

    @Test
    void snapshotSizeIsBounded() {
        assertTrue(SmokeVolumeData.validSnapshotSize(0));
        assertTrue(SmokeVolumeData.validSnapshotSize(SmokeVolumeData.MAX_SNAPSHOT_SMOKES));
        assertFalse(SmokeVolumeData.validSnapshotSize(-1));
        assertFalse(SmokeVolumeData.validSnapshotSize(SmokeVolumeData.MAX_SNAPSHOT_SMOKES + 1));
    }

    private static SmokeVolumeData data(
            float radius,
            long created,
            long expires,
            int deployTicks,
            double x
    ) {
        return new SmokeVolumeData(UUID.randomUUID(), x, 64.0D, 0.0D, radius,
                0xFF4C504C, created, expires, deployTicks,
                0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
    }
}
