package com.grenadier.mixin.iris;

import com.grenadier.client.IrisVeilBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Optional bridge point after Iris has completed its composite and final passes. */
@Pseudo
@Mixin(targets = "net.irisshaders.iris.pipeline.IrisRenderingPipeline", remap = false)
abstract class IrisRenderingPipelineMixin {
    @Inject(method = "finalizeLevelRendering()V", at = @At("RETURN"), require = 0)
    private void grenadier$renderVeilSmokeAfterIris(CallbackInfo callbackInfo) {
        IrisVeilBridge.afterIrisFinalPass(this);
    }
}
