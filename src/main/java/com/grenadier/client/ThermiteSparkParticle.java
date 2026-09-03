package com.grenadier.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.SimpleParticleType;
import org.joml.Vector3f;

public final class ThermiteSparkParticle extends TextureSheetParticle {
    private static final DustColorTransitionOptions TRAIL = new DustColorTransitionOptions(
            new Vector3f(1.0F, 0.96F, 0.68F), new Vector3f(1.0F, 0.24F, 0.01F), 0.72F);
    private final SpriteSet sprites;
    private final float baseSize;

    private ThermiteSparkParticle(ClientLevel level, double x, double y, double z,
                                  double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, 0.0D, 0.0D, 0.0D);
        this.sprites = sprites;
        this.xd = xSpeed;
        this.yd = ySpeed;
        this.zd = zSpeed;
        this.baseSize = 0.13F + random.nextFloat() * 0.09F;
        this.quadSize = baseSize;
        this.lifetime = 18 + random.nextInt(10);
        this.gravity = 0.48F;
        this.friction = 0.95F;
        this.hasPhysics = true;
        this.rCol = 1.0F;
        this.gCol = 0.82F + random.nextFloat() * 0.18F;
        this.bCol = 0.34F + random.nextFloat() * 0.36F;
        this.alpha = 1.0F;
        setSpriteFromAge(sprites);
    }

    @Override
    public void tick() {
        super.tick();
        if (!removed) {
            setSpriteFromAge(sprites);
            if (age % 2 == 0) {
                level.addParticle(TRAIL, x - xd * 0.45D, y - yd * 0.45D, z - zd * 0.45D,
                        0.0D, 0.0D, 0.0D);
            }
            float remaining = 1.0F - age / (float) lifetime;
            quadSize = baseSize * (0.55F + remaining * 0.65F);
            alpha = Math.min(1.0F, remaining * 1.7F);
        }
    }

    @Override
    public int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                                       double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            return new ThermiteSparkParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites);
        }
    }
}
