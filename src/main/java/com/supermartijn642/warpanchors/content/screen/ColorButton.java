package com.supermartijn642.warpanchors.content.screen;

import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.GuiGraphicsHelper;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.warpanchors.WarpAnchors;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;
import com.supermartijn642.warpanchors.content.WarpAnchorBlockEntity;
import com.supermartijn642.warpanchors.content.packets.SetColorPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ColorButton extends AbstractButtonWidget {

    public static final Identifier BACKGROUND = WarpAnchors.identifier("gui/color_button");
    public static final Identifier COLOR = WarpAnchors.identifier("gui/color");
    public static final Identifier CROSS = WarpAnchors.identifier("gui/cross");

    private final WarpAnchorBlockEntity anchor;
    private final WarpAnchorBlock.Color color;

    public ColorButton(int x, int y, WarpAnchorBlockEntity anchor, WarpAnchorBlock.Color color){
        super(x, y, 20, 20, null);
        this.anchor = anchor;
        this.color = color;
    }

    @Override
    public void onPress(){
        super.onPress();
        if(this.anchor.getColor() != this.color)
            WarpAnchors.CHANNEL.sendToServer(new SetColorPacket(this.color, this.anchor.getBlockPos()));
    }

    @Override
    public void renderBackground(WidgetRenderContext context, GuiGraphicsHelper graphics, int mouseX, int mouseY){
        super.renderBackground(context, graphics, mouseX, mouseY);
        graphics.submitSprite(BACKGROUND, this.x, this.y, this.width, this.height, p -> p.uv(0, (this.anchor.getColor() == this.color ? 2 : this.isFocused() && this.isActive() ? 1 : 0) / 3f, 1, 1 / 3f));
        if(this.color == WarpAnchorBlock.Color.NONE)
            graphics.submitSprite(CROSS, this.x + 3, this.y + 3, this.width - 6, this.height - 6);
        else
            graphics.submitSprite(COLOR, this.x + 2, this.y + 2, this.width - 4, this.height - 4, p -> p.color(this.color.getTint()));
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation(
            "warpanchors.screen.color_button.narrate",
            this.color.getTranslation()
        ).get();
    }
}
