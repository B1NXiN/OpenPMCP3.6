package pmcp.mode.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4d;
import pmcp.event.EventTarget;
import pmcp.event.mode.render.EventRender2D;
import pmcp.event.mode.render.EventRender3D;
import pmcp.mode.Mode;
import pmcp.mode.client.ClientSetting;
import pmcp.utils.category.Category;
import pmcp.utils.player.EntityUtils;
import pmcp.utils.player.FriendManager;
import pmcp.utils.render.RenderUtils;
import pmcp.value.values.BooleanValue;
import pmcp.value.values.ModeValue;

import java.awt.*;

public class ESP extends Mode {

    private final BooleanValue<Boolean> player = new BooleanValue<>(this, "Player", true);
    private final ModeValue<playerMode> playerModeValue = new ModeValue<>(this, "PlayerMode", playerMode.values(), playerMode.HitBox);

    enum playerMode {
        HitBox,
        Box2D,
        Outline,
        Orbital
    }


    private final BooleanValue<Boolean> item = new BooleanValue<>(this, "Item", true);
    private final BooleanValue<Boolean> backGround = new BooleanValue<>(this, "BackGround", true, item::getValue);
    private final BooleanValue<Boolean> s = new BooleanValue<>(this, "???", true);

    public ESP() {
        super("ESP", "透视", "能让你透视看见你想看见的东西", Category.RENDER);
    }

    @EventTarget
    private void onRender3D(EventRender3D event) {
        if (mc.level == null) return;

        setSuffix(playerModeValue.getValue().toString());


        for (Entity entity : mc.level.entitiesForRendering()) {
            if (item.getValue() && entity instanceof ItemEntity entity1) {
                ItemStack itemStack = entity1.getItem();

                String displayName = itemStack.getHoverName().getString();
                int count = itemStack.getCount();
                String label = count > 1 ? displayName + " x" + count : displayName;

                double centerY = 0.0;

                double partialTicks = mc.getFrameTime();

                if (itemStack.getItem() instanceof SwordItem) {
                    centerY += 1.2;
                } else if (itemStack.getItem() instanceof BlockItem) {
                    centerY += 0.10;
                }

                RenderUtils.renderNameTag(
                        event.getPoseStack(),
                        label,
                        entity.xOld + (entity.getX() - entity.xOld) * partialTicks,
                        entity.yOld + (entity.getY() - entity.yOld) * partialTicks + centerY,
                        entity.zOld + (entity.getZ() - entity.zOld) * partialTicks,
                        backGround.getValue(),
                        getColor(itemStack)
                );
            }

            if (player.getValue() && (entity instanceof Player player && EntityUtils.isValidTarget(player, mc))) {

                Color c = new Color(255, 255, 255, 50);

                if (FriendManager.isFriend(player.getName().getString())) {
                    c = new Color(0, 255, 255, 50);
                }

                Color color = player.hurtTime > 0 ? new Color(255, 0, 0, 50) : c;

                switch (playerModeValue.getValue()) {
                    case HitBox -> RenderUtils.drawEntityBox3D(event.getPoseStack(), player, color);
                    case Box2D -> RenderUtils.renderEntity2D(event.getPoseStack(), player, Color.WHITE.getRGB(), s.getValue());
                    case Outline -> {
                        renderPlayerDetailedOutline(event.getPoseStack(), (Player) entity, Color.WHITE.getRGB(), 2, s.getValue());
                        TargetMarker.Instance.drawTextureOnEntity(event.getPoseStack(), -24, 0, 48, 48, 48, 48, entity, new ResourceLocation("pmcp/png/cwh.png"), true, Color.WHITE, Color.WHITE, Color.WHITE, Color.WHITE);
                    }
                    case Orbital -> renderEntityOrbitalOutline(
                            event.getPoseStack(),
                            entity,
                            Color.WHITE.getRGB(),
                            2, // 使用更粗的线条
                            2,
                            2f,
                            true // 总是穿墙
                    );
                }
            }
        }
    }

    private static Color getColor(ItemStack itemStack) {
        Color color = Color.WHITE;

        if (itemStack.getItem() == Items.DIAMOND) {
            color = new Color(0, 255, 255, 255);
        } else if (itemStack.getItem() == Items.GOLD_INGOT) {
            color = new Color(255, 255, 0, 255);
        } else if (itemStack.getItem() == Items.IRON_INGOT) {
            color = new Color(150, 150, 150, 255);
        } else if (itemStack.getItem() == Items.EMERALD) {
            color = new Color(0, 255, 0, 255);
        }
        return color;
    }

