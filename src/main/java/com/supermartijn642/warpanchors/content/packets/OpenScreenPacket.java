package com.supermartijn642.warpanchors.content.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.warpanchors.WarpAnchorsClient;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;

public class OpenScreenPacket implements BasePacket {

    private BlockPos pos;
    private boolean editable;

    public OpenScreenPacket(BlockPos pos, boolean editable){
        this.pos = pos;
        this.editable = editable;
    }

    public OpenScreenPacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeBlockPos(this.pos);
        buffer.writeBoolean(this.editable);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.pos = buffer.readBlockPos();
        this.editable = buffer.readBoolean();
    }

    @Override
    public void handle(PacketContext context){
        WarpAnchorsClient.showAnchorScreen(this.pos, this.editable);
    }
}
