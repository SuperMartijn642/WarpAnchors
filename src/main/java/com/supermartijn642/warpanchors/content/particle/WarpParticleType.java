package com.supermartijn642.warpanchors.content.particle;

import com.mojang.serialization.MapCodec;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class WarpParticleType extends ParticleType<WarpParticleType.Options> {

    private final MapCodec<Options> codec = WarpAnchorBlock.Color.CODEC.xmap(this::create,Options::color).fieldOf("color");
    private final StreamCodec<ByteBuf,Options> streamCodec = WarpAnchorBlock.Color.STREAM_CODEC.map(this::create,Options::color);

    private final Options[] byColor = new Options[WarpAnchorBlock.Color.VALUES.length];
    {
        for(int i = 0; i < WarpAnchorBlock.Color.VALUES.length; i++)
            this.byColor[i] = new Options(WarpAnchorBlock.Color.VALUES[i]);
    }

    public WarpParticleType(){
        super(false);
    }

    public Options create(WarpAnchorBlock.Color color){
        return this.byColor[color.ordinal()];
    }

    @Override
    public MapCodec<Options> codec(){
        return this.codec;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf,Options> streamCodec(){
        return this.streamCodec;
    }

    public class Options implements ParticleOptions {

        private final WarpAnchorBlock.Color color;

        public Options(WarpAnchorBlock.Color color){
            this.color = color;
        }

        public WarpAnchorBlock.Color color(){
            return this.color;
        }

        @Override
        public ParticleType<?> getType(){
            return WarpParticleType.this;
        }
    }
}
