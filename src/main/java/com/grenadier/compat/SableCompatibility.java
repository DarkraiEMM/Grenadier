package com.grenadier.compat;

import com.grenadier.GrenadierMod;
import net.minecraft.core.Position;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Optional Sable integration without making Sable a required dependency.
 */
public final class SableCompatibility {
    private static final String SABLE_MOD_ID = "sable";
    private static final Bridge BRIDGE = findBridge();
    private static final AtomicBoolean INVOCATION_WARNING_LOGGED = new AtomicBoolean();
    private static final AtomicBoolean PROJECTION_WARNING_LOGGED = new AtomicBoolean();

    private SableCompatibility() {
    }

    /**
     * Sable uses a tracking sub-level for entities supported by a moving structure.
     * Such entities do not necessarily expose vanilla ground/collision flags.
     */
    public static boolean isTrackingStructure(Entity entity) {
        if (BRIDGE == null) {
            return false;
        }
        try {
            return BRIDGE.getTrackingSubLevel().invoke(BRIDGE.instance(), entity) != null;
        } catch (ReflectiveOperationException | LinkageError exception) {
            if (INVOCATION_WARNING_LOGGED.compareAndSet(false, true)) {
                GrenadierMod.LOGGER.warn("Sable structure tracking query failed; smoke grenades will use vanilla collision flags",
                        exception);
            }
            return false;
        }
    }

    public static boolean isBridgeAvailable() {
        return BRIDGE != null;
    }

    /** Converts a Sable plot-space position returned by its ray caster to world space. */
    public static Vec3 projectToGlobal(Level level, Vec3 position) {
        if (BRIDGE == null) {
            return position;
        }
        try {
            Object projected = BRIDGE.projectOutOfSubLevel().invoke(BRIDGE.instance(), level, position);
            return projected instanceof Vec3 vec ? vec : position;
        } catch (ReflectiveOperationException | LinkageError exception) {
            if (PROJECTION_WARNING_LOGGED.compareAndSet(false, true)) {
                GrenadierMod.LOGGER.warn("Sable position projection failed; physical-structure effects may use plot coordinates",
                        exception);
            }
            return position;
        }
    }

    public static boolean wasProjected(Vec3 original, Vec3 projected) {
        return original.distanceToSqr(projected) > 1.0E-8D;
    }

    private static Bridge findBridge() {
        if (!ModList.get().isLoaded(SABLE_MOD_ID)) {
            return null;
        }
        try {
            Class<?> companionType = Class.forName("dev.ryanhcode.sable.companion.SableCompanion");
            Object instance = companionType.getField("INSTANCE").get(null);
            Method getTrackingSubLevel = companionType.getMethod("getTrackingSubLevel", Entity.class);
            Method projectOutOfSubLevel = companionType.getMethod(
                    "projectOutOfSubLevel", Level.class, Position.class);
            GrenadierMod.LOGGER.info("Sable structure tracking bridge active for smoke grenades");
            return new Bridge(instance, getTrackingSubLevel, projectOutOfSubLevel);
        } catch (ReflectiveOperationException | LinkageError exception) {
            GrenadierMod.LOGGER.warn("Sable is loaded but its structure tracking bridge is unavailable", exception);
            return null;
        }
    }

    private record Bridge(Object instance, Method getTrackingSubLevel, Method projectOutOfSubLevel) {
    }
}
