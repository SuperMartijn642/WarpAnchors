package com.supermartijn642.warpanchors.mixin;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.warpanchors.content.WarpHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Inject(
        method = "collectVisibleEntities",
        at = @At("RETURN"),
        cancellable = true
    )
    private void collectVisibleEntities(Camera camera, Frustum frustum, List<Entity> output, CallbackInfoReturnable<Boolean> ci){
        if(WarpHandler.extractRenderState(camera))
            ci.setReturnValue(true);
    }

    @Inject(
        method = "renderBlockEntities",
        at = @At("HEAD")
    )
    private void renderBlockEntities(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, MultiBufferSource.BufferSource crumblingBufferSource, Camera camera, float partialTicks, Frustum frustum, CallbackInfoReturnable<Boolean> ci){
        WarpHandler.renderOutline(camera.getPosition());
    }

    @Inject(
        method = "lambda$addLateDebugPass$6(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;Lnet/minecraft/world/phys/Vec3;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/debug/DebugRenderer;renderAfterTranslucents(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource$BufferSource;DDD)V"
        )
    )
    private void renderAlwaysOnTop(GpuBufferSlice gpuBufferSlice, Vec3 cameraPos, CallbackInfo ci){
        WarpHandler.renderAlwaysOnTop(cameraPos);
    }
}
