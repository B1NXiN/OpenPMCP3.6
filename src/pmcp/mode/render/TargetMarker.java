package pmcp.mode.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;
import pmcp.event.EventTarget;
import pmcp.event.mode.render.EventRender3D;
import pmcp.mode.Mode;
import pmcp.mode.combat.KillAura;
import pmcp.utils.category.Category;
import pmcp.utils.player.EntityUtils;
import pmcp.utils.render.ColorUtils;
import pmcp.utils.render.RenderUtils;
import pmcp.value.values.BooleanValue;
import pmcp.value.values.ModeValue;
import pmcp.value.values.NumberValue;

import java.awt.*;

public class TargetMarker extends Mode {

    public static TargetMarker Instance;

    private final ModeValue<mode> modeValue = new ModeValue<>(this, "Mode", mode.values(), mode.Nursultan);

    enum mode {
        Nursultan,
        Jello
    }

    private final BooleanValue<Boolean> onlyKillAura = new BooleanValue<>(this, "Only KillAura", false);

    private final NumberValue<Number> range = new NumberValue<>(this, "Range", 6.0,0.0,8.0,0.1);

    public TargetMarker() {
        super("TargetMarker", "目标指示器", "对正在攻击的目标进行渲染", Category.RENDER);
        Instance = this;
    }

    private float circleStep = 0;

    @EventTarget
    private void onRender3D(EventRender3D eventRender3D) {
        if (mc.player == null || mc.level == null) return;

        PoseStack poseStack = eventRender3D.getPoseStack();
        Color mainColor = Color.WHITE;
        Color secondColor = Color.BLACK;

        if (onlyKillAura.getValue()) {
            final Entity entity = KillAura.target;
            if (entity != null) {
                poseStack.pushPose();
                switch (modeValue.getValue()) {
                    case Nursultan -> drawTextureOnEntity(poseStack, -24, -24, 48, 48, 48, 48, entity, new ResourceLocation("pmcp/png/target.png"), true, mainColor, mainColor, secondColor, secondColor);
                    case Jello -> {
                        drawJello(poseStack, entity, 1, circleStep);
                        circleStep += 0.08f;
                    }
                }
                poseStack.popPose();
            }
        } else {
            for (Entity entity : mc.level.entitiesForRendering()) {
                if (entity instanceof Player player) {
                    if (EntityUtils.isValidTarget(player, mc) && mc.player.distanceTo(entity) <= range.getValue().floatValue()) {
                        poseStack.pushPose();
                        switch (modeValue.getValue()) {
                            case Nursultan -> drawTextureOnEntity(poseStack, -24, -24, 48, 48, 48, 48, entity, new ResourceLocation("pmcp/png/target.png"), true, mainColor, mainColor, secondColor, secondColor);
                            case Jello -> drawJello(poseStack, entity, 1, circleStep);
                        }
                        poseStack.popPose();
                    }
                }
            }
            if (modeValue.getValue().equals(mode.Jello)) circleStep += 0.08f;
        }
    }

    public void drawTextureOnEntity(PoseStack poseStack, int xPos, int yPos, int width, int height, float textureWidth, float textureHeight, Entity entity, ResourceLocation texture, boolean rotate, Color c, Color c1, Color c2, Color c3) {
        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();
        org.joml.Quaternionf cameraRotation = renderManager.camera.rotation();

        double x = entity.xOld + (entity.getX() - entity.xOld) * mc.getFrameTime() - renderManager.camera.getPosition().x();
        double y = (entity.yOld + 1) + ((entity.getY() + 1) - (entity.yOld + 1)) * mc.getFrameTime() - renderManager.camera.getPosition().y();
        double z = entity.zOld + (entity.getZ() - entity.zOld) * mc.getFrameTime() - renderManager.camera.getPosition().z();

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(cameraRotation);
        if (rotate) {
            // 正确的 1.20.1 旋转方式
            float angle = (float) (Math.sin(System.currentTimeMillis() / 800.0) * 360);
            // 创建四元数实例并应用旋转
            poseStack.mulPose(new Quaternionf().rotationAxis((float)Math.toRadians(angle), 0, 0, 1));
        }
        poseStack.scale(0.03F, 0.03F, 0.03F);

        // 设置渲染状态
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);

        // 绘制主体
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        RenderUtils.blitTextureWithColor(poseStack, xPos, yPos, 0, 0, width, height, textureWidth, textureHeight, c, c1, c2, c3);

        // 重置渲染状态
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    public void drawJello(PoseStack matrix, Entity target, float delta, float step) {
        Camera camera = mc.getEntityRenderDispatcher().camera;
        Vec3 cameraPos = camera.getPosition();
        double prevSinAnim = Math.abs(1 + Math.sin((double) step - 0.45f)) / 2;
        double sinAnim = Math.abs(1 + Math.sin(step)) / 2;
        double x = target.xo + (target.getX() - target.xo) * delta - cameraPos.x();
        double y = target.yo + (target.getY() - target.yo) * delta - cameraPos.y() + prevSinAnim * target.getBbHeight();
        double z = target.zo + (target.getZ() - target.zo) * delta - cameraPos.z();
        double nextY = target.yo + (target.getY() - target.yo) * delta - cameraPos.y() + sinAnim * target.getBbHeight();

        matrix.pushPose();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        float cos;
        float sin;
        int count = 1;
        Color color;
        for (int i = 0; i <= 360; i += 8) {
            if (count % 2 == 0) {
                color = Color.WHITE;
            } else {
                color = Color.CYAN;
            }
            cos = (float) (x + Math.cos(i * 6.28 / 360) * ((target.getBoundingBox().maxX - target.getBoundingBox().minX) + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ)) * 0.5f);
            sin = (float) (z + Math.sin(i * 6.28 / 360) * ((target.getBoundingBox().maxX - target.getBoundingBox().minX) + (target.getBoundingBox().maxZ - target.getBoundingBox().minZ)) * 0.5f);
            bufferBuilder.vertex(matrix.last().pose(), cos, (float) nextY, sin).color(color.getRGB()).endVertex();
            bufferBuilder.vertex(matrix.last().pose(), cos, (float) y, sin).color(ColorUtils.applyOpacity(color, 0).getRGB()).endVertex();
            ++count;
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
        RenderSystem.enableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();
        matrix.popPose();
    }
}
