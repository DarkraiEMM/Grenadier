package com.grenadier.resources;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FragGrenadeVisualResourcesTest {
    @Test
    void itemModelUsesDedicatedTexture() throws IOException {
        String model = Files.readString(resource("models", "item", "frag_grenade.json"));
        assertTrue(model.contains("\"layer0\": \"grenadier:item/frag_grenade\""));
    }

    @Test
    void texturesUseApprovedPixelDimensions() throws IOException {
        BufferedImage item = ImageIO.read(resource("textures", "item", "frag_grenade.png").toFile());
        BufferedImage entity = ImageIO.read(resource("textures", "entity", "frag_grenade.png").toFile());
        assertEquals(16, item.getWidth());
        assertEquals(16, item.getHeight());
        assertEquals(64, entity.getWidth());
        assertEquals(64, entity.getHeight());
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
