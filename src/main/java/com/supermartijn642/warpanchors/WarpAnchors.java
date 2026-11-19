package com.supermartijn642.warpanchors;

import com.supermartijn642.core.CommonUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlockEntityType;
import com.supermartijn642.core.block.BlockProperties;
import com.supermartijn642.core.item.BaseBlockItem;
import com.supermartijn642.core.item.CreativeItemGroup;
import com.supermartijn642.core.item.ItemProperties;
import com.supermartijn642.core.network.PacketChannel;
import com.supermartijn642.core.network.PacketDirection;
import com.supermartijn642.core.registry.GeneratorRegistrationHandler;
import com.supermartijn642.core.registry.RegistrationHandler;
import com.supermartijn642.core.registry.RegistryEntryAcceptor;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;
import com.supermartijn642.warpanchors.content.WarpAnchorBlockEntity;
import com.supermartijn642.warpanchors.content.packets.*;
import com.supermartijn642.warpanchors.content.particle.WarpParticleType;
import com.supermartijn642.warpanchors.generators.*;
import net.fabricmc.api.ModInitializer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public class WarpAnchors implements ModInitializer {

    public static final String MODID = "warpanchors";

    public static ResourceLocation identifier(String path){
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static final PacketChannel CHANNEL = PacketChannel.create(MODID);

    @RegistryEntryAcceptor(namespace = MODID, identifier = "warp_anchor", registry = RegistryEntryAcceptor.Registry.BLOCKS)
    public static WarpAnchorBlock warpAnchor;
    @RegistryEntryAcceptor(namespace = MODID, identifier = "warp_anchor", registry = RegistryEntryAcceptor.Registry.BLOCK_ENTITY_TYPES)
    public static BaseBlockEntityType<?> warpAnchorBlockEntity;
    @RegistryEntryAcceptor(namespace = MODID, identifier = "warp", registry = RegistryEntryAcceptor.Registry.PARTICLE_TYPES)
    public static WarpParticleType warpParticle;

    public static final CreativeItemGroup CREATIVE_GROUP = CreativeItemGroup.create(MODID, () -> warpAnchor.asItem()).filler(output -> {
        for(WarpAnchorBlock.Color color : WarpAnchorBlock.Color.VALUES){
            ItemStack stack = new ItemStack(warpAnchor);
            stack.set(WarpAnchorBlock.COLOR_DATA_COMPONENT, color);
            output.accept(stack);
        }
    });

    @Override
    public void onInitialize(){
        CHANNEL.registerMessage(OpenScreenPacket.class, OpenScreenPacket::new, PacketDirection.SERVER_TO_CLIENT, true);
        CHANNEL.registerMessage(RequestScreenPacket.class, RequestScreenPacket::new, PacketDirection.CLIENT_TO_SERVER, true);
        CHANNEL.registerMessage(SetColorPacket.class, SetColorPacket::new, PacketDirection.CLIENT_TO_SERVER, true);
        CHANNEL.registerMessage(SetNamePacket.class, SetNamePacket::new, PacketDirection.CLIENT_TO_SERVER, true);
        CHANNEL.registerMessage(WarpPacket.class, WarpPacket::new, PacketDirection.CLIENT_TO_SERVER, true);

        register();
        WarpAnchorsConfig.init();
        if(CommonUtils.getEnvironmentSide().isClient())
            registerGenerators();
    }

    private static void register(){
        RegistrationHandler handler = RegistrationHandler.get(MODID);
        handler.registerBlock("warp_anchor", () -> new WarpAnchorBlock(BlockProperties.create().requiresCorrectTool().pushReaction(PushReaction.BLOCK).sound(SoundType.METAL).lightLevel(3).mapColor(MapColor.COLOR_LIGHT_GRAY).destroyTime(5).explosionResistance(6)));
        handler.registerBlockEntityType("warp_anchor", () -> BaseBlockEntityType.create(WarpAnchorBlockEntity::new, warpAnchor));
        handler.registerItem("warp_anchor", () -> new BaseBlockItem(warpAnchor, ItemProperties.create().group(CREATIVE_GROUP).component(WarpAnchorBlock.COLOR_DATA_COMPONENT, WarpAnchorBlock.Color.NONE)){
            @Override
            public Component getName(ItemStack stack){
                WarpAnchorBlock.Color color = stack.getOrDefault(WarpAnchorBlock.COLOR_DATA_COMPONENT, WarpAnchorBlock.Color.NONE);
                return color == WarpAnchorBlock.Color.NONE ?
                    super.getName(stack) :
                    TextComponents.translation("warpanchors.warp_anchor.name.colored", color.getTranslation()).get();
            }
        });
        handler.registerParticleType("warp", WarpParticleType::new);
        handler.registerDataComponentType("color", WarpAnchorBlock.COLOR_DATA_COMPONENT);
    }

    public static void registerGenerators(){
        GeneratorRegistrationHandler handler = GeneratorRegistrationHandler.get(MODID);
        handler.addGenerator(WarpAnchorsAtlasSourceGenerator::new);
        handler.addGenerator(WarpAnchorsBlockStateGenerator::new);
        handler.addGenerator(WarpAnchorsItemInfoGenerator::new);
        handler.addGenerator(WarpAnchorsLanguageGenerator::new);
        handler.addGenerator(WarpAnchorsLootTableGenerator::new);
        handler.addGenerator(WarpAnchorsRecipeGenerator::new);
        handler.addGenerator(WarpAnchorsTagGenerator::new);
    }
}
