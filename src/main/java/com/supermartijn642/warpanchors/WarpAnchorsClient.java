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
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class WarpAnchorsClient {

    public static void init(FMLJavaModLoadingContext context){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get(WarpAnchors.MODID);
        handler.registerItemModelType("colorable", WarpAnchorItemModel.CODEC);
        RegisterColorHandlersEvent.Block.getBus(context.getModBusGroup()).addListener(e -> {
            e.register(
                (state, level, pos, tintIndex) -> state.getValue(WarpAnchorBlock.COLOR).getTint(),
                WarpAnchors.warpAnchor
            );
        });
        ItemTintSources.ID_MAPPER.put(WarpAnchors.identifier("color"), WarpAnchorItemModel.TINT_CODEC);
        RegisterParticleProvidersEvent.getBus(context.getModBusGroup()).addListener(e -> {
            e.registerSpriteSet(WarpAnchors.warpParticle, WarpParticleProvider::new);
        });
    }

    public static void showAnchorScreen(BlockPos pos, boolean editable){
        if(ClientUtils.getWorld().getBlockEntity(pos) instanceof WarpAnchorBlockEntity anchor)
            ClientUtils.displayScreen(WidgetScreen.of(new WarpAnchorScreen(anchor, editable)));
    }
}
