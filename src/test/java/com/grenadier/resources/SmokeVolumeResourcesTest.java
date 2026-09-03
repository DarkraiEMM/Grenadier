package com.grenadier.resources;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SmokeVolumeResourcesTest {
    @Test
    void pipelineBindsMainDepthAndRunsAfterWeather() throws IOException {
        String pipeline = read("pinwheel", "post", "smoke_volume.json");
        String program = read("pinwheel", "shaders", "program", "smoke_volume.json");
        assertTrue(pipeline.contains("\"renderStage\": \"after_weather\""));
        assertTrue(program.contains("\"name\": \"minecraft:main:depth\""));
    }

    @Test
    void fragmentShaderKeepsBoundedVolumeLoop() throws IOException {
        String shader = read("pinwheel", "shaders", "program", "smoke_volume.fsh");
        assertTrue(shader.contains("uniform vec4 SmokeSpheres[8]"));
        assertTrue(shader.contains("uniform vec4 SmokeCascades[8]"));
        assertTrue(shader.contains("uniform vec4 SmokeCascadeShapes[8]"));
        assertTrue(shader.contains("uniform float SmokeInteriorStrength"));
        assertTrue(shader.contains("for (int sampleIndex = 0; sampleIndex < 12; sampleIndex++)"));
        assertTrue(shader.contains("integrateCascadePlumes("));
        assertFalse(shader.contains("strandIndex"));
        assertTrue(shader.contains("sampleIndex < 24"));
        assertTrue(shader.contains("curtainWidth = max(cascadeShape.x, radius * 0.36) * 1.20"));
        assertTrue(shader.contains("smoothstep(0.25, 3.50, sceneDistance)"));
        assertFalse(shader.contains("outwardCurve"));
        assertTrue(shader.contains("vec3 edge = anchor"));
        assertTrue(shader.contains("radius * mix(0.30, 0.18, cascadeAmount) - cascade.w"));
        assertFalse(shader.contains("airburstDescent"));
        assertFalse(shader.contains("verticalAirburst ?"));
        assertTrue(shader.contains("transmittance *= 1.0 - cloudAlpha"));
    }

    private static String read(String... parts) throws IOException {
        Path path = Path.of(System.getProperty("test.projectDir"), "src", "main", "resources",
                "assets", "grenadier");
        for (String part : parts) {
            path = path.resolve(part);
        }
        return Files.readString(path);
    }
}
