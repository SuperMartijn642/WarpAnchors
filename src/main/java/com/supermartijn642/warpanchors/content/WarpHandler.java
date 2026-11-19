package com.supermartijn642.warpanchors.content;

import com.google.common.collect.Iterables;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.supermartijn642.core.ClientUtils;
import com.supermartijn642.warpanchors.WarpAnchors;
import com.supermartijn642.warpanchors.WarpAnchorsConfig;
import com.supermartijn642.warpanchors.content.packets.WarpPacket;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.OutlineBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11;

import java.util.*;

public final class WarpHandler {

    private static final Set<WarpAnchorBlockEntity> COLORLESS_WARP_ANCHORS;
    private static final Map<WarpAnchorBlock.Color,Set<WarpAnchorBlockEntity>> WARP_ANCHORS = new EnumMap<>(WarpAnchorBlock.Color.class);

    static{
        for(WarpAnchorBlock.Color color : WarpAnchorBlock.Color.VALUES)
            // Weak references as it seems not all block entities are removed when leaving a level
            WARP_ANCHORS.put(color, Collections.newSetFromMap(new WeakHashMap<>()));
        COLORLESS_WARP_ANCHORS = WARP_ANCHORS.get(WarpAnchorBlock.Color.NONE);
    }

    /**
     * If an anchor is further than this threshold, it will be rendered as if at the threshold distance.
     */
    private static final float ENLARGE_THRESHOLD = 20, ENLARGE_NAME_THRESHOLD = 5;
    private static final List<BlockState> ANCHOR_STATES = new ArrayList<>();
    private static final IntList ANCHOR_TINTS = new IntArrayList();
    private static final List<BlockPos.MutableBlockPos> ANCHOR_POSITIONS = new ArrayList<>();
    private static final PoseStack POSE_STACK = new PoseStack();
    private static final Vector3f CAMERA_FORWARD = new Vector3f(), CAMERA_UP = new Vector3f();
    private static int targetedAnchorIndex;
    private static WarpAnchorBlock.Color targetedAnchorColor;
    private static String targetedAnchorName = "";

    public static void add(WarpAnchorBlockEntity anchor){
        WARP_ANCHORS.get(anchor.getColor()).add(anchor);
    }

    public static void remove(WarpAnchorBlockEntity anchor){
        WARP_ANCHORS.get(anchor.getColor()).remove(anchor);
    }

    private static @Nullable WarpAnchorBlockEntity getStandingOnAnchor(){
        Level level = ClientUtils.getWorld();
        Player player = ClientUtils.getPlayer();
        if(player == null || level == null)
            return null;
        if(!player.onGround())
            return null;
        BlockPos pos = player.getOnPos();
        if(level.getBlockState(pos).getBlock() != WarpAnchors.warpAnchor
            || !(level.getBlockEntity(pos) instanceof WarpAnchorBlockEntity anchor))
            return null;
        return anchor;
    }

    public static boolean tryTeleport(){
        WarpAnchorBlockEntity centerAnchor = getStandingOnAnchor();
        if(centerAnchor == null)
            return false;
        BlockPos centerPos = centerAnchor.getBlockPos();

        Iterable<WarpAnchorBlockEntity> anchors;
        if(centerAnchor.getColor() == WarpAnchorBlock.Color.NONE)
            //noinspection unchecked
            anchors = Iterables.concat(WARP_ANCHORS.values().toArray(Iterable[]::new));
        else
            anchors = Iterables.concat(COLORLESS_WARP_ANCHORS, WARP_ANCHORS.get(centerAnchor.getColor()));

        WarpAnchorBlockEntity targetAnchor = null;
        float targetedAnchorAngle = (float)Math.PI;
        Level level = ClientUtils.getWorld();
        Camera camera = ClientUtils.getMinecraft().gameRenderer.getMainCamera();
        Vector3fc cameraPosition = camera.getPosition().toVector3f();
        Vector3fc lookDirection = camera.getLookVector();
        Vector3f dummyVector = new Vector3f();
        int range = WarpAnchorsConfig.anchorRange.get();
        for(WarpAnchorBlockEntity anchor : anchors){
            if(anchor == centerAnchor || anchor.getLevel() != level)
                continue;
            BlockPos anchorPos = anchor.getBlockPos();
            if(anchorPos.distSqr(centerPos) > range * range + 0.1)
                continue;

            // Check if anchor is targeted
            float angle = lookDirection.angle(dummyVector.set(anchorPos.getX() + 0.5, anchorPos.getY() + 0.5, anchorPos.getZ() + 0.5).sub(cameraPosition));
            if(angle * Math.clamp(cameraPosition.distance(anchorPos.getX(), anchorPos.getY(), anchorPos.getZ()), 4, ENLARGE_THRESHOLD) < 1
                && angle < targetedAnchorAngle){
                targetAnchor = anchor;
                targetedAnchorAngle = angle;
            }
        }

        if(targetAnchor != null){
            WarpAnchors.CHANNEL.sendToServer(new WarpPacket(targetAnchor.getBlockPos(), centerAnchor.getBlockPos()));
            return true;
        }
        return false;
    }

