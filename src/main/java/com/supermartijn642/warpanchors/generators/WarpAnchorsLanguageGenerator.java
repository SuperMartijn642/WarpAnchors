package com.supermartijn642.warpanchors.generators;

import com.supermartijn642.core.generator.LanguageGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.warpanchors.WarpAnchors;

public class WarpAnchorsLanguageGenerator extends LanguageGenerator {

    public WarpAnchorsLanguageGenerator(ResourceCache cache){
        super(WarpAnchors.MODID, cache, "en_us");
    }

    @Override
    public void generate(){
        this.block(WarpAnchors.warpAnchor, "Warp Anchor");
        this.itemGroup(WarpAnchors.CREATIVE_GROUP, "Warp Anchors");

        // Screen
        this.translation("warpanchors.screen.name", "Name");
        this.translation("warpanchors.screen.name.suggestion", "Type");
        this.translation("warpanchors.screen.name.hint", "Name is shown when looking at anchors to warp to");
        this.translation("warpanchors.screen.name_field.narrate", "Text field: Enter name");
        this.translation("warpanchors.screen.color", "Color");
        this.translation("warpanchors.screen.color.hint", "Colored anchors can only warp to colorless anchors and anchors of the same color");
        this.translation("warpanchors.screen.color_button.narrate", "Select color %s");
        this.translation("warpanchors.screen.color.none", "None");
        this.translation("warpanchors.screen.narrate", "Warp Anchor configuration screen");
    }
}
