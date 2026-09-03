package com.grenadier.client;

import com.grenadier.GrenadierMod;
import com.grenadier.smoke.SmokeGrenadeColors;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = GrenadierMod.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class GrenadierClientEvents {
    private GrenadierClientEvents() {
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(
                GrenadierMod.TACTICAL_SIGNAL_BEACON_BLOCK_ENTITY.get(),
                TacticalSignalBeaconRenderer::new
        );
        event.registerEntityRenderer(
                GrenadierMod.SIGNAL_FLARE_PROJECTILE.get(),
                SmokeGrenadeProjectileRenderer::new
        );
        event.registerEntityRenderer(
                GrenadierMod.INCENDIARY_GRENADE_PROJECTILE.get(),
                IncendiaryGrenadeProjectileRenderer::new
        );
        event.registerEntityRenderer(
                GrenadierMod.INCENDIARY_FIELD.get(),
                NoopRenderer::new
        );
        event.registerEntityRenderer(
                GrenadierMod.FLASHBANG_PROJECTILE.get(),
                FlashbangProjectileRenderer::new
        );
        event.registerEntityRenderer(
                GrenadierMod.FRAG_GRENADE_PROJECTILE.get(),
                FragGrenadeProjectileRenderer::new
        );
        event.registerEntityRenderer(
                GrenadierMod.IMPACT_GRENADE_PROJECTILE.get(),
                ImpactGrenadeProjectileRenderer::new
        );
        event.registerEntityRenderer(
                GrenadierMod.THERMITE_MINE_PROJECTILE.get(),
                context -> new ThrownItemRenderer<>(context, 0.42F, false)
        );
        event.registerEntityRenderer(
                GrenadierMod.THERMITE_BURST.get(),
                NoopRenderer::new
        );
        event.registerEntityRenderer(
                GrenadierMod.DEPLOYED_MINE.get(),
                DeployedMineRenderer::new
        );
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(
                SmokeGrenadeProjectileModel.LAYER_LOCATION,
                SmokeGrenadeProjectileModel::createBodyLayer
        );
        event.registerLayerDefinition(
                IncendiaryGrenadeProjectileModel.LAYER_LOCATION,
                IncendiaryGrenadeProjectileModel::createBodyLayer
        );
        event.registerLayerDefinition(
                FlashbangProjectileModel.LAYER_LOCATION,
                FlashbangProjectileModel::createBodyLayer
        );
        event.registerLayerDefinition(
                FragGrenadeProjectileModel.LAYER_LOCATION,
                FragGrenadeProjectileModel::createBodyLayer
        );
        event.registerLayerDefinition(
                ImpactGrenadeProjectileModel.LAYER_LOCATION,
                ImpactGrenadeProjectileModel::createBodyLayer
        );
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(
                GrenadierMod.COLORED_SIGNAL_SMOKE.get(),
                ColoredSignalSmokeParticle.Provider::new
        );
        event.registerSpriteSet(
                GrenadierMod.GROUND_SMOKE_SHEET.get(),
                GroundSmokeSheetParticle.Provider::new
        );
        event.registerSpriteSet(
                GrenadierMod.INCENDIARY_FLAME.get(),
                IncendiaryFlameParticle.Provider::new
        );
        event.registerSpriteSet(
                GrenadierMod.THERMITE_SPARK.get(),
                ThermiteSparkParticle.Provider::new
        );
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(SmokeGrenadeColors::itemTint, GrenadierMod.SIGNAL_FLARE.get());
    }
}
