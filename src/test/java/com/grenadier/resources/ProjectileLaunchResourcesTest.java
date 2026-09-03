package com.grenadier.resources;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectileLaunchResourcesTest {
    @Test
    void allHandThrownGrenadesShareTheSameHandOrigin() throws IOException {
        for (String source : new String[]{
                "grenade/FragGrenadeItem.java",
                "grenade/ImpactGrenadeItem.java",
                "flashbang/FlashbangItem.java",
                "incendiary/IncendiaryGrenadeItem.java",
                "signal/SignalFlareItem.java"
        }) {
            String item = Files.readString(Path.of("src/main/java/com/grenadier").resolve(source));
            assertTrue(item.contains("ProjectileLaunch.placeAtHand(projectile, serverPlayer, hand)"), source);
        }
    }
}