    public static boolean extractRenderState(Camera camera){
        int anchors = extractAnchors(camera);
        for(int i = ANCHOR_STATES.size() - anchors; i > 0; i--){
            ANCHOR_STATES.removeLast();
            ANCHOR_TINTS.removeInt(ANCHOR_TINTS.size() - 1);
            ANCHOR_POSITIONS.removeLast();
        }
        return anchors > 0;
    }

    private static int extractAnchors(Camera camera){
        targetedAnchorIndex = -1;
        WarpAnchorBlockEntity centerAnchor = getStandingOnAnchor();
        if(centerAnchor == null)
            return 0;
        BlockPos centerPos = centerAnchor.getBlockPos();

        CAMERA_FORWARD.set(camera.getLookVector());
        CAMERA_UP.set(camera.getUpVector());
        Vector3fc cameraPosition = camera.getPosition().toVector3f();
        Vector3fc lookDirection = camera.getLookVector();

        Iterable<WarpAnchorBlockEntity> anchors;
        if(centerAnchor.getColor() == WarpAnchorBlock.Color.NONE)
            //noinspection unchecked
            anchors = Iterables.concat(WARP_ANCHORS.values().toArray(Iterable[]::new));
        else
            anchors = Iterables.concat(COLORLESS_WARP_ANCHORS, WARP_ANCHORS.get(centerAnchor.getColor()));

        int rendered = 0;
        float targetedAnchorAngle = (float)Math.PI;
        Level level = ClientUtils.getWorld();
        BlockColors blockColors = ClientUtils.getMinecraft().getBlockColors();
        Vector3f dummyVector = new Vector3f();
        int range = WarpAnchorsConfig.anchorRange.get();
        for(WarpAnchorBlockEntity anchor : anchors){
            if(anchor == centerAnchor || anchor.getLevel() != level)
                continue;
            BlockState anchorState = anchor.getBlockState();
            BlockPos anchorPos = anchor.getBlockPos();
            if(anchorPos.distSqr(centerPos) > range * range + 0.1)
                continue;

            // Extract model render state
            if(rendered >= ANCHOR_STATES.size()){
                ANCHOR_POSITIONS.add(new BlockPos.MutableBlockPos());
                ANCHOR_STATES.add(anchorState);
                ANCHOR_TINTS.add(0);
            }else
                ANCHOR_STATES.set(rendered, anchorState);
            ANCHOR_POSITIONS.get(rendered).set(anchorPos);
            ANCHOR_TINTS.set(rendered, blockColors.getColor(anchorState, level, anchorPos, 0));

            // Check if anchor is targeted
            float angle = lookDirection.angle(dummyVector.set(anchorPos.getX() + 0.5, anchorPos.getY() + 0.5, anchorPos.getZ() + 0.5).sub(cameraPosition));
            if(angle * Math.clamp(cameraPosition.distance(anchorPos.getX(), anchorPos.getY(), anchorPos.getZ()), 4, 20) < 1
                && angle < targetedAnchorAngle){
                targetedAnchorIndex = rendered;
                targetedAnchorColor = anchor.getColor();
                targetedAnchorAngle = angle;
                targetedAnchorName = anchor.getName();
            }
            rendered++;
        }
        return rendered;
    }

    public static boolean hasTargetedAnchor(){
        return targetedAnchorIndex >= 0;
    }

