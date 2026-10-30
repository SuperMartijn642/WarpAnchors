package com.supermartijn642.warpanchors.generators;

import com.supermartijn642.core.generator.BlockStateGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.warpanchors.WarpAnchors;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;

public class WarpAnchorsBlockStateGenerator extends BlockStateGenerator {

    public WarpAnchorsBlockStateGenerator(ResourceCache cache){
        super(WarpAnchors.MODID, cache);
    }

    @Override
    public void generate(){
        this.blockState(WarpAnchors.warpAnchor).variantsForProperty(
            WarpAnchorBlock.COLOR,
            (state, variant) -> {
                if(state.get(WarpAnchorBlock.COLOR) == WarpAnchorBlock.Color.NONE)
                    variant.model("block/warp_anchor");
                else
                    variant.model("block/warp_anchor_tinted");
            }
        );
    }
}
