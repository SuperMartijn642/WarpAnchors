package com.supermartijn642.warpanchors.generators;

import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.core.generator.TagGenerator;
import com.supermartijn642.warpanchors.WarpAnchors;

public class WarpAnchorsTagGenerator extends TagGenerator {

    public WarpAnchorsTagGenerator(ResourceCache cache){
        super(WarpAnchors.MODID, cache);
    }

    @Override
    public void generate(){
        this.blockMineableWithPickaxe().add(WarpAnchors.warpAnchor);
    }
}
