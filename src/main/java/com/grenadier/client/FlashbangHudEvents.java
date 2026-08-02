package com.grenadier.client;

import com.grenadier.network.FlashbangClientState;
import com.grenadier.GrenadierMod;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = GrenadierMod.MODID, value = Dist.CLIENT)
public final class FlashbangHudEvents {
    private FlashbangHudEvents() {
    }

    @SubscribeEvent
    public static void renderFlash(RenderGuiEvent.Post event) {
        float strength = FlashbangClientState.strength();
        if (strength <= 0.002F) {
            return;
        }
        int alpha = Mth.clamp(Math.round(strength * 255.0F), 0, 255);
        GuiGraphics graphics = event.getGuiGraphics();
        graphics.fill(0, 0, graphics.guiWidth(), graphics.guiHeight(), alpha << 24 | 0xFFFFFF);
    }
}
