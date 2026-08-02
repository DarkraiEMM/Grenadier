package com.grenadier.smoke;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public final class SmokeGrenadeColors {
    public static final int DEFAULT_RGB = 0x6F716F;
    private static final int NEUTRAL_SMOKE_RGB = 0x555755;

    private SmokeGrenadeColors() {
    }

    public static int itemTint(ItemStack stack, int tintIndex) {
        if (tintIndex != 1) {
            return 0xFFFFFFFF;
        }
        return displayArgb(stack);
    }

    public static int displayArgb(ItemStack stack) {
        return 0xFF000000 | (DyedItemColor.getOrDefault(stack, DEFAULT_RGB) & 0xFFFFFF);
    }

    public static int smokeArgb(ItemStack stack) {
        int dyed = DyedItemColor.getOrDefault(stack, DEFAULT_RGB) & 0xFFFFFF;
        int blended = blend(dyed, NEUTRAL_SMOKE_RGB, 0.78F);
        return 0xF8000000 | blended;
    }

    static int blend(int color, int neutral, float colorWeight) {
        float neutralWeight = 1.0F - colorWeight;
        int red = Math.round(((color >> 16) & 0xFF) * colorWeight + ((neutral >> 16) & 0xFF) * neutralWeight);
        int green = Math.round(((color >> 8) & 0xFF) * colorWeight + ((neutral >> 8) & 0xFF) * neutralWeight);
        int blue = Math.round((color & 0xFF) * colorWeight + (neutral & 0xFF) * neutralWeight);
        return (red << 16) | (green << 8) | blue;
    }
}
