package com.grenadier;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class GrenadierConfig {
    public static final ModConfigSpec.DoubleValue RADIUS;
    public static final ModConfigSpec.DoubleValue SMOKE_DENSITY;
    public static final ModConfigSpec.IntValue SMOKE_FUSE_TICKS;
    public static final ModConfigSpec.DoubleValue SMOKE_RESTITUTION;
    public static final ModConfigSpec.DoubleValue SMOKE_TANGENTIAL_DAMPING;
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
    public static final ModConfigSpec.IntValue INCENDIARY_FUSE_TICKS;
    public static final ModConfigSpec.DoubleValue INCENDIARY_RESTITUTION;
    public static final ModConfigSpec.DoubleValue INCENDIARY_TANGENTIAL_DAMPING;
    public static final ModConfigSpec.IntValue INCENDIARY_DURATION_SECONDS;
    public static final ModConfigSpec.IntValue INCENDIARY_CHECK_INTERVAL_TICKS;
    public static final ModConfigSpec.DoubleValue INCENDIARY_DAMAGE_PER_PULSE;
    public static final ModConfigSpec.IntValue INCENDIARY_IGNITE_SECONDS;
    public static final ModConfigSpec.DoubleValue FLASHBANG_RADIUS;
    public static final ModConfigSpec.IntValue FLASHBANG_FUSE_TICKS;
    public static final ModConfigSpec.DoubleValue FLASHBANG_RESTITUTION;
    public static final ModConfigSpec.DoubleValue FLASHBANG_TANGENTIAL_DAMPING;
    public static final ModConfigSpec.DoubleValue FLASHBANG_EFFECT_STRENGTH;
    public static final ModConfigSpec.IntValue FLASHBANG_MAX_WHITE_TICKS;
    public static final ModConfigSpec.IntValue FLASHBANG_MAX_FADE_TICKS;
    public static final ModConfigSpec.IntValue FLASHBANG_MAX_MOB_STUN_TICKS;
    public static final ModConfigSpec.DoubleValue FRAG_GRENADE_RADIUS;
    public static final ModConfigSpec.IntValue FRAG_GRENADE_FUSE_TICKS;
    public static final ModConfigSpec.DoubleValue FRAG_GRENADE_RESTITUTION;
    public static final ModConfigSpec.DoubleValue FRAG_GRENADE_TANGENTIAL_DAMPING;
    public static final ModConfigSpec.BooleanValue FRAG_GRENADE_DESTROY_BLOCKS;
    public static final ModConfigSpec.DoubleValue IMPACT_GRENADE_RADIUS;
    public static final ModConfigSpec.BooleanValue IMPACT_GRENADE_DESTROY_BLOCKS;
    public static final ModConfigSpec.BooleanValue MINES_DAMAGE_OWNER;
    public static final ModConfigSpec.IntValue ANTI_PERSONNEL_MINE_ARM_TICKS;
    public static final ModConfigSpec.DoubleValue ANTI_PERSONNEL_MINE_TRIGGER_RADIUS;
    public static final ModConfigSpec.DoubleValue ANTI_PERSONNEL_MINE_RADIUS;
    public static final ModConfigSpec.DoubleValue ANTI_PERSONNEL_MINE_HEAVY_BONUS_DAMAGE;
    public static final ModConfigSpec.BooleanValue ANTI_PERSONNEL_MINE_DESTROY_BLOCKS;
    public static final ModConfigSpec.IntValue DIRECTIONAL_MINE_ARM_TICKS;
    public static final ModConfigSpec.DoubleValue DIRECTIONAL_MINE_RANGE;
    public static final ModConfigSpec.DoubleValue DIRECTIONAL_MINE_WIDTH;
    public static final ModConfigSpec.DoubleValue DIRECTIONAL_MINE_HALF_ANGLE;
    public static final ModConfigSpec.DoubleValue DIRECTIONAL_MINE_DAMAGE;
    public static final ModConfigSpec.DoubleValue DIRECTIONAL_MINE_HEAVY_BONUS_DAMAGE;
    public static final ModConfigSpec.DoubleValue DIRECTIONAL_MINE_BACKBLAST_RADIUS;
    public static final ModConfigSpec.BooleanValue DIRECTIONAL_MINE_DESTROY_BLOCKS;
    public static final ModConfigSpec.DoubleValue THERMITE_MINE_THROW_SPEED;
    public static final ModConfigSpec.IntValue THERMITE_MINE_ARM_TICKS;
    public static final ModConfigSpec.DoubleValue THERMITE_MINE_TRIGGER_RADIUS;
    public static final ModConfigSpec.DoubleValue THERMITE_MINE_EXPLOSION_RADIUS;
    public static final ModConfigSpec.DoubleValue THERMITE_MINE_BURST_DAMAGE;
    public static final ModConfigSpec.DoubleValue THERMITE_MINE_CORE_HALF_WIDTH;
    public static final ModConfigSpec.DoubleValue THERMITE_MINE_SPLASH_DAMAGE;
    public static final ModConfigSpec.DoubleValue THERMITE_MINE_SPLASH_HEAVY_BONUS_DAMAGE;
    public static final ModConfigSpec.IntValue THERMITE_MINE_SPLASH_IGNITE_SECONDS;
    public static final ModConfigSpec.DoubleValue THERMITE_MINE_ARMOR_PIERCE_RATIO;
    public static final ModConfigSpec.DoubleValue THERMITE_MINE_BURST_HEAVY_BONUS_DAMAGE;
    public static final ModConfigSpec.DoubleValue THERMITE_MINE_FLAME_RADIUS;
    public static final ModConfigSpec.IntValue THERMITE_MINE_BURST_TICKS;
    public static final ModConfigSpec.IntValue THERMITE_MINE_RESIDUAL_TICKS;
    public static final ModConfigSpec.IntValue THERMITE_MINE_DAMAGE_INTERVAL;
    public static final ModConfigSpec.DoubleValue THERMITE_MINE_FIRE_DAMAGE;
    public static final ModConfigSpec.DoubleValue THERMITE_MINE_HEAVY_BONUS_DAMAGE;
    public static final ModConfigSpec.IntValue THERMITE_MINE_IGNITE_SECONDS;
    public static final ModConfigSpec.BooleanValue THERMITE_MINE_DESTROY_BLOCKS;
    public static final ModConfigSpec SPEC;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("smoke");
        RADIUS = builder.comment("烟雾作用半径，单位：方块。")
                .defineInRange("radius", 9.0D, 1.0D, 32.0D);
        SMOKE_DENSITY = builder.comment("体积烟视觉浓度。1.0 接近完全浓厚；只改变视觉，不改变 AI 遮挡范围。")
                .defineInRange("density", 0.78D, 0.05D, 2.0D);
        SMOKE_FUSE_TICKS = builder.comment("烟雾弹引信时长，20 tick = 1 秒。")
                .defineInRange("fuseTicks", 32, 1, 200);
        SMOKE_RESTITUTION = builder.comment("烟雾弹法向弹性系数：0 不反弹，1 完全弹性反弹。")
                .defineInRange("restitution", 0.56D, 0.0D, 1.0D);
        SMOKE_TANGENTIAL_DAMPING = builder.comment("烟雾弹沿碰撞表面的速度保留比例。")
                .defineInRange("tangentialDamping", 0.80D, 0.0D, 1.0D);
        DURATION_SECONDS = builder.comment("烟雾持续时间，单位：秒。")
                .defineInRange("durationSeconds", 20, 1, 300);
        DEPLOY_TICKS = builder.comment("烟云扩张至完整尺寸所需 tick。")
                .defineInRange("deployTicks", 30, 1, 100);
        CHECK_INTERVAL_TICKS = builder.comment("烟雾 AI 遮挡检查间隔；数值越小响应越快、开销越高。")
                .defineInRange("checkIntervalTicks", 5, 1, 40);
        TARGET_LOSS_TICKS = builder.comment("目标持续被烟雾遮挡多少 tick 后生物才会丢失目标。")
                .defineInRange("targetLossTicks", 10, 1, 200);
        CLOSE_DETECTION_RANGE = builder.comment("生物可无视烟雾识别目标的近距离范围，单位：方块。")
                .defineInRange("closeDetectionRange", 2.5D, 0.0D, 16.0D);
        MAX_CLOUDS_PER_DIMENSION = builder.comment("每个维度最多保留的活动烟云数量。")
                .defineInRange("maxCloudsPerDimension", 24, 1, 256);
        AFFECT_PLAYERS = builder.comment("玩家处于烟雾内时是否应用近距离灰视。")
                .define("affectPlayers", true);
        builder.pop();
        builder.push("signalBeacon");
        SIGNAL_DURATION_SECONDS = builder.comment("信号机激活持续时间，单位：秒。")
                .defineInRange("durationSeconds", 60, 1, 3600);
        SIGNAL_COOLDOWN_SECONDS = builder.comment("信号机冷却时间，单位：秒。")
                .defineInRange("cooldownSeconds", 120, 0, 7200);
        builder.pop();
        builder.push("incendiary");
        INCENDIARY_RADIUS = builder.comment("燃烧区域半径，单位：方块。")
                .defineInRange("radius", 5.0D, 1.0D, 16.0D);
        INCENDIARY_FUSE_TICKS = builder.comment("燃烧弹引信时长，20 tick = 1 秒。")
                .defineInRange("fuseTicks", 36, 6, 200);
        INCENDIARY_RESTITUTION = builder.comment("燃烧弹法向弹性系数。")
                .defineInRange("restitution", 0.12D, 0.0D, 1.0D);
        INCENDIARY_TANGENTIAL_DAMPING = builder.comment("燃烧弹沿碰撞表面的速度保留比例。")
                .defineInRange("tangentialDamping", 0.58D, 0.0D, 1.0D);
        INCENDIARY_DURATION_SECONDS = builder.comment("燃烧区域持续时间，单位：秒。")
                .defineInRange("durationSeconds", 9, 1, 60);
        INCENDIARY_CHECK_INTERVAL_TICKS = builder.comment("燃烧伤害结算间隔，单位：tick。")
                .defineInRange("checkIntervalTicks", 10, 1, 40);
        INCENDIARY_DAMAGE_PER_PULSE = builder.comment("每次结算造成的伤害值；2 点伤害 = 1 颗心。")
                .defineInRange("damagePerPulse", 1.5D, 0.0D, 20.0D);
        INCENDIARY_IGNITE_SECONDS = builder.comment("每次命中附加的燃烧时间，单位：秒。")
                .defineInRange("igniteSeconds", 3, 0, 30);
        builder.pop();
        builder.push("flashbang");
        FLASHBANG_RADIUS = builder.comment("闪光弹最大效果半径，单位：方块。")
                .defineInRange("radius", 32.0D, 2.0D, 48.0D);
        FLASHBANG_FUSE_TICKS = builder.comment("闪光弹引信时长，20 tick = 1 秒。")
                .defineInRange("fuseTicks", 30, 1, 200);
        FLASHBANG_RESTITUTION = builder.comment("闪光弹法向弹性系数。")
                .defineInRange("restitution", 0.22D, 0.0D, 1.0D);
        FLASHBANG_TANGENTIAL_DAMPING = builder.comment("闪光弹沿碰撞表面的速度保留比例。")
                .defineInRange("tangentialDamping", 0.68D, 0.0D, 1.0D);
        FLASHBANG_EFFECT_STRENGTH = builder.comment("闪白和眩晕强度倍率；1.0 为默认，最终效果仍受距离、朝向和遮挡影响。")
                .defineInRange("effectStrength", 1.0D, 0.0D, 3.0D);
        FLASHBANG_MAX_WHITE_TICKS = builder.comment("满强度时纯白屏的最长时间，单位：tick。")
                .defineInRange("maxWhiteTicks", 20, 0, 100);
        FLASHBANG_MAX_FADE_TICKS = builder.comment("满强度时闪白淡出的最长时间，单位：tick。")
                .defineInRange("maxFadeTicks", 60, 1, 200);
        FLASHBANG_MAX_MOB_STUN_TICKS = builder.comment("满强度时生物眩晕的最长时间，单位：tick。")
                .defineInRange("maxMobStunTicks", 40, 0, 200);
        builder.pop();
        builder.push("fragGrenade");
        FRAG_GRENADE_RADIUS = builder.comment("破片手雷爆炸半径，单位：方块。")
                .defineInRange("radius", 4.0D, 1.0D, 16.0D);
        FRAG_GRENADE_FUSE_TICKS = builder.comment("破片手雷引信时长，20 tick = 1 秒。")
                .defineInRange("fuseTicks", 50, 1, 200);
        FRAG_GRENADE_RESTITUTION = builder.comment("破片手雷法向弹性系数。")
                .defineInRange("restitution", 0.28D, 0.0D, 1.0D);
        FRAG_GRENADE_TANGENTIAL_DAMPING = builder.comment("破片手雷沿碰撞表面的速度保留比例。")
                .defineInRange("tangentialDamping", 0.70D, 0.0D, 1.0D);
        FRAG_GRENADE_DESTROY_BLOCKS = builder.comment("破片手雷是否像 TNT 一样破坏地形。")
                .define("destroyBlocks", false);
        builder.pop();
        builder.push("impactGrenade");
        IMPACT_GRENADE_RADIUS = builder.comment("冲击手榴弹的爆炸半径，单位：方块。")
                .defineInRange("radius", 2.5D, 1.0D, 8.0D);
        IMPACT_GRENADE_DESTROY_BLOCKS = builder.comment("冲击手榴弹是否破坏地形。")
                .define("destroyBlocks", false);
        builder.pop();
        builder.push("mines");
        MINES_DAMAGE_OWNER = builder.comment("Whether deployed mines can damage their owner after arming.").define("damageOwner", true);
        builder.pop();
        builder.push("antiPersonnelMine");
        ANTI_PERSONNEL_MINE_ARM_TICKS = builder.comment("Arming delay in ticks after placement and owner clearance.").defineInRange("armTicks", 30, 1, 200);
        ANTI_PERSONNEL_MINE_TRIGGER_RADIUS = builder.comment("Pressure trigger radius around the mine center.").defineInRange("triggerRadius", 0.72D, 0.25D, 2.0D);
        ANTI_PERSONNEL_MINE_RADIUS = builder.comment("Explosion radius in blocks.").defineInRange("radius", 3.25D, 0.5D, 12.0D);
        ANTI_PERSONNEL_MINE_HEAVY_BONUS_DAMAGE = builder.comment("对重型目标追加的爆炸伤害值；2 点伤害 = 1 颗心。").defineInRange("heavyBonusDamage", 20.0D, 0.0D, 100.0D);
        ANTI_PERSONNEL_MINE_DESTROY_BLOCKS = builder.comment("Whether the explosion damages blocks.").define("destroyBlocks", false);
        builder.pop();
        builder.push("directionalMine");
        DIRECTIONAL_MINE_ARM_TICKS = builder.comment("Arming delay in ticks after placement and owner clearance.").defineInRange("armTicks", 30, 1, 200);
        DIRECTIONAL_MINE_RANGE = builder.comment("Forward detection and fragmentation range.").defineInRange("range", 5.0D, 1.0D, 16.0D);
        DIRECTIONAL_MINE_WIDTH = builder.comment("Maximum detection width at the far end.").defineInRange("width", 4.0D, 0.5D, 16.0D);
        DIRECTIONAL_MINE_HALF_ANGLE = builder.comment("Half angle of the forward fragmentation cone in degrees.").defineInRange("halfAngle", 42.0D, 5.0D, 85.0D);
        DIRECTIONAL_MINE_DAMAGE = builder.comment("Maximum forward fragmentation damage in half-hearts.").defineInRange("damage", 18.0D, 0.0D, 80.0D);
        DIRECTIONAL_MINE_HEAVY_BONUS_DAMAGE = builder.comment("对重型目标追加的正面破片伤害值；随距离一同衰减。").defineInRange("heavyBonusDamage", 20.0D, 0.0D, 100.0D);
        DIRECTIONAL_MINE_BACKBLAST_RADIUS = builder.comment("Small omnidirectional backblast radius.").defineInRange("backblastRadius", 1.1D, 0.0D, 4.0D);
        DIRECTIONAL_MINE_DESTROY_BLOCKS = builder.comment("Whether the backblast damages blocks.").define("destroyBlocks", false);
        builder.pop();
        builder.push("thermiteMine");
        THERMITE_MINE_THROW_SPEED = builder.comment("Projectile throw speed.").defineInRange("throwSpeed", 0.82D, 0.1D, 2.0D);
        THERMITE_MINE_ARM_TICKS = builder.comment("Arming delay after landing.").defineInRange("armTicks", 20, 1, 200);
        THERMITE_MINE_TRIGGER_RADIUS = builder.comment("Living target detection radius.").defineInRange("triggerRadius", 1.4D, 0.25D, 8.0D);
        THERMITE_MINE_EXPLOSION_RADIUS = builder.comment("瞬时爆发伤害半径，单位：方块；该伤害不会产生击退。").defineInRange("explosionRadius", 4.5D, 0.5D, 12.0D);
        THERMITE_MINE_BURST_DAMAGE = builder.comment("爆发中心的瞬时伤害值；2 点伤害 = 1 颗心。").defineInRange("burstDamage", 40.0D, 0.0D, 120.0D);
        THERMITE_MINE_CORE_HALF_WIDTH = builder.comment("满伤核心区的半边长；1.5 表示以地雷为中心的 3×3 水平区域。").defineInRange("coreHalfWidth", 1.5D, 0.25D, 6.0D);
        THERMITE_MINE_SPLASH_DAMAGE = builder.comment("3×3 核心区外、爆发半径内固定造成的溅射伤害值。").defineInRange("splashDamage", 16.0D, 0.0D, 80.0D);
        THERMITE_MINE_SPLASH_HEAVY_BONUS_DAMAGE = builder.comment("外圈溅射对重型目标追加的固定伤害值。").defineInRange("splashHeavyBonusDamage", 12.0D, 0.0D, 80.0D);
        THERMITE_MINE_SPLASH_IGNITE_SECONDS = builder.comment("外圈溅射命中后立即点燃目标的秒数。").defineInRange("splashIgniteSeconds", 5, 0, 30);
        THERMITE_MINE_ARMOR_PIERCE_RATIO = builder.comment("瞬时爆发伤害中无视护甲减伤的比例；0.5 表示 50% 穿甲，但仍受抗性和爆炸保护影响。").defineInRange("armorPierceRatio", 0.50D, 0.0D, 1.0D);
        THERMITE_MINE_BURST_HEAVY_BONUS_DAMAGE = builder.comment("3×3 满伤核心区对重型目标追加的伤害值。").defineInRange("burstHeavyBonusDamage", 24.0D, 0.0D, 120.0D);
        THERMITE_MINE_FLAME_RADIUS = builder.comment("短时火焰与熔滴喷流的伤害半径，单位：方块。").defineInRange("flameRadius", 4.5D, 0.5D, 12.0D);
        THERMITE_MINE_BURST_TICKS = builder.comment("High intensity spray duration in ticks.").defineInRange("burstTicks", 25, 1, 100);
        THERMITE_MINE_RESIDUAL_TICKS = builder.comment("Low intensity residual burn duration after the burst.").defineInRange("residualTicks", 45, 0, 200);
        THERMITE_MINE_DAMAGE_INTERVAL = builder.comment("Damage pulse interval in ticks.").defineInRange("damageInterval", 10, 1, 40);
        THERMITE_MINE_FIRE_DAMAGE = builder.comment("Fire damage per pulse in half-hearts.").defineInRange("fireDamage", 3.0D, 0.0D, 40.0D);
        THERMITE_MINE_HEAVY_BONUS_DAMAGE = builder.comment("Extra damage per pulse for tagged heavy targets.").defineInRange("heavyBonusDamage", 4.0D, 0.0D, 80.0D);
        THERMITE_MINE_IGNITE_SECONDS = builder.comment("Ignition duration applied by each pulse.").defineInRange("igniteSeconds", 3, 0, 30);
        THERMITE_MINE_DESTROY_BLOCKS = builder.comment("兼容旧配置保留；热熔爆发现在不会破坏方块。").define("destroyBlocks", false);
        builder.pop();
        SPEC = builder.build();
    }

    private GrenadierConfig() {
    }
}
