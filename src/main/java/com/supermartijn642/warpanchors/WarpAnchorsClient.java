package com.supermartijn642.warpanchors;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;
import com.supermartijn642.warpanchors.content.WarpAnchorBlockEntity;
import com.supermartijn642.warpanchors.content.WarpAnchorItemModel;
import com.supermartijn642.warpanchors.content.particle.WarpParticleProvider;
import com.supermartijn642.warpanchors.content.screen.WarpAnchorScreen;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import java.util.List;
import java.util.function.Consumer;

@Mod(value = WarpAnchors.MODID, dist = Dist.CLIENT)
public class WarpAnchorsClient {

    public WarpAnchorsClient(IEventBus eventBus){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get(WarpAnchors.MODID);
        handler.registerItemModelType("colorable", WarpAnchorItemModel.CODEC);
        eventBus.addListener((Consumer<RegisterColorHandlersEvent.BlockTintSources>)e -> {
            e.register(
                List.of(state -> state.getValue(WarpAnchorBlock.COLOR).getTint()),
                WarpAnchors.warpAnchor
            );
        });
        eventBus.addListener((Consumer<RegisterColorHandlersEvent.ItemTintSources>)e -> {
            e.register(WarpAnchors.identifier("color"), WarpAnchorItemModel.TINT_CODEC);
        });
        eventBus.addListener((Consumer<RegisterParticleProvidersEvent>)e -> {
            e.registerSpriteSet(WarpAnchors.warpParticle, WarpParticleProvider::new);
        });
    }

    public static void showAnchorScreen(BlockPos pos, boolean editable){
        if(ClientUtils.getWorld().getBlockEntity(pos) instanceof WarpAnchorBlockEntity anchor)
            ClientUtils.displayScreen(WidgetScreen.of(new WarpAnchorScreen(anchor, editable)));
    }
}
