package com.supermartijn642.warpanchors.content.screen;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.widget.premade.TextFieldWidget;
import com.supermartijn642.warpanchors.WarpAnchors;
import com.supermartijn642.warpanchors.content.WarpAnchorBlockEntity;
import com.supermartijn642.warpanchors.content.packets.SetNamePacket;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

import java.util.LinkedList;
import java.util.List;

public class AnchorNameField extends TextFieldWidget {

    private final WarpAnchorBlockEntity anchor;
    private String lastSyncedName;
    private final List<String> pastNames = new LinkedList<>();

    public AnchorNameField(int x, int y, int width, int height, int maxLength, WarpAnchorBlockEntity anchor){
        super(x, y, width, height, anchor.getName(), maxLength);
        this.anchor = anchor;
        this.lastSyncedName = anchor.getName();
        this.setSuggestion(I18n.get("warpanchors.screen.name.suggestion"));
        this.setDrawBackground(false);
    }

    @Override
    public void update(){
        super.update();

        String currentName = this.anchor.getName();
        if(!currentName.equals(this.lastSyncedName)){
            if(currentName.equals(this.getText()))
                this.pastNames.clear();
            else{
                int index = this.pastNames.indexOf(currentName);
                if(index < 0){
                    this.setTextSuppressed(currentName);
                    this.cursorPosition = this.selectionPos = this.getText().length();
                    this.moveLineOffsetToCursor();
                }else
                    this.pastNames.subList(0, index + 1).clear();
            }
            this.lastSyncedName = currentName;
        }
    }

    @Override
    protected void onTextChanged(String oldText, String newText){
        this.pastNames.add(oldText);
        WarpAnchors.CHANNEL.sendToServer(new SetNamePacket(newText, this.anchor.getBlockPos()));
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("warpanchors.screen.name_field.narrate").get();
    }
}
