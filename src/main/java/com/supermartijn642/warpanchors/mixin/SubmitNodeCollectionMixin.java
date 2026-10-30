package com.supermartijn642.warpanchors.mixin;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.supermartijn642.warpanchors.extension.SubmitNodeCollectorExtension;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.feature.phase.SimpleFeatureRenderPhase;
import net.minecraft.client.renderer.feature.phase.TranslucentFeatureRenderPhase;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.client.renderer.feature.submit.TranslucentSubmit;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = SubmitNodeCollection.class, priority = 1100)
public class SubmitNodeCollectionMixin implements SubmitNodeCollectorExtension {

    @Final
    @Shadow
    private SimpleFeatureRenderPhase alwaysOnTop;

    @Unique
    private boolean forceAlwaysOnTop;

    @Override
    public void warpanchors_setAlwaysOnTop(boolean alwaysOnTop){
        this.forceAlwaysOnTop = alwaysOnTop;
    }

    @WrapWithCondition(
        method = {
            "submitBlockModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;Ljava/util/List;[IIII)V",
            "submitBlockModel(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/function/Function;ZLjava/util/List;Lnet/fabricmc/fabric/api/client/renderer/v1/mesh/Mesh;[IIII)V"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/feature/phase/TranslucentFeatureRenderPhase;submit(Lnet/minecraft/client/renderer/feature/submit/TranslucentSubmit;)V"
        )
    )
    private boolean interceptTranslucentBlocks(TranslucentFeatureRenderPhase phase, TranslucentSubmit node){
        if(!this.forceAlwaysOnTop)
            return true;
        this.alwaysOnTop.submit(node);
        return false;
    }

    @ModifyReceiver(
        method = {
            "submitBlockModel(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;Ljava/util/List;[IIII)V",
            "submitBlockModel(Lcom/mojang/blaze3d/vertex/PoseStack;Ljava/util/function/Function;ZLjava/util/List;Lnet/fabricmc/fabric/api/client/renderer/v1/mesh/Mesh;[IIII)V"
        },
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/feature/phase/SimpleFeatureRenderPhase;submit(Lnet/minecraft/client/renderer/feature/submit/SubmitNode;)V",
            ordinal = 0
        )
    )
    private SimpleFeatureRenderPhase interceptSolidBlocks(SimpleFeatureRenderPhase phase, SubmitNode node){
        return this.forceAlwaysOnTop ? this.alwaysOnTop : phase;
    }

    @ModifyReceiver(
        method = "submitNameTag",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/feature/phase/SimpleFeatureRenderPhase;submit(Lnet/minecraft/client/renderer/feature/submit/SubmitNode;)V"
        ),
        expect = 2
    )
    private SimpleFeatureRenderPhase interceptNameTags(SimpleFeatureRenderPhase phase, SubmitNode node){
        return this.forceAlwaysOnTop ? this.alwaysOnTop : phase;
    }

    @WrapWithCondition(
        method = "submitNameTag",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/feature/phase/TranslucentFeatureRenderPhase;submit(Lnet/minecraft/client/renderer/feature/submit/TranslucentSubmit;)V"
        )
    )
    private boolean interceptTranslucentNameTags(TranslucentFeatureRenderPhase phase, TranslucentSubmit node){
        if(!this.forceAlwaysOnTop)
            return true;
        this.alwaysOnTop.submit(node);
        return false;
    }
}
