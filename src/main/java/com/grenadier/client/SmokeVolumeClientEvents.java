package com.grenadier.client;

import com.grenadier.GrenadierMod;
import com.grenadier.network.SmokeFogClientState;
import foundry.veil.api.client.render.VeilRenderSystem;
import foundry.veil.api.client.render.post.PostPipeline;
import foundry.veil.api.client.render.post.PostProcessingManager;
import foundry.veil.forge.event.ForgeVeilPostProcessingEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.joml.Vector4f;

import java.util.List;

@EventBusSubscriber(modid = GrenadierMod.MODID, value = Dist.CLIENT)
public final class SmokeVolumeClientEvents {
    private static final ResourceLocation PIPELINE_ID = GrenadierMod.path("smoke_volume");
    private static boolean volumeActive;
    private static boolean warnedUnavailable;
    private static int particleTicker;

    private SmokeVolumeClientEvents() {
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            deactivate();
            ClientSmokeVolumeManager.clear();
            return;
        }

        boolean hasSmoke = !ClientSmokeVolumeManager.allActive().isEmpty();
        PostProcessingManager manager = VeilRenderSystem.renderer().getPostProcessingManager();
        if (!hasSmoke) {
            if (manager.isActive(PIPELINE_ID)) {
                manager.remove(PIPELINE_ID);
            }
            volumeActive = false;
            return;
        }

        PostPipeline pipeline = manager.getPipeline(PIPELINE_ID);
        if (pipeline == null || !pipeline.hasUniform("SmokeCount") || !pipeline.hasUniform("SmokeSpheres")) {
            volumeActive = false;
            if (!warnedUnavailable) {
                warnedUnavailable = true;
                GrenadierMod.LOGGER.warn("Smoke volume pipeline {} is unavailable; using particle fallback", PIPELINE_ID);
            }
            particleTicker++;
            if (particleTicker % 4 == 0) {
                spawnLocalParticles(minecraft, false);
            }
            return;
        }

        if (IrisVeilBridge.shouldUseBridge()) {
            if (manager.isActive(PIPELINE_ID)) {
                manager.remove(PIPELINE_ID);
            }
            volumeActive = true;
            warnedUnavailable = false;
            return;
        }

