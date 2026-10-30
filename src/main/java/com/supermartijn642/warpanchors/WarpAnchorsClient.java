package com.supermartijn642.warpanchors;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;
import com.supermartijn642.warpanchors.content.WarpAnchorBlockEntity;
import com.supermartijn642.warpanchors.content.particle.WarpParticleProvider;
import com.supermartijn642.warpanchors.content.screen.WarpAnchorScreen;
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
        eventBus.addListener((Consumer<RegisterColorHandlersEvent.BlockTintSources>)e -> {
            e.register(
                List.of(state -> state.getValue(WarpAnchorBlock.COLOR).getTint()),
                WarpAnchors.warpAnchor
            );
        });
        eventBus.addListener((Consumer<RegisterParticleProvidersEvent>)e -> {
            e.registerSpriteSet(WarpAnchors.warpParticle, WarpParticleProvider::new);
        });
    }

    public static void showAnchorScreen(WarpAnchorBlockEntity anchor){
        ClientUtils.displayScreen(WidgetScreen.of(new WarpAnchorScreen(anchor)));
    }
}
