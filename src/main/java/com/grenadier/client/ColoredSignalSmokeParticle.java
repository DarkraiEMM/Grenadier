package com.grenadier.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.CampfireSmokeParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.ColorParticleOption;

public class ColoredSignalSmokeParticle extends CampfireSmokeParticle {
    private final float rollSpeed;
    private final float baseAlpha;

    private ColoredSignalSmokeParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double xSpeed,
            double ySpeed,
            double zSpeed,
            ColorParticleOption color
    ) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed, true);
        this.setColor(color.getRed(), color.getGreen(), color.getBlue());
        boolean core = this.random.nextFloat() < 0.88F;
        this.baseAlpha = Math.min(color.getAlpha(), core ? 0.96F : 0.52F);
        // CampfireSmokeParticle removes itself at the start of its first tick when alpha is zero.
        // Start nearly transparent so the inherited lifecycle survives long enough for our fade-in.
        this.setAlpha(Math.max(0.04F, this.baseAlpha / 6.0F));
        this.xd = xSpeed * 0.32D + (this.random.nextDouble() - 0.5D) * 0.008D;
        this.yd = Math.abs(ySpeed) * 0.18D + 0.006D + this.random.nextDouble() * 0.009D;
        this.zd = zSpeed * 0.32D + (this.random.nextDouble() - 0.5D) * 0.008D;
        this.lifetime = this.random.nextInt(36) + (core ? 56 : 72);
        this.scale(core ? 0.70F + this.random.nextFloat() * 0.25F : 0.90F + this.random.nextFloat() * 0.28F);
        this.roll = this.random.nextFloat() * (float) (Math.PI * 2.0D);
        this.oRoll = this.roll;
        this.rollSpeed = (this.random.nextBoolean() ? 1.0F : -1.0F) * (0.0018F + this.random.nextFloat() * 0.0024F);
    }

    @Override
    public void tick() {
        super.tick();
        this.oRoll = this.roll;
        this.roll += this.rollSpeed;
        float fadeIn = Math.min(1.0F, this.age / 6.0F);
        float fadeOut = Math.min(1.0F, Math.max(0.0F, this.lifetime - this.age) / 18.0F);
        this.setAlpha(this.baseAlpha * fadeIn * fadeOut);
    }

    public static class Provider implements ParticleProvider<ColorParticleOption> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(
                ColorParticleOption type,
                ClientLevel level,
                double x,
                double y,
                double z,
                double xSpeed,
                double ySpeed,
                double zSpeed
        ) {
            ColoredSignalSmokeParticle particle = new ColoredSignalSmokeParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, type);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
