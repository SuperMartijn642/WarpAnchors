package com.supermartijn642.warpanchors;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.core.registry.ClientRegistrationHandler;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;
import com.supermartijn642.warpanchors.content.WarpAnchorBlockEntity;
import com.supermartijn642.warpanchors.content.WarpAnchorItemModel;
import com.supermartijn642.warpanchors.content.particle.WarpParticleProvider;
import com.supermartijn642.warpanchors.content.screen.WarpAnchorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.core.BlockPos;

import java.util.List;

public class WarpAnchorsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(){
        ClientRegistrationHandler handler = ClientRegistrationHandler.get(WarpAnchors.MODID);
        handler.registerItemModelType("colorable", WarpAnchorItemModel.CODEC);
        BlockColorRegistry.register(
            List.of(state -> state.getValue(WarpAnchorBlock.COLOR).getTint()),
            WarpAnchors.warpAnchor
        );
        ItemTintSources.ID_MAPPER.put(WarpAnchors.identifier("color"), WarpAnchorItemModel.TINT_CODEC);
        ParticleProviderRegistry.getInstance().register(WarpAnchors.warpParticle, WarpParticleProvider::new);
    }

    public static void showAnchorScreen(BlockPos pos, boolean editable){
        if(ClientUtils.getWorld().getBlockEntity(pos) instanceof WarpAnchorBlockEntity anchor)
            ClientUtils.displayScreen(WidgetScreen.of(new WarpAnchorScreen(anchor, editable)));
    }
}
