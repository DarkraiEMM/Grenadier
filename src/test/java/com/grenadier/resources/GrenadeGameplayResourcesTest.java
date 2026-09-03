package com.grenadier.resources;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GrenadeGameplayResourcesTest {
    @Test
    void fragmentationExplosionUsesProjectileAsDirectSource() throws IOException {
        String source = javaSource("grenade", "FragGrenadeProjectile.java");
        assertTrue(source.contains("level.explode(this,"));
        assertFalse(source.contains("level.explode(owner == null ? this : owner"));
    }

    @Test
    void incendiaryDamageMatchesDisplayedRadiusAndDoesNotUseAirAsFallback() throws IOException {
        String field = javaSource("incendiary", "IncendiaryFieldEntity.java");
        String projectile = javaSource("incendiary", "IncendiaryGrenadeProjectile.java");
        String surfaceLocator = javaSource("util", "SurfaceLocator.java");
        assertTrue(field.contains("dx * dx + dz * dz <= radius * radius"));
        assertTrue(projectile.contains("SurfaceLocator.findSurfaceBelow"));
        assertTrue(surfaceLocator.contains("SableCompatibility.projectToGlobal"),
                "surface hits from Sable plot space must be projected back to world space");
        assertTrue(surfaceLocator.contains("return Optional.empty();"));
        assertTrue(surfaceLocator.contains("ClipContext.Block.COLLIDER"));
        assertFalse(projectile.contains("return origin;"));
    }

    @Test
    void impactGrenadeHasCompleteItemResources() throws IOException {
        String model = Files.readString(resource("models", "item", "impact_grenade.json"));
        String recipe = Files.readString(data("recipe", "impact_grenade.json"));
        BufferedImage texture = ImageIO.read(resource("textures", "item", "grenade_materials.png").toFile());
        assertTrue(model.contains("grenadier:item/grenade_materials"));
        assertTrue(model.contains("yellow_cap"));
        assertTrue(model.contains("round_center"));
        assertTrue(recipe.contains("grenadier:impact_grenade"));
        assertEquals(64, texture.getWidth());
        assertEquals(64, texture.getHeight());
    }

    @Test
    void signalBeaconUsesApprovedBlockModelAndVanillaGuiTransform() throws IOException {
        String model = Files.readString(resource("models", "item", "tactical_signal_beacon.json"));
        assertTrue(model.contains("grenadier:block/tactical_signal_beacon"));
        assertTrue(model.contains("\"rotation\": [30, 225, 0]"));
        assertTrue(model.contains("\"scale\": [0.625, 0.625, 0.625]"));
    }

    private static String javaSource(String packageName, String file) throws IOException {
        Path path = Path.of(System.getProperty("test.projectDir"), "src", "main", "java",
                "com", "grenadier", packageName, file);
        return Files.readString(path);
    }

    private static Path resource(String... parts) {
        return path(Path.of(System.getProperty("test.projectDir"), "src", "main", "resources",
                "assets", "grenadier"), parts);
    }

    private static Path data(String... parts) {
        return path(Path.of(System.getProperty("test.projectDir"), "src", "main", "resources",
                "data", "grenadier"), parts);
    }

    private static Path path(Path root, String... parts) {
        Path path = root;
        for (String part : parts) {
            path = path.resolve(part);
        }
        return path;
    }
}
