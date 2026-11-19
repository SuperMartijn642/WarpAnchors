package com.supermartijn642.warpanchors.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.warpanchors.content.WarpHandler;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.PostChain;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

    @Final
    @Shadow
    private Minecraft minecraft;
    @Shadow
    @Nullable
    private PostChain entityEffect;
    @Unique
    private boolean processedEntityEffect;

    @Shadow
    protected boolean shouldShowEntityOutlines(){
        throw new AssertionError();
    }

    @Inject(
        method = "renderLevel",
        at = @At("HEAD")
    )
    private void extract(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f cameraRotationMatrix, Matrix4f projectionMatrix, CallbackInfo ci){
        WarpHandler.extractRenderState(camera);
    }

    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/OutlineBufferSource;endOutlineBatch()V"
        )
    )
    private void renderOutline(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f cameraRotationMatrix, Matrix4f projectionMatrix, CallbackInfo ci){
        WarpHandler.renderOutline(camera.getPosition());
    }

    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/PostChain;process(F)V",
            ordinal = 0
        )
    )
    private void processEntityEffect(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f cameraRotationMatrix, Matrix4f projectionMatrix, CallbackInfo ci){
        this.processedEntityEffect = true;
    }

    @Inject(
        method = "renderLevel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;renderDebug(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/client/Camera;)V"
        )
    )
    private void renderAlwaysOnTop(DeltaTracker deltaTracker, boolean renderBlockOutline, Camera camera, GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f cameraRotationMatrix, Matrix4f projectionMatrix, CallbackInfo ci){
        // Process the outline when there are no glowing entities
        if(!this.processedEntityEffect && WarpHandler.hasTargetedAnchor() && this.shouldShowEntityOutlines()){
            this.entityEffect.process(deltaTracker.getGameTimeDeltaTicks());
            this.minecraft.getMainRenderTarget().bindWrite(false);
        }
        this.processedEntityEffect = false;
        WarpHandler.renderAlwaysOnTop(camera.getPosition());
    }
}
