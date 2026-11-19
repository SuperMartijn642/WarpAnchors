package com.supermartijn642.warpanchors.content.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.core.TextComponents;
import com.supermartijn642.core.gui.ScreenUtils;
import com.supermartijn642.core.gui.widget.WidgetRenderContext;
import com.supermartijn642.core.gui.widget.premade.AbstractButtonWidget;
import com.supermartijn642.warpanchors.WarpAnchors;
import com.supermartijn642.warpanchors.content.WarpAnchorBlock;
import com.supermartijn642.warpanchors.content.WarpAnchorBlockEntity;
import com.supermartijn642.warpanchors.content.packets.SetColorPacket;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

public class ColorButton extends AbstractButtonWidget {

    public static final ResourceLocation BACKGROUND = WarpAnchors.identifier("gui/color_button");
    public static final ResourceLocation COLOR = WarpAnchors.identifier("gui/color");
    public static final ResourceLocation CROSS = WarpAnchors.identifier("gui/cross");

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
    public void renderBackground(WidgetRenderContext context, int mouseX, int mouseY){
        super.renderBackground(context, mouseX, mouseY);
        drawSprite(context.poseStack(), context.buffers(), BACKGROUND, this.x, this.y, this.width, this.height, (this.anchor.getColor() == this.color ? 2 : this.isFocused() && this.isActive() ? 1 : 0) / 3f, 1 / 3f, -1);
        if(this.color == WarpAnchorBlock.Color.NONE)
            ScreenUtils.drawGuiSprite(CROSS, context.poseStack(), this.x + 3, this.y + 3, this.width - 6, this.height - 6);
        else
            drawSprite(context.poseStack(), context.buffers(), COLOR, this.x + 2, this.y + 2, this.width - 4, this.height - 4, 0, 1, this.color.getTint());
    }

    private static void drawSprite(PoseStack poseStack, MultiBufferSource bufferSource, ResourceLocation location, float x, float y, float width, float height, float v, float vHeight, int color){
        TextureAtlasSprite sprite = ClientUtils.getMinecraft().getGuiSprites().getSprite(location);
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.guiTextured(sprite.atlasLocation()));
        Matrix4f matrix = poseStack.last().pose();
        float v0 = sprite.getV(v), v1 = sprite.getV(v + vHeight);
        buffer.addVertex(matrix, x, y + height, 0).setUv(sprite.getU0(), v1).setColor(color);
        buffer.addVertex(matrix, x + width, y + height, 0).setUv(sprite.getU1(), v1).setColor(color);
        buffer.addVertex(matrix, x + width, y, 0).setUv(sprite.getU1(), v0).setColor(color);
        buffer.addVertex(matrix, x, y, 0).setUv(sprite.getU0(), v0).setColor(color);
    }

    @Override
    public Component getNarrationMessage(){
        return TextComponents.translation(
            "warpanchors.screen.color_button.narrate",
            this.color.getTranslation()
        ).get();
    }
}
