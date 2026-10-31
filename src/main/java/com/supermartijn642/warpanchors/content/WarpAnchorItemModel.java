package com.supermartijn642.warpanchors.content;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class WarpAnchorItemModel implements ItemModel.Unbaked {

    public static final MapCodec<WarpAnchorItemModel> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
        Identifier.CODEC.fieldOf("colorless_model").forGetter(m -> m.colorlessModel),
        Identifier.CODEC.fieldOf("colored_model").forGetter(m -> m.coloredModel)
    ).apply(i, WarpAnchorItemModel::new));
    private static final ItemTintSource TINT_SOURCE = new ItemTintSource() {
        @Override
        public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner){
            return stack.getOrDefault(WarpAnchorBlock.COLOR_DATA_COMPONENT, WarpAnchorBlock.Color.NONE).getTint();
        }

        @Override
        public MapCodec<? extends ItemTintSource> type(){
            return TINT_CODEC;
        }
    };
    public static final MapCodec<ItemTintSource> TINT_CODEC = MapCodec.unit(TINT_SOURCE);

    private final Identifier colorlessModel, coloredModel;

    public WarpAnchorItemModel(Identifier colorlessModel, Identifier coloredModel){
        this.colorlessModel = colorlessModel;
        this.coloredModel = coloredModel;
    }

    @Override
    public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation){
        ItemModel colorless = new CuboidItemModelWrapper.Unbaked(this.colorlessModel, Optional.empty(), List.of()).bake(context, transformation);
        ItemModel colored = new CuboidItemModelWrapper.Unbaked(this.coloredModel, Optional.empty(), List.of(TINT_SOURCE)).bake(context, transformation);
        return (output, item, resolver, displayContext, level, owner, seed) -> {
            WarpAnchorBlock.Color color = item.getOrDefault(WarpAnchorBlock.COLOR_DATA_COMPONENT, WarpAnchorBlock.Color.NONE);
            output.appendModelIdentityElement(color);
            if(color == WarpAnchorBlock.Color.NONE)
                colorless.update(output, item, resolver, displayContext, level, owner, seed);
            else
                colored.update(output, item, resolver, displayContext, level, owner, seed);
        };
    }

    @Override
    public void resolveDependencies(Resolver resolver){
        resolver.markDependency(this.colorlessModel);
        resolver.markDependency(this.coloredModel);
    }

    @Override
    public MapCodec<? extends ItemModel.Unbaked> type(){
        return CODEC;
    }
}
