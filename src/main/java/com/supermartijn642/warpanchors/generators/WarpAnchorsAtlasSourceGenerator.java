package com.supermartijn642.warpanchors.generators;

import com.supermartijn642.core.generator.AtlasSourceGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.warpanchors.WarpAnchors;
import com.supermartijn642.warpanchors.content.screen.ColorButton;
import com.supermartijn642.warpanchors.content.screen.WarpAnchorScreen;

public class WarpAnchorsAtlasSourceGenerator extends AtlasSourceGenerator {

    public WarpAnchorsAtlasSourceGenerator(ResourceCache cache){
        super(WarpAnchors.MODID, cache);
    }

    @Override
    public void generate(){
        this.atlas("minecraft", "gui")
            .texture(WarpAnchorScreen.BACKGROUND_ENDER)
            .texture(WarpAnchorScreen.BACKGROUND_FRAME)
            .texture(ColorButton.BACKGROUND)
            .texture(ColorButton.COLOR)
            .texture(ColorButton.CROSS);
    }
}
