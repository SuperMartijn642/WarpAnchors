package com.supermartijn642.warpanchors;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;
import com.supermartijn642.warpanchors.content.WarpAnchorBlockEntity;
import com.supermartijn642.warpanchors.content.particle.WarpParticleProvider;
import com.supermartijn642.warpanchors.content.screen.WarpAnchorScreen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

import java.util.function.Consumer;

@Mod(value = WarpAnchors.MODID, dist = Dist.CLIENT)
public class WarpAnchorsClient {

    public WarpAnchorsClient(IEventBus eventBus){
        eventBus.addListener((Consumer<RegisterColorHandlersEvent.Block>)e -> {
            e.register(
                (state, level, pos, tintIndex) -> state.getValue(WarpAnchorBlock.COLOR).getTint(),
                WarpAnchors.warpAnchor
            );
        });
        eventBus.addListener((Consumer<RegisterColorHandlersEvent.Item>)e -> {
            e.register(
                (stack, tintIndex) -> stack.getOrDefault(WarpAnchorBlock.COLOR_DATA_COMPONENT, WarpAnchorBlock.Color.NONE).getTint(),
                WarpAnchors.warpAnchor
            );
        });
        eventBus.addListener((Consumer<FMLClientSetupEvent>)e -> e.enqueueWork(() -> {
            ItemProperties.register(WarpAnchors.warpAnchor.asItem(), WarpAnchors.identifier("colored"), (stack, level, entity, seed) -> stack.getOrDefault(WarpAnchorBlock.COLOR_DATA_COMPONENT, WarpAnchorBlock.Color.NONE) == WarpAnchorBlock.Color.NONE ? 0 : 1);
        }));
        eventBus.addListener((Consumer<RegisterParticleProvidersEvent>)e -> {
            e.registerSpriteSet(WarpAnchors.warpParticle, WarpParticleProvider::new);
        });
    }

    public static void showAnchorScreen(BlockPos pos, boolean editable){
        if(ClientUtils.getWorld().getBlockEntity(pos) instanceof WarpAnchorBlockEntity anchor)
            ClientUtils.displayScreen(WidgetScreen.of(new WarpAnchorScreen(anchor, editable)));
    }
}
