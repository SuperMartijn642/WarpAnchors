package com.supermartijn642.warpanchors;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;
import com.supermartijn642.warpanchors.content.WarpAnchorBlockEntity;
import com.supermartijn642.warpanchors.content.particle.WarpParticleProvider;
import com.supermartijn642.warpanchors.content.screen.WarpAnchorScreen;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;

import java.util.List;

public class WarpAnchorsClient {

    public static void init(){
        RegisterColorHandlersEvent.Block.BUS.addListener(e -> {
            e.register(
                List.of(state -> state.getValue(WarpAnchorBlock.COLOR).getTint()),
                WarpAnchors.warpAnchor
            );
        });
        RegisterParticleProvidersEvent.BUS.addListener(e -> {
            e.registerSpriteSet(WarpAnchors.warpParticle, WarpParticleProvider::new);
        });
    }

    public static void showAnchorScreen(WarpAnchorBlockEntity anchor){
        ClientUtils.displayScreen(WidgetScreen.of(new WarpAnchorScreen(anchor)));
    }
}
