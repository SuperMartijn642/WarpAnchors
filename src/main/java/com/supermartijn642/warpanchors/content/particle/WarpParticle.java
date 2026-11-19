package com.supermartijn642.warpanchors.content.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import net.minecraft.util.RandomSource;
import org.joml.Math;

public class WarpParticle extends TextureSheetParticle {

    private static final int MIN_WARMUP = 5, MAX_WARMUP = 10;
    private static final int MIN_DEGRADE = 10, MAX_DEGRADE = 30;
    private static final float MIN_SIZE = 0.02f, MAX_SIZE = 0.1f;

    private final int warmupTime, degradeTime;
    private final float size;

    WarpParticle(ClientLevel level, double x, double y, double z, TextureAtlasSprite sprite, WarpAnchorBlock.Color color, RandomSource random){
        super(level, x, y, z);
        this.setSprite(sprite);
        this.hasPhysics = false;
        this.warmupTime = random.nextIntBetweenInclusive(MIN_WARMUP, MAX_WARMUP);
        this.degradeTime = random.nextIntBetweenInclusive(MIN_DEGRADE, MAX_DEGRADE);
        this.lifetime = this.warmupTime + this.degradeTime;
        this.size = MIN_SIZE + (MAX_SIZE - MIN_SIZE) * random.nextFloat();
        if(color == WarpAnchorBlock.Color.NONE)
            color = WarpAnchorBlock.Color.LIGHT_BLUE;
        this.rCol = ARGB.redFloat(color.getTint());
        this.gCol = ARGB.greenFloat(color.getTint());
        this.bCol = ARGB.blueFloat(color.getTint());
    }

    @Override
    public void tick(){
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if(this.age++ >= this.lifetime){
            this.remove();
            return;
        }
        this.move(this.xd, this.yd, this.zd);
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTickTime){
        float age = (this.age + partialTickTime);
        if(age >= this.warmupTime)
            this.quadSize = Math.cos((age - this.warmupTime) / this.degradeTime * Math.PI_OVER_2_f) * this.size;
        else
            this.quadSize = Math.sin(age / this.warmupTime * Math.PI_OVER_2_f) * this.size;
        super.render(buffer, camera, partialTickTime);
    }

    @Override
    protected int getLightColor(float a){
        return LightTexture.FULL_BRIGHT;
    }

    @Override
    public ParticleRenderType getRenderType(){
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }
}
