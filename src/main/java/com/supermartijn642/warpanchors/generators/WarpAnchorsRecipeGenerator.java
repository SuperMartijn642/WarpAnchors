package com.supermartijn642.warpanchors.generators;

import com.supermartijn642.core.generator.RecipeGenerator;
import com.supermartijn642.core.generator.ResourceCache;
import com.supermartijn642.warpanchors.WarpAnchors;
import net.neoforged.neoforge.common.Tags;

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
            .input('A', Tags.Items.INGOTS_IRON)
            .input('B', Tags.Items.GEMS_LAPIS)
            .input('C', Tags.Items.ENDER_PEARLS)
            .unlockedBy(Tags.Items.ENDER_PEARLS);
    }
}
