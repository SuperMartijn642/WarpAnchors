package com.supermartijn642.warpanchors.mixin;

import com.supermartijn642.warpanchors.extension.SubmitNodeCollectorExtension;
import net.minecraft.client.renderer.SubmitNodeStorage;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SubmitNodeStorage.class)
public class SubmitNodeStorageMixin implements SubmitNodeCollectorExtension {
    @Override
    public void warpanchors_setAlwaysOnTop(boolean alwaysOnTop){
        //noinspection DataFlowIssue
        ((SubmitNodeCollectorExtension)((SubmitNodeStorage)(Object)this).order(0)).warpanchors_setAlwaysOnTop(alwaysOnTop);
    }
}
