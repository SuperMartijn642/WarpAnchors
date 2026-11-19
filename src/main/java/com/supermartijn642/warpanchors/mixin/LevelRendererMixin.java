package com.supermartijn642.warpanchors.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.resource.GraphicsResourceAllocator;
import com.mojang.blaze3d.resource.ResourceHandle;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.warpanchors.content.WarpHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.state.LevelRenderState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;
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
    @Final
    @Shadow
    private RenderBuffers renderBuffers;

    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;extractVisibleBlockEntities(Lnet/minecraft/client/Camera;FLnet/minecraft/client/renderer/state/LevelRenderState;Lnet/minecraft/client/renderer/culling/Frustum;)V"
        )
    )
    private void extract(GraphicsResourceAllocator graphicsAllocator, DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, Matrix4f cameraRotationMatrix, Matrix4f projectionMatrix, Matrix4f otherProjectionMatrix, GpuBufferSlice gpuBufferSlice, Vector4f fogColor, boolean renderSky, CallbackInfo ci){
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

    @Inject(
        method = "lambda$addLateDebugPass$5(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lcom/mojang/blaze3d/resource/ResourceHandle;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/world/phys/Vec3;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/culling/Frustum;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDDZ)V",
            shift = At.Shift.BEFORE
        )
    )
    private void renderAlwaysOnTop(GpuBufferSlice gpuBufferSlice, ResourceHandle<?> resourceHandle, Frustum frustum, Vec3 cameraPos, CallbackInfo ci){
        if(!WarpHandler.submittedAlwaysOnTopNodes)
            return;
        RenderSystem.getDevice().createCommandEncoder().clearDepthTexture(ClientUtils.getMinecraft().getMainRenderTarget().getDepthTexture(), 1.0);
        WarpHandler.FEATURE_RENDER_DISPATCHER.renderAllFeatures();
        this.renderBuffers.bufferSource().endBatch();
    }
}
