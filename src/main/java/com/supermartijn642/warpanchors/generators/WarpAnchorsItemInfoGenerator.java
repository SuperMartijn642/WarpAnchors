package com.supermartijn642.warpanchors.generators;

import com.supermartijn642.core.generator.ItemInfoGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.warpanchors.WarpAnchors;
import com.supermartijn642.warpanchors.content.WarpAnchorItemModel;

public class WarpAnchorsItemInfoGenerator extends ItemInfoGenerator {

    public WarpAnchorsItemInfoGenerator(ResourceCache cache){
        super(WarpAnchors.MODID, cache);
    }

    @Override
    public void generate(){
        this.info(WarpAnchors.warpAnchor)
            .model(new WarpAnchorItemModel(
                WarpAnchors.identifier("block/warp_anchor"),
                WarpAnchors.identifier("block/warp_anchor_tinted")
            ));
    }
}
