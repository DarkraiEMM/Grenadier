package com.grenadier.resources;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmokeProjectileBehaviorResourcesTest {
    @Test
    void ordinarySmokeVentsInAirAndDeploysItsVolumeOnARealSurface() throws IOException {
        String projectile = Files.readString(Path.of(
                System.getProperty("test.projectDir"), "src", "main", "java", "com", "grenadier",
                "signal", "SignalFlareProjectile.java"));

        assertTrue(projectile.contains("this.fuseExpired = true"));
        assertTrue(projectile.contains("this.spawnVentTrail(serverLevel)"));
        assertTrue(projectile.contains("this.onGround() || this.verticalCollisionBelow || this.resting"),
                "moving physical structures can report support through verticalCollisionBelow");
        assertTrue(projectile.contains("SableCompatibility.isTrackingStructure(this)"),
                "Sable tracking sub-level support must deploy smoke without vanilla ground flags");
        assertTrue(projectile.contains("Smoke grenade still airborne after fuse"),
                "candidate build must emit a bounded support diagnostic when deployment stalls");
        assertTrue(projectile.contains("physicalStructureHit"));
        assertTrue(projectile.contains("stickyAnchor(result, worldHitLocation)"));
        assertTrue(projectile.contains("SurfaceLocator.findSurfaceBelow"));
        assertTrue(projectile.contains("normal == Direction.UP && (this.fuseExpired"));
        assertFalse(projectile.contains("this.deploy(serverLevel, this.position(), true)"),
                "fuse expiry alone must not create a detached volumetric cloud in mid-air");
    }
}
