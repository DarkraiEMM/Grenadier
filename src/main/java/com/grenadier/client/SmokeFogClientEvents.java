package com.grenadier.client;

import com.grenadier.network.SmokeFogClientState;
import com.grenadier.GrenadierMod;
import com.mojang.blaze3d.shaders.FogShape;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = GrenadierMod.MODID, value = Dist.CLIENT)
public final class SmokeFogClientEvents {
    private static final float SMOKE_FOG_START = 0.25F;
    private static final float SMOKE_FOG_END = 3.5F;
    private static final int SMOKE_FOG_COLOR = 0x4C504C;

    private SmokeFogClientEvents() {
    }

    @SubscribeEvent
    public static void renderFog(ViewportEvent.RenderFog event) {
        float strength = SmokeFogClientState.strength();
        if (strength <= 0.0F || event.getType() != FogType.NONE) {
            return;
        }
        event.setNearPlaneDistance(lerp(event.getNearPlaneDistance(), SMOKE_FOG_START, strength));
        event.setFarPlaneDistance(lerp(event.getFarPlaneDistance(), SMOKE_FOG_END, strength));
        event.setFogShape(FogShape.SPHERE);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void colorFog(ViewportEvent.ComputeFogColor event) {
        float strength = SmokeFogClientState.strength();
        if (strength <= 0.0F) {
            return;
        }
        float colorStrength = strength * 0.82F;
        event.setRed(lerp(event.getRed(), ((SMOKE_FOG_COLOR >> 16) & 0xFF) / 255.0F, colorStrength));
        event.setGreen(lerp(event.getGreen(), ((SMOKE_FOG_COLOR >> 8) & 0xFF) / 255.0F, colorStrength));
        event.setBlue(lerp(event.getBlue(), (SMOKE_FOG_COLOR & 0xFF) / 255.0F, colorStrength));
    }

    private static float lerp(float from, float to, float strength) {
        return from + (to - from) * strength;
    }
}
