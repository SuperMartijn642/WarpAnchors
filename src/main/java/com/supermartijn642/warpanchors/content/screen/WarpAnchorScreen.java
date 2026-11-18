package com.supermartijn642.warpanchors.content.screen;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.warpanchors.WarpAnchors;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;
import com.supermartijn642.warpanchors.content.WarpAnchorBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class WarpAnchorScreen extends BaseWidget {

    public static final Identifier BACKGROUND_FRAME = WarpAnchors.identifier("gui/background_frame");
    public static final Identifier BACKGROUND_ENDER = WarpAnchors.identifier("gui/background_ender");

    private final WarpAnchorBlockEntity anchor;
    private final boolean editable;

    public WarpAnchorScreen(WarpAnchorBlockEntity anchor, boolean editable){
        super(0, 0, 176, 143);
        this.anchor = anchor;
        this.editable = editable;
    }

    @Override
    protected void addWidgets(){
        AnchorNameField nameField = this.addWidget(new AnchorNameField(15, 23, 146, 18, WarpAnchorBlock.MAX_NAME_LENGTH, this.anchor));
        nameField.setActive(this.editable);
        if(!this.editable)
            nameField.setSuggestion(null);
        int index = 0;
        for(WarpAnchorBlock.Color color : WarpAnchorBlock.Color.VALUES){
            ColorButton button = this.addWidget(new ColorButton(15 + index % 7 * 21, 66 + index / 7 * 21, this.anchor, color));
            button.setActive(this.editable);
            index++;
        }
    }

    @Override
    public void update(){
        if(this.anchor.isRemoved() || !this.anchor.hasLevel()
            || this.anchor.getBlockPos().distToCenterSqr(ClientUtils.getPlayer().position()) > 32 * 32
            || this.anchor.getLevel() != ClientUtils.getWorld())
            ClientUtils.closeScreen();
    }

    @Override
    public void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        graphics.submitSprite(BACKGROUND_ENDER, this.x, this.y, this.width, this.height);
        graphics.submitSprite(BACKGROUND_FRAME, this.x, this.y, this.width, this.height);
        super.renderBackground(context, graphics, mouseX, mouseY);
    }

    @Override
    public void render(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        super.render(context, graphics, mouseX, mouseY);
        graphics.submitText(TextComponents.translation("warpanchors.screen.name").color(ChatFormatting.WHITE).bold().get(), 16, 14, p -> p.shadow());
        graphics.submitText(TextComponents.translation("warpanchors.screen.color").color(ChatFormatting.WHITE).bold().get(), 16, 56, p -> p.shadow());
    }

    @Override
    public void renderTooltips(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        super.renderTooltips(context, graphics, mouseX, mouseY);
        if(mouseX >= 12 && mouseX <= 49 && mouseY >= 12 && mouseY <= 22)
            graphics.submitTooltipForTopStratum(t -> t.text(TextComponents.translation("warpanchors.screen.name.hint").get()), mouseX, mouseY);
        else if(mouseX >= 12 && mouseX <= 49 && mouseY >= 54 && mouseY <= 64)
            graphics.submitTooltipForTopStratum(t -> t.text(TextComponents.translation("warpanchors.screen.color.hint").get()), mouseX, mouseY);
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("warpanchors.screen.narrate").get();
    }
}
