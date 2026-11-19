package com.supermartijn642.warpanchors.content;

import com.google.common.collect.Iterables;
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
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

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
    public static FeatureRenderDispatcher FEATURE_RENDER_DISPATCHER;
    private static final List<BlockState> ANCHOR_STATES = new ArrayList<>();
    private static final IntList ANCHOR_TINTS = new IntArrayList();
    private static final List<BlockPos.MutableBlockPos> ANCHOR_POSITIONS = new ArrayList<>();
    private static final PoseStack POSE_STACK = new PoseStack();
    private static final Vector3f CAMERA_FORWARD = new Vector3f(), CAMERA_UP = new Vector3f();
    private static int targetedAnchorIndex;
    private static WarpAnchorBlock.Color targetedAnchorColor;
    private static String targetedAnchorName = "";
    public static boolean submittedAlwaysOnTopNodes = false;

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
        Vector3fc cameraPosition = camera.position().toVector3f();
        Vector3fc lookDirection = camera.forwardVector();
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
        WarpAnchorBlockEntity centerAnchor = getStandingOnAnchor();
        if(centerAnchor == null)
            return 0;
        BlockPos centerPos = centerAnchor.getBlockPos();

        CAMERA_FORWARD.set(camera.forwardVector());
        CAMERA_UP.set(camera.upVector());
        Vector3fc cameraPosition = camera.position().toVector3f();
        Vector3fc lookDirection = camera.forwardVector();

        Iterable<WarpAnchorBlockEntity> anchors;
        if(centerAnchor.getColor() == WarpAnchorBlock.Color.NONE)
            //noinspection unchecked
            anchors = Iterables.concat(WARP_ANCHORS.values().toArray(Iterable[]::new));
        else
            anchors = Iterables.concat(COLORLESS_WARP_ANCHORS, WARP_ANCHORS.get(centerAnchor.getColor()));

        targetedAnchorIndex = -1;
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

    public static void submitRenderState(CameraRenderState cameraRenderState){
        Minecraft minecraft = ClientUtils.getMinecraft();
        if(FEATURE_RENDER_DISPATCHER == null){
            RenderBuffers renderBuffers = minecraft.renderBuffers();
            FEATURE_RENDER_DISPATCHER = new FeatureRenderDispatcher(
                new SubmitNodeStorage(),
                minecraft.getBlockRenderer(),
                renderBuffers.bufferSource(),
                minecraft.getAtlasManager(),
                renderBuffers.outlineBufferSource(),
                renderBuffers.crumblingBufferSource(),
                minecraft.font
            );
        }
        SubmitNodeStorage output = FEATURE_RENDER_DISPATCHER.getSubmitNodeStorage();

        if(ANCHOR_STATES.isEmpty()){
            submittedAlwaysOnTopNodes = false;
            return;
        }
        submittedAlwaysOnTopNodes = true;

        BlockRenderDispatcher blockRenderer = minecraft.getBlockRenderer();
        Vec3 cameraPos = cameraRenderState.pos;
        POSE_STACK.pushPose();
        POSE_STACK.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Vector3f dummyVector = new Vector3f();
        for(int i = 0; i < ANCHOR_STATES.size(); i++){
            boolean isTargeted = targetedAnchorIndex == i;
            // Block
            BlockPos.MutableBlockPos pos = ANCHOR_POSITIONS.get(i);
            dummyVector.set(pos.getX() + 0.5f, pos.getY() + 0.5f, pos.getZ() + 0.5f);
            float distance = dummyVector.distance((float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z);
            if(distance > ENLARGE_THRESHOLD)
                dummyVector.sub((float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z)
                    .mul(ENLARGE_THRESHOLD / distance)
                    .add((float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z);
            BlockState state = ANCHOR_STATES.get(i);
            int tint = ANCHOR_TINTS.getInt(i);
            POSE_STACK.pushPose();
            POSE_STACK.translate(dummyVector.x, dummyVector.y, dummyVector.z);
            POSE_STACK.scale(1.1f, 1.1f, 1.1f);
            POSE_STACK.translate(-0.5f, -0.5f, -0.5f);
            output.submitBlockModel(POSE_STACK, ItemBlockRenderTypes.getRenderType(state), blockRenderer.getBlockModel(state), ARGB.redFloat(tint), ARGB.greenFloat(tint), ARGB.blueFloat(tint), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, isTargeted ? targetedAnchorColor.getTint() : 0);
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
                output.submitNameTag(POSE_STACK, new Vec3(dummyVector.sub(0, 0.5f, 0)), 0, Component.literal(targetedAnchorName), true, LightTexture.FULL_BRIGHT, dummyVector.distanceSquared((float)cameraPos.x, (float)cameraPos.y, (float)cameraPos.z), cameraRenderState);
            }
        }
        POSE_STACK.popPose();
    }
}
