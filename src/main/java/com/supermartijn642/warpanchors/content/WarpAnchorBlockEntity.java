package com.supermartijn642.warpanchors.content;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.block.BaseBlockEntity;
import com.supermartijn642.core.util.teleport.TeleportTarget;
import com.supermartijn642.core.util.teleport.Teleporter;
import com.supermartijn642.warpanchors.WarpAnchors;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class WarpAnchorBlockEntity extends BaseBlockEntity {

    private String name = "";

    public WarpAnchorBlockEntity(BlockPos pos, BlockState state){
        super(WarpAnchors.warpAnchorBlockEntity, pos, state);
    }

    public WarpAnchorBlock.Color getColor(){
        return this.getBlockState().getValue(WarpAnchorBlock.COLOR);
    }

    public void setColor(WarpAnchorBlock.Color color){
        this.level.setBlock(this.worldPosition, this.getBlockState().setValue(WarpAnchorBlock.COLOR, color), 2);
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
        this.dataChanged();
    }

    public void warp(Player player, WarpAnchorBlockEntity from){
        // Don't warp the player into blocks above the anchor
        Vec3 offset = new Vec3(this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.1, this.worldPosition.getZ() + 0.5).subtract(player.position());
        if(!this.level.noCollision(player, player.getBoundingBox().move(offset))){
            player.displayClientMessage(TextComponents.translation("warpanchors.warp.obstructed").color(ChatFormatting.RED).get(), true);
            return;
        }
        BlockPos oldPos = player.blockPosition();
        float oldPlayerWidth = player.getBbWidth() + 0.4f, oldPlayerHeight = player.getBbHeight() + 0.2f;
        Vec3 oldPlayerCenter = player.getBoundingBox().getCenter();
        Teleporter.Result result = Teleporter.create(TeleportTarget.position(this.worldPosition.above()))
            .includeMount(true, true, true, true)
            .includePassengers(true, true)
            .resetFallDistance()
            .teleport(player);
        if(!result.succeeded())
            return;
        this.level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
        from.level.playSound(null, oldPos, SoundEvents.PLAYER_TELEPORT, SoundSource.PLAYERS);
        float playerWidth = player.getBbWidth() + 0.2f, playerHeight = player.getBbHeight();
        Vec3 playerCenter = player.getBoundingBox().getCenter();
        ((ServerLevel)this.level).sendParticles(
            WarpAnchors.warpParticle.create(this.getColor()),
            true,
            false,
            playerCenter.x, playerCenter.y, playerCenter.z,
            50,
            playerWidth / 2, playerHeight / 2, playerWidth / 2,
            0
        );
        ((ServerLevel)from.level).sendParticles(
            WarpAnchors.warpParticle.create(from.getColor()),
            true,
            false,
            oldPlayerCenter.x, oldPlayerCenter.y, oldPlayerCenter.z,
            50,
            oldPlayerWidth / 2, oldPlayerHeight / 2, oldPlayerWidth / 2,
            0
        );
    }

    @Override
    public void setLevel(Level level){
        super.setLevel(level);
        if(level.isClientSide())
            WarpHandler.add(this);
    }

    @Override
    public void setRemoved(){
        super.setRemoved();
        if(this.level != null && this.level.isClientSide())
            WarpHandler.remove(this);
    }

    @Override
    public void clearRemoved(){
        super.clearRemoved();
        if(this.level != null && this.level.isClientSide())
            WarpHandler.add(this);
    }

    @Override
    public void setBlockState(BlockState blockState){
        if(this.level != null && this.level.isClientSide())
            WarpHandler.remove(this);
        super.setBlockState(blockState);
        if(this.level != null && this.level.isClientSide())
            WarpHandler.add(this);
    }

    @Override
    protected void writeData(ValueOutput output){
        output.putString("name", this.name);
    }

    @Override
    protected void readData(ValueInput input){
        this.name = input.getStringOr("name", "");
    }
}