    public void renderPlayerDetailedOutline(
            PoseStack poseStack,
            Player player,
            int color,
            float lineWidth,
            boolean throughWalls
    ) {
        if (player == null) return;

        // 获取相对于摄像机的实体位置
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        Camera camera = renderManager.camera;
        double partialTicks = mc.getFrameTime();

        // 计算插值位置
        double x = player.xOld + (player.getX() - player.xOld) * partialTicks - camera.getPosition().x();
        double y = player.yOld + (player.getY() - player.yOld) * partialTicks - camera.getPosition().y();
        double z = player.zOld + (player.getZ() - player.zOld) * partialTicks - camera.getPosition().z();

        // 提取颜色分量
        float a = (color >> 24 & 0xFF) / 255f;
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >> 8 & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // 检查玩家受伤状态以调整颜色
        if (player.hurtTime > 0) {
            float hurtProgress = (float) player.hurtTime / 10.0f;
            g *= (1.0f - hurtProgress * 0.8f);
            b *= (1.0f - hurtProgress * 0.8f);
            r = Math.min(1.0f, r + (1.0f - r) * hurtProgress * 0.8f);
        }

        // 配置渲染状态
        poseStack.pushPose();
        poseStack.translate(x, y, z);

        // 应用玩家旋转 - 转换为弧度并绕Y轴旋转
        float yaw = (float) Math.toRadians(-player.getYRot());
        poseStack.mulPose(new Quaternionf().rotationY(yaw));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(lineWidth);

        // 如果需要穿墙渲染，禁用深度测试
        boolean depthWasEnabled = false;
        if (throughWalls) {
            depthWasEnabled = true;
            RenderSystem.disableDepthTest();
        }

        // 绘制玩家模型轮廓
        drawPlayerModelOutline(poseStack, r, g, b, a, player.isCrouching());

        // 恢复原始状态
        if (throughWalls && depthWasEnabled) {
            RenderSystem.enableDepthTest();
        }
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();
        poseStack.popPose();
    }
    private void drawPlayerModelOutline(
            PoseStack poseStack,
            float r, float g, float b, float a,
            boolean isCrouching
    ) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        buffer.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);

        // Define player body points
        Vector3f head = new Vector3f(0, isCrouching ? 1.5f : 1.62f, 0);
        Vector3f neck = new Vector3f(0, isCrouching ? 1.35f : 1.42f, 0);
        Vector3f body = new Vector3f(0, isCrouching ? 0.95f : 0.85f, 0);
        Vector3f leftShoulder = new Vector3f(0.18f, isCrouching ? 1.35f : 1.42f, 0);
        Vector3f rightShoulder = new Vector3f(-0.18f, isCrouching ? 1.35f : 1.42f, 0);
        Vector3f leftElbow = new Vector3f(0.28f, isCrouching ? 1.15f : 1.2f, 0.05f);
        Vector3f rightElbow = new Vector3f(-0.28f, isCrouching ? 1.15f : 1.2f, 0.05f);
        Vector3f leftHand = new Vector3f(0.35f, isCrouching ? 0.95f : 0.95f, 0.1f);
        Vector3f rightHand = new Vector3f(-0.35f, isCrouching ? 0.95f : 0.95f, 0.1f);
        Vector3f pelvis = new Vector3f(0, isCrouching ? 0.75f : 0.75f, 0);
        Vector3f leftHip = new Vector3f(0.1f, isCrouching ? 0.75f : 0.75f, 0);
        Vector3f rightHip = new Vector3f(-0.1f, isCrouching ? 0.75f : 0.75f, 0);
        Vector3f leftKnee = new Vector3f(0.13f, isCrouching ? 0.5f : 0.4f, 0.05f);
        Vector3f rightKnee = new Vector3f(-0.13f, isCrouching ? 0.5f : 0.4f, 0.05f);
        Vector3f leftFoot = new Vector3f(0.13f, isCrouching ? 0.15f : 0.05f, 0.1f);
        Vector3f rightFoot = new Vector3f(-0.13f, isCrouching ? 0.15f : 0.05f, 0.1f);

        // Head outline
        drawCircleFilled(buffer, matrix, head, 0.18f, 0.18f, 0.18f, r, g, b, a, 0.8f);

        // Connect head to body
        buffer.vertex(matrix, head.x(), head.y() - 0.18f, head.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, neck.x(), neck.y(), neck.z()).color(r, g, b, a).endVertex();

        // Draw spine
        buffer.vertex(matrix, neck.x(), neck.y(), neck.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, body.x(), body.y(), body.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, body.x(), body.y(), body.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, pelvis.x(), pelvis.y(), pelvis.z()).color(r, g, b, a).endVertex();

        // Draw shoulders
        buffer.vertex(matrix, neck.x(), neck.y(), neck.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, leftShoulder.x(), leftShoulder.y(), leftShoulder.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, neck.x(), neck.y(), neck.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, rightShoulder.x(), rightShoulder.y(), rightShoulder.z()).color(r, g, b, a).endVertex();

        // Draw arms
        buffer.vertex(matrix, leftShoulder.x(), leftShoulder.y(), leftShoulder.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, leftElbow.x(), leftElbow.y(), leftElbow.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, leftElbow.x(), leftElbow.y(), leftElbow.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, leftHand.x(), leftHand.y(), leftHand.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, rightShoulder.x(), rightShoulder.y(), rightShoulder.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, rightElbow.x(), rightElbow.y(), rightElbow.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, rightElbow.x(), rightElbow.y(), rightElbow.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, rightHand.x(), rightHand.y(), rightHand.z()).color(r, g, b, a).endVertex();

        // Draw hips
        buffer.vertex(matrix, pelvis.x(), pelvis.y(), pelvis.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, leftHip.x(), leftHip.y(), leftHip.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, pelvis.x(), pelvis.y(), pelvis.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, rightHip.x(), rightHip.y(), rightHip.z()).color(r, g, b, a).endVertex();

        // Draw legs
        buffer.vertex(matrix, leftHip.x(), leftHip.y(), leftHip.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, leftKnee.x(), leftKnee.y(), leftKnee.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, leftKnee.x(), leftKnee.y(), leftKnee.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, leftFoot.x(), leftFoot.y(), leftFoot.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, rightHip.x(), rightHip.y(), rightHip.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, rightKnee.x(), rightKnee.y(), rightKnee.z()).color(r, g, b, a).endVertex();

        buffer.vertex(matrix, rightKnee.x(), rightKnee.y(), rightKnee.z()).color(r, g, b, a).endVertex();
        buffer.vertex(matrix, rightFoot.x(), rightFoot.y(), rightFoot.z()).color(r, g, b, a).endVertex();

        BufferUploader.drawWithShader(buffer.end());
    }
    private void drawCircleFilled(
            BufferBuilder buffer,
            Matrix4f matrix,
            Vector3f center,
            float radiusX,
            float radiusY,
            float radiusZ,
            float r, float g, float b, float a,
            float opacity
    ) {
        // Draw circle outline
        int segments = 16;
        float angleDelta = (float) ((2 * Math.PI) / segments);

        Vector3f prev = null;
        for (int i = 0; i <= segments; i++) {
            float angle = i * angleDelta;
            float x = (float) (center.x() + Math.cos(angle) * radiusX);
            float z = (float) (center.z() + Math.sin(angle) * radiusZ);
            Vector3f current = new Vector3f(x, center.y(), z);

            if (prev != null) {
                buffer.vertex(matrix, prev.x(), prev.y(), prev.z()).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, current.x(), current.y(), current.z()).color(r, g, b, a).endVertex();
            }

            prev = current;
        }

        // Draw vertical circle
        prev = null;
        for (int i = 0; i <= segments; i++) {
            float angle = i * angleDelta;
            float x = (float) (center.x() + Math.cos(angle) * radiusX);
            float y = (float) (center.y() + Math.sin(angle) * radiusY);
            Vector3f current = new Vector3f(x, y, center.z());

            if (prev != null) {
                buffer.vertex(matrix, prev.x(), prev.y(), prev.z()).color(r, g, b, a).endVertex();
                buffer.vertex(matrix, current.x(), current.y(), current.z()).color(r, g, b, a).endVertex();
            }

            prev = current;
        }
    }

    public void renderEntityOrbitalOutline(
            PoseStack poseStack,
            Entity entity,
            int color,
            float lineWidth,
            int orbitCount,
            float orbitRadius,
            boolean throughWalls
    ) {
        if (entity == null) return;

        // 获取相对于摄像机的实体位置
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        Camera camera = renderManager.camera;
        double partialTicks = mc.getFrameTime();

        // 计算插值位置
        double x = entity.xOld + (entity.getX() - entity.xOld) * partialTicks - camera.getPosition().x();
        double y = entity.yOld + (entity.getY() - entity.yOld) * partialTicks - camera.getPosition().y();
        double z = entity.zOld + (entity.getZ() - entity.zOld) * partialTicks - camera.getPosition().z();

        // 获取实体边界框大小
        AABB bb = entity.getBoundingBox();
        float entityWidth = (float)(bb.maxX - bb.minX) / 2.0f;
        float entityHeight = (float)(bb.maxY - bb.minY);



        // 计算轨道半径
        float radius = entityWidth * orbitRadius;

        // 提取颜色分量
        float a = (color >> 24 & 0xFF) / 255f;
        float r = (color >> 16 & 0xFF) / 255f;
        float g = (color >> 8 & 0xFF) / 255f;
        float b = (color & 0xFF) / 255f;

        // 配置渲染状态
        poseStack.pushPose();
        poseStack.translate(x, y + entityHeight / 2, z);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(lineWidth);

        // 如果需要穿墙渲染，禁用深度测试
        boolean depthWasEnabled = false;
        if (throughWalls) {
            depthWasEnabled = true;
            RenderSystem.disableDepthTest();
        }

        // 获取当前时间以产生动画效果
        float time = (System.currentTimeMillis() % 10000) / 1000.0f;

        // 绘制多条轨道
        for (int i = 0; i < orbitCount; i++) {
            float orbitPhase = (float) i / orbitCount * (float) Math.PI * 2.0f;
            float colorOffset = (float) i / orbitCount;

            // 每条轨道的颜色略有变化
            float orbitR = r;
            float orbitG = g;
            float orbitB = b;

            // 根据实体状态调整轨道颜色
            if (entity instanceof LivingEntity living) {
                if (living.hurtTime > 0) {
                    float hurtProgress = (float) living.hurtTime / 10.0f;
                    orbitG *= (1.0f - hurtProgress * 0.8f);
                    orbitB *= (1.0f - hurtProgress * 0.8f);
                    orbitR = Math.min(1.0f, orbitR + (1.0f - orbitR) * hurtProgress * 0.8f);
                }
            }

            // 绘制轨道
            drawOrbit(poseStack, radius, orbitPhase, time, orbitR, orbitG, orbitB, a);
        }

        // 恢复原始状态
        if (throughWalls && depthWasEnabled) {
            RenderSystem.enableDepthTest();
        }
        RenderSystem.lineWidth(1.0f);
        RenderSystem.disableBlend();
        poseStack.popPose();
    }

    private static void drawOrbit(
            PoseStack poseStack,
            float radius,
            float phase,
            float time,
            float r, float g, float b, float a
    ) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        buffer.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        // 旋转速度
        float rotationSpeed = 0.5f;

        // 波浪效果参数
        float waveAmplitude = radius * 0.15f;
        float waveFrequency = 3.0f;

        // 旋转轨道
        poseStack.pushPose();
        poseStack.mulPose(new Quaternionf().rotationY(time * rotationSpeed + phase));

        // 绘制轨道
        int segments = 48; // 增加线段数使轨道更平滑
        for (int i = 0; i <= segments; i++) {
            float angle = (float) i / segments * (float) Math.PI * 2.0f;

            // 计算波浪效果
            float yOffset = waveAmplitude * (float) Math.sin(angle * waveFrequency + time * 2.0f + phase);

            // 轨道位置
            float x = (float) (radius * Math.cos(angle));
            float y = yOffset;
            float z = (float) (radius * Math.sin(angle));

            // 颜色可以根据角度变化
            float segmentOffset = (float) i / segments;
            float alpha = (float) (a * (0.7f + 0.3f * Math.sin(segmentOffset * Math.PI * 2 + time * 3)));

            buffer.vertex(matrix, x, y, z).color(r, g, b, alpha).endVertex();
        }

        BufferUploader.drawWithShader(buffer.end());
        poseStack.popPose();
    }
}
