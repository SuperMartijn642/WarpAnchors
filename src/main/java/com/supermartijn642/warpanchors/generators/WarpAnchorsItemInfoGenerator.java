package com.supermartijn642.warpanchors.generators;

import com.supermartijn642.core.generator.ItemInfoGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.warpanchors.WarpAnchors;

public class WarpAnchorsItemInfoGenerator extends ItemInfoGenerator {

    public WarpAnchorsItemInfoGenerator(ResourceCache cache){
        super(WarpAnchors.MODID, cache);
    }

    @Override
    public void generate(){
        this.simpleInfo(WarpAnchors.warpAnchor, "block/warp_anchor");
    }
}
