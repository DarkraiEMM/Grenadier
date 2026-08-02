package com.grenadier.client;

import com.grenadier.GrenadierMod;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Keeps the Veil volume pass after Iris' final world composite while reusing Iris' scene depth.
 * All Iris access is reflective so the mod remains loadable when Iris is absent.
 */
public final class IrisVeilBridge {
    private static final String IRIS_API = "net.irisshaders.iris.api.v0.IrisApi";
    private static Method irisGetInstance;
    private static Method irisShaderPackInUse;
    private static boolean irisApiResolved;
    private static boolean hookObserved;
    private static boolean warned;
    private static boolean activeLogged;
    private static Field renderTargetsField;
    private static Method getDepthTexture;
    private static int depthReadFramebuffer;

    private IrisVeilBridge() {
    }

    public static boolean shouldUseBridge() {
        return hookObserved && isShaderPackInUse();
    }

    public static void afterIrisFinalPass(Object pipeline) {
        hookObserved = true;
        if (!isShaderPackInUse() || !SmokeVolumeClientEvents.hasRenderableSmoke()) {
            return;
        }
        try {
            if (copyIrisDepthToMain(pipeline)) {
                SmokeVolumeClientEvents.renderAfterIrisFinalPass();
                if (!activeLogged) {
                    activeLogged = true;
                    GrenadierMod.LOGGER.info("Iris-Veil smoke bridge is active after the shader-pack final pass");
                }
            }
        } catch (ReflectiveOperationException | RuntimeException | LinkageError exception) {
            warnOnce("Unable to bridge Iris depth into the Veil smoke pass", exception);
        }
    }

    public static void release() {
        if (depthReadFramebuffer != 0 && RenderSystem.isOnRenderThread()) {
            GL30.glDeleteFramebuffers(depthReadFramebuffer);
            depthReadFramebuffer = 0;
        }
        activeLogged = false;
    }

    private static boolean isShaderPackInUse() {
        if (!irisApiResolved) {
            irisApiResolved = true;
            try {
                Class<?> apiClass = Class.forName(IRIS_API, false, IrisVeilBridge.class.getClassLoader());
                irisGetInstance = apiClass.getMethod("getInstance");
                irisShaderPackInUse = apiClass.getMethod("isShaderPackInUse");
            } catch (ReflectiveOperationException | LinkageError ignored) {
                return false;
            }
        }
        if (irisGetInstance == null || irisShaderPackInUse == null) {
            return false;
        }
        try {
            Object api = irisGetInstance.invoke(null);
            return Boolean.TRUE.equals(irisShaderPackInUse.invoke(api));
        } catch (ReflectiveOperationException | RuntimeException exception) {
            warnOnce("Unable to query the Iris shader-pack state", exception);
            return false;
        }
    }

    private static boolean copyIrisDepthToMain(Object pipeline) throws ReflectiveOperationException {
        RenderSystem.assertOnRenderThread();
        Object renderTargets = renderTargets(pipeline);
        int irisDepthTexture = ((Number) getDepthTexture.invoke(renderTargets)).intValue();
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget main = minecraft.getMainRenderTarget();
        if (irisDepthTexture <= 0 || main.getDepthTextureId() <= 0 || main.width <= 0 || main.height <= 0) {
            return false;
        }

        if (depthReadFramebuffer == 0) {
            depthReadFramebuffer = GL30.glGenFramebuffers();
        }
        int previousRead = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        int previousDraw = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        try {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, depthReadFramebuffer);
            GL30.glFramebufferTexture2D(
                    GL30.GL_READ_FRAMEBUFFER,
                    GL30.GL_DEPTH_ATTACHMENT,
                    GL11.GL_TEXTURE_2D,
                    irisDepthTexture,
                    0
            );
            if (GL30.glCheckFramebufferStatus(GL30.GL_READ_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
                warnOnce("Iris depth framebuffer is incomplete", null);
                return false;
            }
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, main.frameBufferId);
            GL30.glBlitFramebuffer(
                    0, 0, main.width, main.height,
                    0, 0, main.width, main.height,
                    GL11.GL_DEPTH_BUFFER_BIT,
                    GL11.GL_NEAREST
            );
            return true;
        } finally {
            GL30.glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, previousRead);
            GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, previousDraw);
        }
    }

    private static Object renderTargets(Object pipeline) throws ReflectiveOperationException {
        if (renderTargetsField == null || !renderTargetsField.getDeclaringClass().isInstance(pipeline)) {
            renderTargetsField = pipeline.getClass().getDeclaredField("renderTargets");
            renderTargetsField.setAccessible(true);
        }
        Object renderTargets = renderTargetsField.get(pipeline);
        if (getDepthTexture == null || !getDepthTexture.getDeclaringClass().isInstance(renderTargets)) {
            getDepthTexture = renderTargets.getClass().getMethod("getDepthTexture");
        }
        return renderTargets;
    }

    private static void warnOnce(String message, Throwable throwable) {
        if (warned) {
            return;
        }
        warned = true;
        if (throwable == null) {
            GrenadierMod.LOGGER.warn(message);
        } else {
            GrenadierMod.LOGGER.warn(message, throwable);
        }
    }
}
