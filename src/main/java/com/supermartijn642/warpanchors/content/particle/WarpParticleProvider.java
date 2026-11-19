package com.supermartijn642.warpanchors.content.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import org.jetbrains.annotations.Nullable;

public class WarpParticleProvider implements ParticleProvider<WarpParticleType.Options> {

    private final SpriteSet spriteSet;

    public WarpParticleProvider(SpriteSet spriteSet){
        this.spriteSet = spriteSet;
    }

    @Override
    public @Nullable Particle createParticle(WarpParticleType.Options options, ClientLevel level, double x, double y, double z, double xAux, double yAux, double zAux){
        return new WarpParticle(level, x, y, z, this.spriteSet.get(level.getRandom()), options.color(), level.getRandom());
    }
}
