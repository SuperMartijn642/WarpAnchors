package com.supermartijn642.warpanchors.content.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.warpanchors.WarpAnchors;
import com.supermartijn642.warpanchors.content.WarpAnchorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public class RequestScreenPacket implements BasePacket {

    private BlockPos pos;

    public RequestScreenPacket(BlockPos pos){
        this.pos = pos;
    }

    public RequestScreenPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeBlockPos(this.pos);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.pos = buffer.readBlockPos();
    }

    @Override
    public void handle(PacketContext context){
        Player player = context.getPlayer();
        if(this.pos.distToCenterSqr(player.position()) > 32 * 32)
            return;
        if(!(context.getWorld().getBlockEntity(this.pos) instanceof WarpAnchorBlockEntity))
            return;
        boolean editable = player.getAbilities().mayBuild && player.mayInteract((ServerLevel)context.getWorld(), this.pos);
        WarpAnchors.CHANNEL.sendToPlayer(player, new OpenScreenPacket(this.pos, editable));
    }
}
