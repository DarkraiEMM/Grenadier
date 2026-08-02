package com.grenadier;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class GrenadierConfig {
    public static final ModConfigSpec.DoubleValue RADIUS;
    public static final ModConfigSpec.IntValue DURATION_SECONDS;
    public static final ModConfigSpec.IntValue DEPLOY_TICKS;
    public static final ModConfigSpec.IntValue CHECK_INTERVAL_TICKS;
    public static final ModConfigSpec.IntValue TARGET_LOSS_TICKS;
    public static final ModConfigSpec.DoubleValue CLOSE_DETECTION_RANGE;
    public static final ModConfigSpec.IntValue MAX_CLOUDS_PER_DIMENSION;
    public static final ModConfigSpec.BooleanValue AFFECT_PLAYERS;
    public static final ModConfigSpec.IntValue SIGNAL_DURATION_SECONDS;
    public static final ModConfigSpec.IntValue SIGNAL_COOLDOWN_SECONDS;
    public static final ModConfigSpec.DoubleValue INCENDIARY_RADIUS;
    public static final ModConfigSpec.IntValue INCENDIARY_DURATION_SECONDS;
    public static final ModConfigSpec.IntValue INCENDIARY_CHECK_INTERVAL_TICKS;
    public static final ModConfigSpec.DoubleValue INCENDIARY_DAMAGE_PER_PULSE;
    public static final ModConfigSpec.IntValue INCENDIARY_IGNITE_SECONDS;
    public static final ModConfigSpec.DoubleValue FLASHBANG_RADIUS;
    public static final ModConfigSpec.IntValue FLASHBANG_MAX_WHITE_TICKS;
    public static final ModConfigSpec.IntValue FLASHBANG_MAX_FADE_TICKS;
    public static final ModConfigSpec.IntValue FLASHBANG_MAX_MOB_STUN_TICKS;
    public static final ModConfigSpec.DoubleValue FRAG_GRENADE_RADIUS;
    public static final ModConfigSpec.IntValue FRAG_GRENADE_FUSE_TICKS;
    public static final ModConfigSpec.BooleanValue FRAG_GRENADE_DESTROY_BLOCKS;
    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("smoke");
        RADIUS = builder.defineInRange("radius", 9.0D, 1.0D, 32.0D);
        DURATION_SECONDS = builder.defineInRange("durationSeconds", 20, 1, 300);
        DEPLOY_TICKS = builder.defineInRange("deployTicks", 30, 1, 100);
        CHECK_INTERVAL_TICKS = builder.defineInRange("checkIntervalTicks", 5, 1, 40);
        TARGET_LOSS_TICKS = builder.defineInRange("targetLossTicks", 10, 1, 200);
        CLOSE_DETECTION_RANGE = builder.defineInRange("closeDetectionRange", 2.5D, 0.0D, 16.0D);
        MAX_CLOUDS_PER_DIMENSION = builder.defineInRange("maxCloudsPerDimension", 24, 1, 256);
        AFFECT_PLAYERS = builder.comment("Apply colored close-range fog while a player is inside smoke.")
                .define("affectPlayers", true);
        builder.pop();
        builder.push("signalBeacon");
        SIGNAL_DURATION_SECONDS = builder.defineInRange("durationSeconds", 60, 1, 3600);
        SIGNAL_COOLDOWN_SECONDS = builder.defineInRange("cooldownSeconds", 120, 0, 7200);
        builder.pop();
        builder.push("incendiary");
        INCENDIARY_RADIUS = builder.defineInRange("radius", 5.0D, 1.0D, 16.0D);
        INCENDIARY_DURATION_SECONDS = builder.defineInRange("durationSeconds", 9, 1, 60);
        INCENDIARY_CHECK_INTERVAL_TICKS = builder.defineInRange("checkIntervalTicks", 10, 1, 40);
        INCENDIARY_DAMAGE_PER_PULSE = builder.defineInRange("damagePerPulse", 1.5D, 0.0D, 20.0D);
        INCENDIARY_IGNITE_SECONDS = builder.defineInRange("igniteSeconds", 3, 0, 30);
        builder.pop();
        builder.push("flashbang");
        FLASHBANG_RADIUS = builder.defineInRange("radius", 32.0D, 2.0D, 48.0D);
        FLASHBANG_MAX_WHITE_TICKS = builder.defineInRange("maxWhiteTicks", 20, 0, 100);
        FLASHBANG_MAX_FADE_TICKS = builder.defineInRange("maxFadeTicks", 60, 1, 200);
        FLASHBANG_MAX_MOB_STUN_TICKS = builder.defineInRange("maxMobStunTicks", 40, 0, 200);
        builder.pop();
        builder.push("fragGrenade");
        FRAG_GRENADE_RADIUS = builder.defineInRange("radius", 4.0D, 1.0D, 16.0D);
        FRAG_GRENADE_FUSE_TICKS = builder.defineInRange("fuseTicks", 50, 1, 200);
        FRAG_GRENADE_DESTROY_BLOCKS = builder.comment("Allow fragmentation grenades to destroy terrain like TNT.")
                .define("destroyBlocks", false);
        builder.pop();
        SPEC = builder.build();
    }

    private GrenadierConfig() {
    }
}
