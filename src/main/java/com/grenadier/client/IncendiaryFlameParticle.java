package com.grenadier.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;

public final class IncendiaryFlameParticle extends TextureSheetParticle {
    private final float baseSize;

    private IncendiaryFlameParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed
    ) {
        super(level, x, y, z, xSpeed * 0.18D, Math.max(0.012D, ySpeed), zSpeed * 0.18D);
        this.baseSize = 0.38F + this.random.nextFloat() * 0.20F;
        this.quadSize = this.baseSize;
        this.lifetime = 12 + this.random.nextInt(8);
        this.friction = 0.91F;
        this.gravity = -0.012F;
        this.hasPhysics = false;
        this.alpha = 0.96F;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }
        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd = this.yd * 0.94D + 0.003D;
        this.zd *= this.friction;
        float remaining = 1.0F - this.age / (float) this.lifetime;
        this.alpha = Mth.clamp(remaining * 1.35F, 0.0F, 0.96F);
    }

    @Override
    public float getQuadSize(float partialTick) {
        float progress = (this.age + partialTick) / this.lifetime;
        float flicker = 0.92F + 0.08F * Mth.sin((this.age + partialTick) * 1.7F);
        float endShrink = 1.0F - 0.35F * progress * progress;
        return this.baseSize * flicker * endShrink;
    }

    @Override
    public int getLightColor(float partialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                SimpleParticleType type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            IncendiaryFlameParticle particle = new IncendiaryFlameParticle(
                    level, x, y, z, xSpeed, ySpeed, zSpeed);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
