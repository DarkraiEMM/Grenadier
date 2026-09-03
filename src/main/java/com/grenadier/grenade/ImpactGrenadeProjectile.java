package com.grenadier.grenade;

import com.grenadier.GrenadierConfig;
import com.grenadier.GrenadierMod;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public final class ImpactGrenadeProjectile extends ThrowableItemProjectile {
    private boolean detonated;

    public ImpactGrenadeProjectile(EntityType<? extends ImpactGrenadeProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public ImpactGrenadeProjectile(Level level, LivingEntity owner) {
        super(GrenadierMod.IMPACT_GRENADE_PROJECTILE.get(), owner, level);
    }

    @Override
    protected Item getDefaultItem() {
        return GrenadierMod.IMPACT_GRENADE.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && this.tickCount > 200) {
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        Vec3 center = result.getLocation().add(Vec3.atLowerCornerOf(result.getDirection().getNormal()).scale(0.05D));
        this.detonate(center);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        this.detonate(result.getLocation());
    }

    private void detonate(Vec3 center) {
        if (this.detonated || !(this.level() instanceof ServerLevel level)) {
            return;
        }
        this.detonated = true;
        Level.ExplosionInteraction interaction = GrenadierConfig.IMPACT_GRENADE_DESTROY_BLOCKS.get()
                ? Level.ExplosionInteraction.TNT
                : Level.ExplosionInteraction.NONE;
        level.explode(this, center.x, center.y, center.z,
                GrenadierConfig.IMPACT_GRENADE_RADIUS.get().floatValue(), false, interaction);
        this.discard();
    }
}
