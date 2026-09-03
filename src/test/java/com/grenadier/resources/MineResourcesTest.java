package com.grenadier.resources;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MineResourcesTest {
    private static final String[] MINES = {
            "anti_personnel_mine", "directional_fragmentation_mine", "thermite_mine"
    };

    @Test
    void everyMineHasBlockstateModelsRecipeAndLoot() throws IOException {
        for (String mine : MINES) {
            assertTrue(Files.exists(asset("blockstates", mine + ".json")), mine + " blockstate");
            assertTrue(Files.exists(asset("models", "block", mine + ".json")), mine + " block model");
            assertTrue(Files.exists(asset("models", "item", mine + ".json")), mine + " item model");
            assertTrue(Files.exists(data("recipe", mine + ".json")), mine + " recipe");
            assertTrue(Files.exists(data("loot_table", "blocks", mine + ".json")), mine + " loot");
        }
    }

    @Test
    void mineModelsUseTheExactBlockbenchPixelTextures() throws IOException {
        String[][] exports = {
                {"anti_personnel_mine", "ap", "ap_shell", "ap_cap", "ap_dark", "ap_yellow"},
                {"directional_fragmentation_mine", "claymore", "cl_shell", "cl_face", "cl_metal", "cl_yellow"},
                {"thermite_mine", "thermite", "th_frame", "th_edge", "th_cell", "th_hot"}
        };
        for (String[] export : exports) {
            String model = Files.readString(asset("models", "block", export[0] + ".json"));
            for (int i = 2; i < export.length; i++) {
                Path texture = asset("textures", "block", "mines", "blockbench", export[1], export[i] + ".png");
                assertTrue(Files.exists(texture), export[i] + " Blockbench texture");
                BufferedImage image = ImageIO.read(texture.toFile());
                assertEquals(16, image.getWidth(), export[i] + " texture width");
                assertEquals(16, image.getHeight(), export[i] + " texture height");
                assertTrue(model.contains("grenadier:block/mines/blockbench/" + export[1] + "/" + export[i]),
                        export[i] + " exact texture reference");
            }
        }
    }

    @Test
    void thermiteUsesVanillaParticlesAndHeavyTargetTag() throws IOException {
        String burst = javaSource("mine", "ThermiteBurstEntity.java");
        assertTrue(burst.contains("ParticleTypes.LAVA"));
        assertTrue(burst.contains("ParticleTypes.FLAME"));
        assertTrue(burst.contains("ParticleTypes.ASH"));
        assertTrue(Files.exists(data("tags", "entity_type", "thermite_heavy_targets.json")));
        assertTrue(Files.exists(data("tags", "entity_type", "mine_heavy_targets.json")));
    }

    @Test
    void thermiteBurstDealsDirectDamageWithoutExplosionKnockback() throws IOException {
        String effects = javaSource("mine", "MineEffects.java");
        String config = Files.readString(Path.of("src/main/java/com/grenadier/GrenadierConfig.java"));
        assertTrue(effects.contains("damageThermiteBurst(level, center, owner)"));
        assertTrue(effects.contains("THERMITE_MINE_BURST_DAMAGE"));
        assertTrue(effects.contains("THERMITE_MINE_ARMOR_PIERCE_RATIO"));
        assertTrue(effects.contains("THERMITE_MINE_CORE_HALF_WIDTH"));
        assertTrue(effects.contains("THERMITE_MINE_SPLASH_DAMAGE"));
        assertTrue(effects.contains("THERMITE_MINE_SPLASH_IGNITE_SECONDS"));
        assertTrue(effects.contains("hurtWithPartialArmorPiercing"));
        assertTrue(!effects.contains("explode(level, center, GrenadierConfig.THERMITE_MINE_EXPLOSION_RADIUS"));
        assertTrue(config.contains("defineInRange(\"explosionRadius\", 4.5D"));
        assertTrue(config.contains("defineInRange(\"flameRadius\", 4.5D"));
    }

    @Test
    void deploymentPathsUseReliablePlacementRules() throws IOException {
        String antiPersonnelItem = javaSource("mine", "AntiPersonnelMineItem.java");
        String thermiteProjectile = javaSource("mine", "ThermiteMineProjectile.java");
        String deployedMine = javaSource("mine", "DeployedMineEntity.java");
        String modRegistration = javaSource("GrenadierMod.java");
        assertTrue(antiPersonnelItem.contains("extends BlockItem"));
        assertTrue(antiPersonnelItem.contains("DeployedMineEntity"));
        assertTrue(thermiteProjectile.contains("SurfaceLocator.findSurfaceBelow"));
        assertTrue(thermiteProjectile.contains("new DeployedMineEntity"));
        assertTrue(deployedMine.contains("findPressureTarget"));
        assertTrue(deployedMine.contains("getBoundingBox().minY"));
        assertTrue(deployedMine.contains("DamageTypeTags.IS_PROJECTILE"));
        assertTrue(deployedMine.contains("source.getDirectEntity() instanceof Projectile"));
        assertTrue(deployedMine.contains("serverLevel && !triggered"),
                "projectiles must detonate a placed mine even before its proximity fuze arms");
        assertTrue(deployedMine.contains("case ANTI_PERSONNEL -> EntityDimensions.fixed(0.42F, 0.10F)"));
        assertTrue(deployedMine.contains("case DIRECTIONAL -> EntityDimensions.fixed(0.50F, 0.48F)"));
        assertTrue(deployedMine.contains("case THERMITE -> EntityDimensions.fixed(0.40F, 0.09F)"));
        assertTrue(deployedMine.contains("onSyncedDataUpdated(EntityDataAccessor<?> key)"));
        assertTrue(deployedMine.contains("refreshDimensions()"));
        assertTrue(modRegistration.contains(".sized(0.42F, 0.10F).clientTrackingRange(10)"),
                "entity type default dimensions must match the default anti-personnel kind on clients");

        String legacyMine = javaSource("mine", "MineBlockEntity.java");
        assertTrue(legacyMine.contains("if (!triggered && level instanceof ServerLevel serverLevel)"),
                "legacy block mines must use the same immediate projectile detonation rule");
    }

    @Test
    void mineModelsDoNotContainSubPixelUvSpansThatBleedUnderMipmaps() throws IOException {
        Pattern uvPattern = Pattern.compile("\\\"uv\\\"\\s*:\\s*\\[\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*,\\s*(-?\\d+(?:\\.\\d+)?)\\s*]");
        for (String name : List.of("anti_personnel_mine", "anti_personnel_mine_buried",
                "directional_fragmentation_mine", "thermite_mine")) {
            Matcher matcher = uvPattern.matcher(Files.readString(asset("models", "block", name + ".json")));
            while (matcher.find()) {
                double width = Math.abs(Double.parseDouble(matcher.group(3)) - Double.parseDouble(matcher.group(1)));
                double height = Math.abs(Double.parseDouble(matcher.group(4)) - Double.parseDouble(matcher.group(2)));
                assertTrue(width == 0.0D || width >= 1.0D, name + " contains a sub-pixel UV width");
                assertTrue(height == 0.0D || height >= 1.0D, name + " contains a sub-pixel UV height");
            }
        }
    }

    @Test
    void directionalMineModelKeepsClaymoreSilhouetteParts() throws IOException {
        String model = Files.readString(asset("models", "block", "directional_fragmentation_mine.json"));
        for (String part : new String[]{"body_center", "body_left", "body_right", "front_center",
                "mark_bar", "mark_stem", "sight_top", "leg_l_out", "leg_r_out"}) {
            assertTrue(model.contains(part), part);
        }
    }

    @Test
    void approvedMineModelsKeepTheirCompactSilhouettes() throws IOException {
        String antiPersonnel = Files.readString(asset("models", "block", "anti_personnel_mine.json"))
                .replace("\r\n", "\n");
        for (String part : new String[]{"shell_north", "shell_center", "shell_south",
                "cap_shadow_center", "pressure_cap_center", "safe_mark"}) {
            assertTrue(antiPersonnel.contains(part), part);
        }
        assertTrue(antiPersonnel.contains("5.25"), "anti-personnel footprint starts at x=5.25");
        assertTrue(antiPersonnel.contains("10.75"), "anti-personnel footprint ends at x=10.75");
        assertTrue(antiPersonnel.contains("1.02"), "anti-personnel mine remains low profile");

        String thermite = Files.readString(asset("models", "block", "thermite_mine.json"));
        for (String part : new String[]{"thin_base", "rail_n", "rail_s", "rail_w", "rail_e",
                "cross_x", "cross_z", "cell_nw", "cell_ne", "cell_sw", "cell_se", "igniter"}) {
            assertTrue(thermite.contains(part), part);
        }
        assertTrue(thermite.contains("5.35"), "thermite footprint remains compact");
        assertTrue(thermite.contains("0.69"), "thermite remains a thin plate");

        String antiPersonnelItem = Files.readString(asset("models", "item", "anti_personnel_mine.json"));
        String thermiteItem = Files.readString(asset("models", "item", "thermite_mine.json"));
        assertTrue(antiPersonnelItem.contains("\"elements\""), "anti-personnel item has dedicated geometry");
        assertTrue(thermiteItem.contains("\"elements\""), "thermite item has dedicated geometry");
        assertTrue(thermiteItem.contains("thin_base"), "thermite item preserves approved silhouette");
        assertTrue(antiPersonnelItem.contains("9.75"), "anti-personnel item uses approved GUI centering");
        assertTrue(thermiteItem.contains("9.75"), "thermite item uses approved GUI centering");
        assertTrue(antiPersonnelItem.contains("2.0") || antiPersonnelItem.contains("2,"),
                "anti-personnel item uses calibrated GUI scale");
        assertTrue(thermiteItem.contains("2.0") || thermiteItem.contains("2,"),
                "thermite item uses calibrated GUI scale");
    }

    @Test
    void serverConfigExposesMineBalancingFields() throws IOException {
        String config = javaSource("GrenadierConfig.java");
        for (String key : new String[]{"ANTI_PERSONNEL_MINE_ARM_TICKS", "ANTI_PERSONNEL_MINE_TRIGGER_RADIUS", "DIRECTIONAL_MINE_RANGE",
                "DIRECTIONAL_MINE_HALF_ANGLE", "THERMITE_MINE_THROW_SPEED", "THERMITE_MINE_BURST_TICKS",
                "THERMITE_MINE_HEAVY_BONUS_DAMAGE", "THERMITE_MINE_BURST_HEAVY_BONUS_DAMAGE",
                "ANTI_PERSONNEL_MINE_HEAVY_BONUS_DAMAGE", "DIRECTIONAL_MINE_HEAVY_BONUS_DAMAGE",
                "MINES_DAMAGE_OWNER"}) {
            assertTrue(config.contains(key), key);
        }
    }

    private static String javaSource(String... parts) throws IOException {
        Path path = Path.of(System.getProperty("test.projectDir"), "src", "main", "java", "com", "grenadier");
        for (String part : parts) path = path.resolve(part);
        return Files.readString(path);
    }

    private static Path asset(String... parts) { return path(Path.of(System.getProperty("test.projectDir"), "src", "main", "resources", "assets", "grenadier"), parts); }
    private static Path data(String... parts) { return path(Path.of(System.getProperty("test.projectDir"), "src", "main", "resources", "data", "grenadier"), parts); }
    private static Path path(Path root, String... parts) { Path path = root; for (String part : parts) path = path.resolve(part); return path; }
}
