package com.supermartijn642.warpanchors.generators;

import com.supermartijn642.core.generator.LootTableGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.warpanchors.WarpAnchors;

public class WarpAnchorsLootTableGenerator extends LootTableGenerator {

    public WarpAnchorsLootTableGenerator(ResourceCache cache){
        super(WarpAnchors.MODID, cache);
    }

    @Override
    public void generate(){
        this.dropSelf(WarpAnchors.warpAnchor);
    }
}
