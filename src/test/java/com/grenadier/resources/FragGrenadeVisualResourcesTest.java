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

class FragGrenadeVisualResourcesTest {
    @Test
    void itemModelUsesApprovedSharedMaterialsAndDistinctGeometry() throws IOException {
        String model = Files.readString(resource("models", "item", "frag_grenade.json"));
        assertTrue(model.contains("grenadier:item/grenade_materials"));
        assertTrue(model.contains("body_center"));
        assertTrue(model.contains("spoon"));
    }

    @Test
    void texturesUseApprovedPixelDimensions() throws IOException {
        BufferedImage item = ImageIO.read(resource("textures", "item", "grenade_materials.png").toFile());
        BufferedImage entity = ImageIO.read(resource("textures", "entity", "frag_grenade.png").toFile());
        assertEquals(64, item.getWidth());
        assertEquals(64, item.getHeight());
        assertEquals(64, entity.getWidth());
        assertEquals(64, entity.getHeight());
    }

    @Test
    void allProjectileRenderersUseGrenadierTextureNamespace() throws IOException {
        Path clientSources = Path.of(System.getProperty("test.projectDir"), "src", "main", "java",
                "com", "grenadier", "client");
        for (String projectile : new String[]{"IncendiaryGrenade", "Flashbang"}) {
            String renderer = Files.readString(clientSources.resolve(projectile + "ProjectileRenderer.java"));
            String model = Files.readString(clientSources.resolve(projectile + "ProjectileModel.java"));
            assertTrue(renderer.contains("GrenadierMod.MODID"), projectile + " renderer must use grenadier textures");
            assertTrue(model.contains("GrenadierMod.path("), projectile + " model layer must use grenadier namespace");
        }
    }

    @Test
    void fragAndImpactProjectilesReuseTheirApprovedItemModels() throws IOException {
        Path clientSources = Path.of(System.getProperty("test.projectDir"), "src", "main", "java",
                "com", "grenadier", "client");
        for (String projectile : new String[]{"FragGrenade", "ImpactGrenade"}) {
            String renderer = Files.readString(clientSources.resolve(projectile + "ProjectileRenderer.java"));
            assertTrue(renderer.contains("context.getItemRenderer()"));
            assertTrue(renderer.contains("ItemDisplayContext.GROUND"));
            assertFalse(renderer.contains("poseStack.scale(-"));
        }
    }

    private static Path resource(String... parts) {
        Path path = Path.of(System.getProperty("test.projectDir"), "src", "main", "resources",
                "assets", "grenadier");
        for (String part : parts) {
            path = path.resolve(part);
        }
        return path;
    }
}
