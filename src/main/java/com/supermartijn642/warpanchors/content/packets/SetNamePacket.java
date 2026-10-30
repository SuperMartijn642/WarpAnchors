package com.supermartijn642.warpanchors.content.packets;

import com.supermartijn642.core.network.BasePacket;
import com.supermartijn642.core.network.PacketContext;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;
import com.supermartijn642.warpanchors.content.WarpAnchorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.StringUtil;

public class SetNamePacket implements BasePacket {

    private String name;
    private BlockPos pos;

    public SetNamePacket(String name, BlockPos pos) {
        this.name = name;
        this.pos = pos;
    }

    public SetNamePacket(){
    }

    @Override
    public void write(FriendlyByteBuf buffer){
        buffer.writeUtf(this.name.trim(), WarpAnchorBlock.MAX_NAME_LENGTH);
        buffer.writeBlockPos(this.pos);
    }

    @Override
    public void read(FriendlyByteBuf buffer){
        this.name = buffer.readUtf(WarpAnchorBlock.MAX_NAME_LENGTH).trim();
        this.pos = buffer.readBlockPos();
    }

    @Override
    public boolean verify(PacketContext context){
        return this.name.length() <= WarpAnchorBlock.MAX_NAME_LENGTH &&
            StringUtil.filterText(this.name).equals(this.name);
    }

    @Override
    public void handle(PacketContext context){
        if(this.pos.distToCenterSqr(context.getPlayer().position()) > 32 * 32)
            return;
        if(!(context.getWorld().getBlockEntity(this.pos) instanceof WarpAnchorBlockEntity anchor))
            return;
        anchor.setName(this.name);
    }
}
