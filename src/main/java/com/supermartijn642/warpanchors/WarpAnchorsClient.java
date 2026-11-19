package com.supermartijn642.warpanchors;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.gui.WidgetScreen;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;
import com.supermartijn642.warpanchors.content.WarpAnchorBlockEntity;
import com.supermartijn642.warpanchors.content.particle.WarpParticleProvider;
import com.supermartijn642.warpanchors.content.screen.WarpAnchorScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.fabricmc.fabric.api.object.builder.v1.client.model.FabricModelPredicateProviderRegistry;
import net.minecraft.core.BlockPos;

public class WarpAnchorsClient implements ClientModInitializer {

    @Override
    public void onInitializeClient(){
        ColorProviderRegistry.BLOCK.register(
            (state, level, pos, tintIndex) -> state.getValue(WarpAnchorBlock.COLOR).getTint(),
            WarpAnchors.warpAnchor
        );
        ColorProviderRegistry.ITEM.register(
            (stack, tintIndex) -> stack.getOrDefault(WarpAnchorBlock.COLOR_DATA_COMPONENT, WarpAnchorBlock.Color.NONE).getTint(),
            WarpAnchors.warpAnchor
        );
        FabricModelPredicateProviderRegistry.register(WarpAnchors.warpAnchor.asItem(), WarpAnchors.identifier("colored"), (stack, level, entity, seed) -> stack.getOrDefault(WarpAnchorBlock.COLOR_DATA_COMPONENT, WarpAnchorBlock.Color.NONE) == WarpAnchorBlock.Color.NONE ? 0 : 1);
        ParticleFactoryRegistry.getInstance().register(WarpAnchors.warpParticle, WarpParticleProvider::new);
    }

    public static void showAnchorScreen(BlockPos pos, boolean editable){
        if(ClientUtils.getWorld().getBlockEntity(pos) instanceof WarpAnchorBlockEntity anchor)
            ClientUtils.displayScreen(WidgetScreen.of(new WarpAnchorScreen(anchor, editable)));
    }
}
