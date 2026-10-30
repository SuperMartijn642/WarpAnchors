package com.supermartijn642.warpanchors.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.warpanchors.content.WarpHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.joml.Matrix4fc;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Final
    @Shadow
    private LevelRenderState levelRenderState;

    @Inject(
        method = "extractLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;extractVisibleBlockEntities(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/state/level/LevelRenderState;Lnet/minecraft/client/renderer/culling/Frustum;)V"
        )
    )
    private void extract(DeltaTracker deltaTracker, Camera camera, float deltaPartialTick, CallbackInfo ci){
        if(WarpHandler.extractRenderState(camera))
            this.levelRenderState.haveGlowingEntities = true;
    }

    @Inject(
        method = "submitBlockEntities",
        at = @At("HEAD")
    )
    private void submitFeatures(PoseStack poseStack, LevelRenderState levelRenderState, SubmitNodeStorage submitNodeStorage, CallbackInfo ci){
        WarpHandler.submitRenderState(levelRenderState.cameraRenderState);
    }

    @ModifyExpressionValue(
        method = "lambda$addLateDebugPass$0(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/resource/ResourceHandle;Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/gizmos/DrawableGizmoPrimitives;isEmpty()Z"
        )
    )
    private boolean bypassAlwaysOnTopEmptyCheck(boolean original){
        return original && !WarpHandler.submittedAlwaysOnTopNodes;
    }

    @Inject(
        method = "lambda$addLateDebugPass$0(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/resource/ResourceHandle;Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/gizmos/DrawableGizmoPrimitives;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/renderer/state/level/CameraRenderState;Lorg/joml/Matrix4fc;)V",
            shift = At.Shift.BEFORE
        )
    )
    private void renderAlwaysOnTop(GpuBufferSlice gpuBufferSlice, ResourceHandle<?> resourceHandle, CameraRenderState cameraRenderState, Matrix4fc matrix4fc, CallbackInfo ci){
        WarpHandler.FEATURE_RENDER_DISPATCHER.renderAllFeatures();
    }
}
