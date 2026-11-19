package com.supermartijn642.warpanchors.content;

import com.mojang.serialization.Codec;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlock;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.block.BlockShape;
import com.supermartijn642.core.block.EntityHoldingBlock;
import com.supermartijn642.warpanchors.WarpAnchors;
import com.supermartijn642.warpanchors.content.packets.RequestScreenPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.function.Consumer;

public class WarpAnchorBlock extends BaseBlock implements EntityHoldingBlock {

    public static int MAX_NAME_LENGTH = 20;

    public static final DataComponentType<Color> COLOR_DATA_COMPONENT = DataComponentType.<Color>builder().persistent(Color.CODEC).networkSynchronized(Color.STREAM_CODEC).build();

    private static final BlockShape OCCLUSION_SHAPE = BlockShape.createBlockShape(1, 1, 1, 15, 15, 15);
    public static final EnumProperty<Color> COLOR = EnumProperty.create("color", Color.class);

    public WarpAnchorBlock(BlockProperties properties){
        super(false, properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(COLOR, Color.NONE));
    }

    @Override
    protected InteractionFeedback interact(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, Direction hitSide, Vec3 hitLocation){
        if(level.isClientSide())
            WarpAnchors.CHANNEL.sendToServer(new RequestScreenPacket(pos));
        return InteractionFeedback.SUCCESS;
    }

    @Override
    public void appendItemInformation(ItemStack stack, Consumer<Component> info, boolean advanced){
        super.appendItemInformation(stack, info, advanced);
        info.accept(TextComponents.translation("warpanchors.warp_anchor.description").color(ChatFormatting.GRAY).get());
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context){
        Color color = context.getItemInHand().get(COLOR_DATA_COMPONENT);
        if(color != null)
            return this.defaultBlockState().setValue(COLOR, color);
        return this.defaultBlockState();
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state){
        ItemStack stack = new ItemStack(this);
        stack.set(COLOR_DATA_COMPONENT, state.getValue(COLOR));
        return stack;
    }

    @Override
    public BlockEntity createNewBlockEntity(BlockPos pos, BlockState state){
        return new WarpAnchorBlockEntity(pos, state);
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos){
        return OCCLUSION_SHAPE.getUnderlying();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){
        builder.add(COLOR);
    }

    public enum Color implements StringRepresentable {
        NONE(null),
        WHITE(DyeColor.WHITE),
        ORANGE(DyeColor.ORANGE),
        MAGENTA(DyeColor.MAGENTA),
        LIGHT_BLUE(DyeColor.LIGHT_BLUE),
        YELLOW(DyeColor.YELLOW),
        LIME(DyeColor.LIME),
        PINK(DyeColor.PINK),
        GRAY(DyeColor.GRAY),
        LIGHT_GRAY(DyeColor.LIGHT_GRAY),
        CYAN(DyeColor.CYAN),
        PURPLE(DyeColor.PURPLE),
        BLUE(DyeColor.BLUE),
        BROWN(DyeColor.BROWN),
        GREEN(DyeColor.GREEN),
        RED(DyeColor.RED),
        BLACK(DyeColor.BLACK);

        public static final Color[] VALUES = values();
        public static final Codec<Color> CODEC = StringRepresentable.fromEnum(Color::values);
        public static final StreamCodec<ByteBuf,Color> STREAM_CODEC = ByteBufCodecs.idMapper(i -> VALUES[Math.clamp(i, 0, VALUES.length - 1)], Enum::ordinal);

        private final String name = this.name().toLowerCase(Locale.ROOT);
        private final @Nullable DyeColor dyeColor;

        Color(@Nullable DyeColor dyeColor){
            this.dyeColor = dyeColor;
        }

        public @Nullable DyeColor getDyeColor(){
            return this.dyeColor;
        }

        public int getTint(){
            return this.dyeColor == null ? -1 : this.dyeColor.getTextureDiffuseColor();
        }

        public Component getTranslation(){
            return this == WarpAnchorBlock.Color.NONE ?
                TextComponents.translation("warpanchors.screen.color.none").get() :
                TextComponents.translation("color.minecraft." + this.dyeColor.getName()).get();
        }

        @Override
        public String getSerializedName(){
            return this.name;
        }
    }
}
