package com.supermartijn642.warpanchors.content.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;
import com.supermartijn642.warpanchors.content.WarpAnchorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class SetColorPacket implements BasePacket {

    private WarpAnchorBlock.Color color;
    private BlockPos pos;

    public SetColorPacket(WarpAnchorBlock.Color color, BlockPos pos){
        this.color = color;
        this.pos = pos;
    }

    public SetColorPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeEnum(this.color);
        buffer.writeBlockPos(this.pos);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.color = buffer.readEnum(WarpAnchorBlock.Color.class);
        this.pos = buffer.readBlockPos();
    }

    @Override
    public void handle(PacketContext context){
        if(this.pos.distToCenterSqr(context.getPlayer().position()) > 32 * 32)
            return;
        if(!(context.getWorld().getBlockEntity(this.pos) instanceof WarpAnchorBlockEntity anchor))
            return;
        anchor.setColor(this.color);
    }
}
