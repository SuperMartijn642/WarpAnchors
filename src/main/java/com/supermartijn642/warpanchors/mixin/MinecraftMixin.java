package com.supermartijn642.warpanchors.mixin;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.warpanchors.content.WarpHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public class MinecraftMixin {

    @Shadow
    private int missTime;

    @Inject(
        method = "startAttack()Z",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/player/LocalPlayer;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;",
            shift = At.Shift.BEFORE
        ),
        cancellable = true
    )
    private void leftClickTeleport(CallbackInfoReturnable<Boolean> cir){
        Player player = ClientUtils.getPlayer();
        if(player.isSpectator())
            return;
        if(WarpHandler.tryTeleport()){
            player.swing(InteractionHand.MAIN_HAND);
            this.missTime = 40;
            cir.setReturnValue(false);
        }
    }
}
