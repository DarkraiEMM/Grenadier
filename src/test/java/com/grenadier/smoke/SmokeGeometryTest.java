package com.grenadier.smoke;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmokeGeometryTest {
    @Test
    void detectsSegmentPassingThroughCloud() {
        assertTrue(SmokeGeometryMath.segmentIntersectsSphere(-10, 0, 0, 10, 0, 0, 0, 0, 0, 3));
    }

    @Test
    void ignoresCloudBehindTheTarget() {
        assertFalse(SmokeGeometryMath.segmentIntersectsSphere(0, 0, 0, 2, 0, 0, 8, 0, 0, 2));
    }

    @Test
    void handlesStationarySightPoint() {
        assertTrue(SmokeGeometryMath.segmentIntersectsSphere(1, 1, 1, 1, 1, 1, 0, 0, 0, 2));
    }

    @Test
    void detectsSegmentCrossingCurtainBounds() {
        assertTrue(SmokeGeometryMath.segmentIntersectsAabb(-4, 0, 0, 4, 0, 0, 2, 3, 1));
        assertFalse(SmokeGeometryMath.segmentIntersectsAabb(-4, 4, 0, 4, 4, 0, 2, 3, 1));
    }

    @Test
    void detectsFlattenedGroundPoolWithoutMakingItTall() {
        assertTrue(SmokeGeometryMath.segmentIntersectsEllipsoid(-4, 0, 0, 4, 0, 0, 0, 0, 0, 3, 0.5, 3));
        assertFalse(SmokeGeometryMath.segmentIntersectsEllipsoid(-4, 1, 0, 4, 1, 0, 0, 0, 0, 3, 0.5, 3));
    }
}