    public static void renderOutline(Vec3 cameraPos){
        if(!hasTargetedAnchor())
            return;

        Minecraft minecraft = ClientUtils.getMinecraft();
        OutlineBufferSource outlineBufferSource = minecraft.renderBuffers().outlineBufferSource();
        int color = targetedAnchorColor.getTint();
        outlineBufferSource.setColor(FastColor.ARGB32.red(color), FastColor.ARGB32.green(color), FastColor.ARGB32.blue(color), 255);
        BlockState state = ANCHOR_STATES.get(targetedAnchorIndex);
        int tint = ANCHOR_TINTS.getInt(targetedAnchorIndex);
        Vector3f renderPos = new Vector3f();
        getRenderPosition(ANCHOR_POSITIONS.get(targetedAnchorIndex), cameraPos, renderPos);
        POSE_STACK.pushPose();
        POSE_STACK.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        POSE_STACK.translate(renderPos.x, renderPos.y, renderPos.z);
        POSE_STACK.scale(1.1f, 1.1f, 1.1f);
        POSE_STACK.translate(-0.5f, -0.5f, -0.5f);
        minecraft.getBlockRenderer().getModelRenderer().renderModel(POSE_STACK.last(), outlineBufferSource.getBuffer(RenderType.outline(TextureAtlas.LOCATION_BLOCKS)), state, minecraft.getBlockRenderer().getBlockModel(state), FastColor.ARGB32.red(tint) / 255f, FastColor.ARGB32.green(tint) / 255f, FastColor.ARGB32.blue(tint) / 255f, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
        POSE_STACK.popPose();
    }

    public static void renderAlwaysOnTop(Vec3 cameraPos){
        if(ANCHOR_STATES.isEmpty())
            return;

        Minecraft minecraft = ClientUtils.getMinecraft();
        RenderSystem.clear(GL11.GL_DEPTH_BUFFER_BIT, Minecraft.ON_OSX);
        MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
        BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();
        POSE_STACK.pushPose();
        POSE_STACK.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Vector3f dummyVector = new Vector3f();
        for(int i = 0; i < ANCHOR_STATES.size(); i++){
            boolean isTargeted = targetedAnchorIndex == i;
            // Block
            BlockPos.MutableBlockPos pos = ANCHOR_POSITIONS.get(i);
            float distance = getRenderPosition(pos, cameraPos, dummyVector);
            BlockState state = ANCHOR_STATES.get(i);
            int tint = ANCHOR_TINTS.getInt(i);
            POSE_STACK.pushPose();
            POSE_STACK.translate(dummyVector.x, dummyVector.y, dummyVector.z);
            POSE_STACK.scale(1.1f, 1.1f, 1.1f);
            POSE_STACK.translate(-0.5f, -0.5f, -0.5f);
            blockRenderer.getModelRenderer().renderModel(POSE_STACK.last(), bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(state, false)), state, blockRenderer.getBlockModel(state), FastColor.ARGB32.red(tint) / 255f, FastColor.ARGB32.green(tint) / 255f, FastColor.ARGB32.blue(tint) / 255f, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY);
            POSE_STACK.popPose();
            // Name
            if(isTargeted && !targetedAnchorName.isEmpty()){
                float distanceScale = (1 - Math.min(distance, ENLARGE_THRESHOLD) / ENLARGE_THRESHOLD);
                distanceScale *= distanceScale;
                dummyVector.set(pos.getX() + 0.5f, pos.getY() + 0.5, pos.getZ() + 0.5f);
                if(distance > ENLARGE_NAME_THRESHOLD){
                    dummyVector.sub((float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z)
                        .mul(ENLARGE_NAME_THRESHOLD / distance)
                        .add((float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z);
                    CAMERA_UP.mulAdd(distanceScale * 1 + 0.4f, dummyVector, dummyVector);
                    CAMERA_FORWARD.mulAdd(-distanceScale * 1 + 0.4f, dummyVector, dummyVector);
                }else
                    dummyVector.add(CAMERA_UP);
                renderNameTag(Component.literal(targetedAnchorName), dummyVector, bufferSource);
            }
        }
        POSE_STACK.popPose();
        bufferSource.endBatch();
    }

    private static float getRenderPosition(BlockPos pos, Vec3 cameraPos, Vector3f output){
        output.set(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
        float distance = output.distance((float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z);
        if(distance > ENLARGE_THRESHOLD)
            output.sub((float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z)
                .mul(ENLARGE_THRESHOLD / distance)
                .add((float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z);
        return distance;
    }

    private static void renderNameTag(Component name, Vector3f position, MultiBufferSource bufferSource){
        Minecraft minecraft = ClientUtils.getMinecraft();
        POSE_STACK.pushPose();
        POSE_STACK.translate(position.x, position.y, position.z);
        POSE_STACK.mulPose(minecraft.gameRenderer.getMainCamera().rotation());
        POSE_STACK.scale(0.025f, -0.025f, 0.025f);
        Matrix4f matrix = POSE_STACK.last().pose();
        float x = -minecraft.font.width(name) / 2f;
        int backgroundColor = (int)(minecraft.options.getBackgroundOpacity(0.25f) * 255) << 24;
        minecraft.font.drawInBatch(name, x, 0, -2130706433, false, matrix, bufferSource, Font.DisplayMode.SEE_THROUGH, backgroundColor, LightTexture.FULL_BRIGHT);
        minecraft.font.drawInBatch(name, x, 0, -1, false, matrix, bufferSource, Font.DisplayMode.NORMAL, 0, LightTexture.FULL_BRIGHT);
        POSE_STACK.popPose();
    }
}