        if (!manager.isActive(PIPELINE_ID)) {
            manager.add(PIPELINE_ID);
        }
        volumeActive = manager.isActive(PIPELINE_ID);
        if (volumeActive) {
            warnedUnavailable = false;
        }
        particleTicker++;
        if (particleTicker % 4 == 0) {
            spawnLocalParticles(minecraft, volumeActive);
        }
    }

    @SubscribeEvent
    public static void beforePost(ForgeVeilPostProcessingEvent.Pre event) {
        if (!PIPELINE_ID.equals(event.getName())) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        uploadUniforms(event.getPipeline(), minecraft);
    }

    static boolean hasRenderableSmoke() {
        return Minecraft.getInstance().level != null && !ClientSmokeVolumeManager.allActive().isEmpty();
    }

    static void renderAfterIrisFinalPass() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || ClientSmokeVolumeManager.allActive().isEmpty()) {
            return;
        }
        PostProcessingManager manager = VeilRenderSystem.renderer().getPostProcessingManager();
        PostPipeline pipeline = manager.getPipeline(PIPELINE_ID);
        if (pipeline == null || !pipeline.hasUniform("SmokeCount") || !pipeline.hasUniform("SmokeSpheres")) {
            return;
        }
        uploadUniforms(pipeline, minecraft);
        manager.runPipeline(pipeline);
        volumeActive = true;
    }

    private static void uploadUniforms(PostPipeline pipeline, Minecraft minecraft) {

        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        float partialTick = minecraft.getTimer().getGameTimeDeltaPartialTick(true);
        List<ClientSmokeVolumeManager.RenderedSmoke> smokes =
                ClientSmokeVolumeManager.nearest(camera, partialTick);
        pipeline.getUniformSafe("SmokeCount").setInt(smokes.size());
        pipeline.getUniformSafe("SmokeTime").setFloat(ClientSmokeVolumeManager.serverGameTime() + partialTick);
        float interiorFogStrength = IrisVeilBridge.shouldUseBridge()
                ? SmokeFogClientState.strength()
                : 0.0F;
        pipeline.getUniformSafe("SmokeInteriorStrength").setFloat(interiorFogStrength);

        Vector4f[] spheres = new Vector4f[ClientSmokeVolumeManager.MAX_RENDERED_SMOKES];
        Vector4f[] colors = new Vector4f[ClientSmokeVolumeManager.MAX_RENDERED_SMOKES];
        Vector4f[] parameters = new Vector4f[ClientSmokeVolumeManager.MAX_RENDERED_SMOKES];
        Vector4f[] cascades = new Vector4f[ClientSmokeVolumeManager.MAX_RENDERED_SMOKES];
        Vector4f[] cascadeShapes = new Vector4f[ClientSmokeVolumeManager.MAX_RENDERED_SMOKES];
        for (int i = 0; i < spheres.length; i++) {
            if (i < smokes.size()) {
                ClientSmokeVolumeManager.RenderedSmoke smoke = smokes.get(i);
                Vec3 relative = smoke.center().subtract(camera);
                spheres[i] = new Vector4f((float) relative.x, (float) relative.y, (float) relative.z, smoke.radius());
                colors[i] = colorAndDensity(smoke.data().color(), smoke.data().density());
                long seedBits = smoke.data().id().getMostSignificantBits() ^ smoke.data().id().getLeastSignificantBits();
                float angle = (seedBits & 0xFFFFL) / 65535.0F * (float) (Math.PI * 2.0D);
                float deployScale = smoke.data().radius() <= 0.0F ? 1.0F : smoke.radius() / smoke.data().radius();
                parameters[i] = new Vector4f((float) Math.cos(angle), (float) Math.sin(angle), 1.05F, deployScale);
                cascades[i] = new Vector4f(
                        smoke.data().cascadeDirectionX(),
                        smoke.data().cascadeDirectionZ(),
                        smoke.data().cascadeEdgeDistance(),
                        smoke.data().cascadeDropDistance() * deployScale
                );
                cascadeShapes[i] = new Vector4f(
                        smoke.data().cascadeCurtainWidth() * deployScale,
                        smoke.data().cascadePoolRadius() * deployScale,
                        angle,
                        0.0F
                );
            } else {
                spheres[i] = new Vector4f();
                colors[i] = new Vector4f();
                parameters[i] = new Vector4f();
                cascades[i] = new Vector4f();
                cascadeShapes[i] = new Vector4f();
            }
        }
        pipeline.getUniformSafe("SmokeSpheres").setVectors(spheres);
        pipeline.getUniformSafe("SmokeColors").setVectors(colors);
        pipeline.getUniformSafe("SmokeParams").setVectors(parameters);
        pipeline.getUniformSafe("SmokeCascades").setVectors(cascades);
        pipeline.getUniformSafe("SmokeCascadeShapes").setVectors(cascadeShapes);
    }

    @SubscribeEvent
    public static void loggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        deactivate();
        IrisVeilBridge.release();
        ClientSmokeVolumeManager.clear();
    }

    public static boolean isVolumeActive() {
        return volumeActive;
    }

    private static Vector4f colorAndDensity(int argb, float density) {
        float red = ((argb >> 16) & 0xFF) / 255.0F;
        float green = ((argb >> 8) & 0xFF) / 255.0F;
        float blue = (argb & 0xFF) / 255.0F;
        float luminance = red * 0.2126F + green * 0.7152F + blue * 0.0722F;
        float neutralBase = luminance + (0.42F - luminance) * 0.48F;
        float saturation = 0.40F;
        red = neutralBase + (red - luminance) * saturation;
        green = neutralBase + (green - luminance) * saturation;
        blue = neutralBase + (blue - luminance) * saturation;
        return new Vector4f(red, green, blue, density);
    }

    private static void spawnLocalParticles(Minecraft minecraft, boolean volumeMode) {
        if (minecraft.level == null) {
            return;
        }
        Vec3 camera = minecraft.gameRenderer.getMainCamera().getPosition();
        List<ClientSmokeVolumeManager.RenderedSmoke> smokes = ClientSmokeVolumeManager.nearest(camera, 0.0F);
        if (volumeMode) {
            return;
        }
        int countPerCloud = 28;
        for (ClientSmokeVolumeManager.RenderedSmoke smoke : smokes) {
            int particleColor = (volumeMode ? 0x90000000 : 0xE8000000) | (smoke.data().color() & 0xFFFFFF);
            ColorParticleOption particle = ColorParticleOption.create(
                    com.grenadier.GrenadierMod.COLORED_SIGNAL_SMOKE.get(), particleColor);
            boolean cascading = smoke.data().cascadeDropDistance() >= 2.5F;
            double bodyDescent = cascading
                    ? smoke.data().cascadeDropDistance() * smoke.radius() / smoke.data().radius()
                    : 0.0D;
            for (int i = 0; i < countPerCloud; i++) {
                double angle = minecraft.level.random.nextDouble() * Math.PI * 2.0D;
                double radialFraction = volumeMode
                        ? 0.72D + minecraft.level.random.nextDouble() * 0.25D
                        : Math.sqrt(minecraft.level.random.nextDouble()) * 0.92D;
                double horizontalRadius = smoke.radius() * radialFraction;
                double x = smoke.center().x + Math.cos(angle) * horizontalRadius;
                double z = smoke.center().z + Math.sin(angle) * horizontalRadius;
                double yFraction = minecraft.level.random.nextDouble() * 1.55D - 0.62D;
                double y = smoke.center().y - bodyDescent + yFraction * smoke.radius() * 0.72D;
                double inward = volumeMode ? -0.008D : 0.0D;
                double xSpeed = Math.cos(angle) * inward;
                double zSpeed = Math.sin(angle) * inward;
                minecraft.level.addParticle(particle, x, y, z, xSpeed, 0.006D, zSpeed);
            }
        }
    }

    private static void deactivate() {
        try {
            PostProcessingManager manager = VeilRenderSystem.renderer().getPostProcessingManager();
            if (manager.isActive(PIPELINE_ID)) {
                manager.remove(PIPELINE_ID);
            }
        } catch (RuntimeException | LinkageError ignored) {
            // The renderer may already be shutting down.
        }
        volumeActive = false;
        particleTicker = 0;
    }
}
