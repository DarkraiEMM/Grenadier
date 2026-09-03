package com.grenadier.mine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MineMathTest {
    @Test
    void coneAcceptsForwardTargetsAndRejectsRearOrWideTargets() {
        assertTrue(MineGeometry.isInHorizontalCone(0.0D, -4.0D, 0.0D, -1.0D, 5.0D, 4.0D, 42.0D));
        assertFalse(MineGeometry.isInHorizontalCone(0.0D, 2.0D, 0.0D, -1.0D, 5.0D, 4.0D, 42.0D));
        assertFalse(MineGeometry.isInHorizontalCone(3.0D, -4.0D, 0.0D, -1.0D, 5.0D, 4.0D, 42.0D));
    }

    @Test
    void armingRequiresBothDelayAndOwnerClearance() {
        assertFalse(MineGeometry.canArm(100L, 100L, false));
        assertFalse(MineGeometry.canArm(99L, 100L, true));
        assertTrue(MineGeometry.canArm(100L, 100L, true));
    }

    @Test
    void pressureMineRequiresFeetDirectlyOverThePlate() {
        assertTrue(MineGeometry.isPressureContact(0.18D, -0.12D, 0.01D, 0.48D, 0.28D));
        assertFalse(MineGeometry.isPressureContact(0.72D, 0.0D, 0.01D, 0.48D, 0.28D));
        assertFalse(MineGeometry.isPressureContact(0.0D, 0.0D, 0.65D, 0.48D, 0.28D));
    }

    @Test
    void thermiteCoreIsAnExactThreeByThreeSquare() {
        assertTrue(MineGeometry.isInsideSquare(1.5D, -1.5D, 1.5D));
        assertFalse(MineGeometry.isInsideSquare(1.51D, 0.0D, 1.5D));
        assertTrue(MineGeometry.isInsideHorizontalRadius(1.4D, 0.0D, 1.4D));
        assertFalse(MineGeometry.isInsideHorizontalRadius(1.41D, 0.0D, 1.4D));
    }
}
