package com.supermartijn642.warpanchors.generators;

import com.supermartijn642.core.generator.RecipeGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.warpanchors.WarpAnchors;
import net.fabricmc.fabric.api.tag.convention.v2.ConventionalItemTags;

public class WarpAnchorsRecipeGenerator extends RecipeGenerator {

    public WarpAnchorsRecipeGenerator(ResourceCache cache){
        super(WarpAnchors.MODID, cache);
    }

    @Override
    public void generate(){
        this.shaped(WarpAnchors.warpAnchor)
            .pattern("ABA")
            .pattern("BCB")
            .pattern("ABA")
            .input('A', ConventionalItemTags.IRON_INGOTS)
            .input('B', ConventionalItemTags.LAPIS_GEMS)
            .input('C', ConventionalItemTags.ENDER_PEARLS)
            .unlockedBy(ConventionalItemTags.ENDER_PEARLS);
    }
}
