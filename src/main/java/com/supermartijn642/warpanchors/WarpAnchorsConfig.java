package com.supermartijn642.warpanchors;

import com.supermartijn642.configlib.api.ConfigBuilders;
import com.supermartijn642.configlib.api.IConfigBuilder;

import java.util.function.Supplier;

public class WarpAnchorsConfig {

    public static final Supplier<Integer> anchorRange;

    static{
        IConfigBuilder builder = ConfigBuilders.newTomlConfig(WarpAnchors.MODID, null, false);

        builder.push("General");
        anchorRange = builder.comment("In what range should a warp anchor be able to warp players to other warp anchors?").define("anchorRange", 100, 1, 1000);
        builder.pop();

        builder.build();
    }

    public static void init(){
        // Cause this class to load
    }
}
