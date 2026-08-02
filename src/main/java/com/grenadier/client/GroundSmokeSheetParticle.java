package com.grenadier.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

public final class GroundSmokeSheetParticle extends TextureSheetParticle {
    private static final SingleQuadParticle.FacingCameraMode HORIZONTAL =
            (Quaternionf rotation, net.minecraft.client.Camera camera, float partialTick) ->
                    rotation.rotationX((float) (Math.PI * 0.5D));

    private final float baseAlpha;

    private GroundSmokeSheetParticle(
            ClientLevel level,
            double x,
            double y,
            double z,
            double radius,
            ColorParticleOption color
    ) {
        super(level, x, y, z);
        this.quadSize = Mth.clamp((float) Math.abs(radius), 0.75F, 32.0F);
        this.setColor(color.getRed(), color.getGreen(), color.getBlue());
        this.baseAlpha = Math.min(color.getAlpha(), 0.34F);
        this.alpha = 0.04F;
        this.lifetime = 30;
        this.roll = this.random.nextFloat() * (float) (Math.PI * 2.0D);
        this.oRoll = this.roll;
        this.hasPhysics = false;
    }

    @Override
    public SingleQuadParticle.FacingCameraMode getFacingCameraMode() {
        return HORIZONTAL;
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
        float fadeIn = Math.min(1.0F, this.age / 4.0F);
        float fadeOut = Math.min(1.0F, (this.lifetime - this.age) / 10.0F);
        this.alpha = this.baseAlpha * fadeIn * fadeOut;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static final class Provider implements ParticleProvider<ColorParticleOption> {
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
            GroundSmokeSheetParticle particle = new GroundSmokeSheetParticle(level, x, y, z, xSpeed, type);
            particle.pickSprite(this.sprites);
            return particle;
        }
    }
}
