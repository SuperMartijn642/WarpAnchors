package com.supermartijn642.warpanchors.content.screen;

import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.BaseWidget;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.warpanchors.WarpAnchors;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;
import com.supermartijn642.warpanchors.content.WarpAnchorBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class WarpAnchorScreen extends BaseWidget {

    public static final ResourceLocation BACKGROUND_FRAME = WarpAnchors.identifier("gui/background_frame");
    public static final ResourceLocation BACKGROUND_ENDER = WarpAnchors.identifier("gui/background_ender");

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
    public void renderBackground(WidgetRenderContext context, int mouseX, int mouseY){
        ColorButton.drawSprite(context.poseStack(), BACKGROUND_ENDER, this.x, this.y, this.width, this.height, 0, 1, -1);
        ColorButton.drawSprite(context.poseStack(), BACKGROUND_FRAME, this.x, this.y, this.width, this.height, 0, 1, -1);
        super.renderBackground(context, mouseX, mouseY);
    }

    @Override
    public void render(WidgetRenderContext context, int mouseX, int mouseY){
        super.render(context, mouseX, mouseY);
        ScreenUtils.drawStringWithShadow(context.poseStack(), TextComponents.translation("warpanchors.screen.name").color(ChatFormatting.WHITE).bold().get(), 16, 14, -1);
        ScreenUtils.drawStringWithShadow(context.poseStack(), TextComponents.translation("warpanchors.screen.color").color(ChatFormatting.WHITE).bold().get(), 16, 56, -1);
    }

    @Override
    public void renderTooltips(WidgetRenderContext context, int mouseX, int mouseY){
        super.renderTooltips(context, mouseX, mouseY);
        if(mouseX >= 12 && mouseX <= 49 && mouseY >= 12 && mouseY <= 22)
            ScreenUtils.drawTooltip(context.poseStack(), TextComponents.translation("warpanchors.screen.name.hint").get(), mouseX, mouseY);
        else if(mouseX >= 12 && mouseX <= 49 && mouseY >= 54 && mouseY <= 64)
            ScreenUtils.drawTooltip(context.poseStack(), TextComponents.translation("warpanchors.screen.color.hint").get(), mouseX, mouseY);
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation("warpanchors.screen.narrate").get();
    }
}
