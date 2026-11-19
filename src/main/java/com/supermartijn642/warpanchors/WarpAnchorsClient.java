package com.supermartijn642.warpanchors;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;
import com.supermartijn642.warpanchors.content.WarpAnchorBlockEntity;
import com.supermartijn642.warpanchors.content.particle.WarpParticleProvider;
import com.supermartijn642.warpanchors.content.screen.WarpAnchorScreen;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.util.function.Consumer;

public class WarpAnchorsClient {

    public static void init(FMLJavaModLoadingContext context){
        context.getModEventBus().addListener((Consumer<RegisterColorHandlersEvent.Block>)e -> {
            e.register(
                (state, level, pos, tintIndex) -> state.getValue(WarpAnchorBlock.COLOR).getTint(),
                WarpAnchors.warpAnchor
            );
        });
        context.getModEventBus().addListener((Consumer<RegisterColorHandlersEvent.Item>)e -> {
            e.register(
                (stack, tintIndex) -> stack.getOrDefault(WarpAnchorBlock.COLOR_DATA_COMPONENT, WarpAnchorBlock.Color.NONE).getTint(),
                WarpAnchors.warpAnchor
            );
        });
        context.getModEventBus().addListener((Consumer<FMLClientSetupEvent>)e -> e.enqueueWork(() -> {
            ItemProperties.register(WarpAnchors.warpAnchor.asItem(), WarpAnchors.identifier("colored"), (stack, level, entity, seed) -> stack.getOrDefault(WarpAnchorBlock.COLOR_DATA_COMPONENT, WarpAnchorBlock.Color.NONE) == WarpAnchorBlock.Color.NONE ? 0 : 1);
        }));
        context.getModEventBus().addListener((Consumer<RegisterParticleProvidersEvent>)e -> {
            e.registerSpriteSet(WarpAnchors.warpParticle, WarpParticleProvider::new);
        });
    }

    public static void showAnchorScreen(BlockPos pos, boolean editable){
        if(ClientUtils.getWorld().getBlockEntity(pos) instanceof WarpAnchorBlockEntity anchor)
            ClientUtils.displayScreen(WidgetScreen.of(new WarpAnchorScreen(anchor, editable)));
    }
}
