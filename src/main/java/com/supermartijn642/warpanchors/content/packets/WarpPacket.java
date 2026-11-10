package com.supermartijn642.warpanchors.content.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.core.util.teleport.TeleportTarget;
import com.supermartijn642.core.util.teleport.Teleporter;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;
import com.supermartijn642.warpanchors.content.WarpAnchorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class WarpPacket implements BasePacket {

    private BlockPos target, from;

    public WarpPacket(BlockPos target, BlockPos from){
        this.target = target;
        this.from = from;
    }

    public WarpPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeBlockPos(this.target);
        buffer.writeBlockPos(this.from);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.target = buffer.readBlockPos();
        this.from = buffer.readBlockPos();
    }

    @Override
    public void handle(PacketContext context){
        Player player = context.getPlayer();
        if(this.from.distSqr(player.getOnPos()) > 16)
            return;
        if(this.target.distSqr(this.from) > WarpAnchorBlock.RANGE_SQUARED + 0.1)
            return;
        Level level = player.level();
        if(!level.getWorldBorder().isWithinBounds(this.target) || !level.hasChunkAt(this.target))
            return;
        if(!(level.getBlockEntity(this.from) instanceof WarpAnchorBlockEntity fromAnchor)
            || !(level.getBlockEntity(this.target) instanceof WarpAnchorBlockEntity targetAnchor))
            return;
        if(fromAnchor.getColor() != WarpAnchorBlock.Color.NONE
            && targetAnchor.getColor() != WarpAnchorBlock.Color.NONE
            && fromAnchor.getColor() != targetAnchor.getColor())
            return;
        targetAnchor.warp(player, fromAnchor);
    }
}
