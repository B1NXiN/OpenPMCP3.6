package pmcp.utils.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.*;
import pmcp.mode.client.HUDSetting;
import pmcp.ui.font.TrueTypeFont;
import pmcp.utils.client.MinecraftInstance;

import java.awt.*;
import java.io.IOException;
import java.lang.Math;
import java.util.Optional;

import static com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION_COLOR;

public class RenderUtils extends MinecraftInstance {

    public static LivingEntity livingEntity;

    public static MultiBufferSource.BufferSource bufferSource;

    public static final Matrix4f lastProjMat = new Matrix4f();
    public static final Matrix4f lastModMat = new Matrix4f();
    public static final Matrix4f lastWorldSpaceMatrix = new Matrix4f();


    public static void drawRect(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.fill(x, y, x + width, y + height,color);
    }

    public static void drawRect(PoseStack matrixStack, int pMinX, int pMinY, int pMaxX, int pMaxY, VertexConsumer vertexconsumer, int pColor) {
        Matrix4f matrix4f = matrixStack.last().pose();

        float f3 = (float) FastColor.ARGB32.alpha(pColor) / 255.0F;
        float f = (float)FastColor.ARGB32.red(pColor) / 255.0F;
        float f1 = (float)FastColor.ARGB32.green(pColor) / 255.0F;
        float f2 = (float)FastColor.ARGB32.blue(pColor) / 255.0F;
        vertexconsumer.vertex(matrix4f, (float)pMinX, (float)pMinY, (float) 0).color(f, f1, f2, f3).endVertex();
        vertexconsumer.vertex(matrix4f, (float)pMinX, (float)pMaxY, (float) 0).color(f, f1, f2, f3).endVertex();
        vertexconsumer.vertex(matrix4f, (float)pMaxX, (float)pMaxY, (float) 0).color(f, f1, f2, f3).endVertex();
        vertexconsumer.vertex(matrix4f, (float)pMaxX, (float)pMinY, (float) 0).color(f, f1, f2, f3).endVertex();
    }

    public static void drawRoundedOutline(PoseStack pose, float x, float y, float width, float height,
                                          float radius, float outlineWidth, Color outlineColor, Color fillColor) {
        // 外框
        drawRoundedRect(pose, x, y, width, height, radius, outlineColor);
        // 内层
        drawRoundedRect(pose, x + outlineWidth, y + outlineWidth,
                width - outlineWidth * 2, height - outlineWidth * 2,
                radius - outlineWidth, fillColor);
    }

    public static void drawRoundedOutline(PoseStack pose, float x, float y, float width, float height,
                                          float radius, float outlineWidth, Color color) {
        // 外框
        drawRoundedRect(pose, x, y, width, height, radius, color);
        // 内层空出 outlineWidth，颜色随需求可设置成透明或者背景色
        // 这里我默认用透明清除，如果你有底色，就传底色
        Color transparent = new Color(0, 0, 0, 0);
        drawRoundedRect(pose, x + outlineWidth, y + outlineWidth,
                width - outlineWidth * 2, height - outlineWidth * 2,
                radius - outlineWidth, transparent);
    }

    public static void drawImage(GuiGraphics guiGraphics, ResourceLocation image, int x, int y, int width, int height) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, image);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        guiGraphics.blit(image,x, y, width, height, 0, 0, width, height, width, height);
    }

    public static void drawRectText(Font font, String text , int x, int y, int width, int height, int backColor, int textColor, GuiGraphics guiGraphics) {
        drawRect(guiGraphics,x,y,width,height, backColor);

        guiGraphics.drawString(font, text, (x + (width / 2)) - (font.width(text) / 2), (y + (height / 2)) - (font.lineHeight / 2), textColor);
    }

    public static void drawRectText(TrueTypeFont font, String text , int x, int y, int width, int height, Color backColor, int textColor, GuiGraphics guiGraphics) {
        drawRoundedRect(guiGraphics.pose(),x,y,width,height,0, backColor);

        font.drawString(guiGraphics.pose(), text, (x + (width / 2)) - (font.getStringWidth(text) / 2), (y + (height / 2)) - (font.getHeight() / 2), textColor);
    }


    private static void setColor(Color color, float alpha) {
        RenderSystem.setShaderColor(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, color.getAlpha() != 255 ? color.getAlpha() / 255.0F : alpha);
    }

    public static void drawBlockBox(PoseStack poseStack, final BlockPos blockPos, final Color color) {
        if (blockPos == null || mc.level == null) return;

        var camera = mc.getEntityRenderDispatcher().camera;
        final double x = blockPos.getX() - camera.getPosition().x();
        final double y = blockPos.getY() - camera.getPosition().y();
        final double z = blockPos.getZ() - camera.getPosition().z();

        AABB axisAlignedBB = new AABB(x, y, z, x + 1, y + 1, z + 1);

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        setColor(color, color.getAlpha());
        drawFilledBox(poseStack, axisAlignedBB,VertexFormat.Mode.QUADS);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
    public static void drawBlockBox(PoseStack poseStack, final BlockPos blockPos, final double xE, final double yE, final double zE, final Color color) {
        if (blockPos == null || mc.level == null) return;

        var camera = mc.getEntityRenderDispatcher().camera;
        final double x = blockPos.getX() - camera.getPosition().x();
        final double y = blockPos.getY() - camera.getPosition().y();
        final double z = blockPos.getZ() - camera.getPosition().z();

        AABB axisAlignedBB = new AABB(x, y, z, x + xE, y + yE, z + zE);

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        setColor(color, color.getAlpha());
        drawFilledBox(poseStack, axisAlignedBB,VertexFormat.Mode.QUADS);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }

    public static void drawEntityBox3D(PoseStack matrixStack, Entity entity, Color color) {
        if (color.equals(new Color(0, 0, 0))) {
            return;
        }

        EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();

        double partialTicks = mc.getFrameTime();
        Vec3 cameraPos = renderManager.camera.getPosition();

        double x = entity.xOld + (entity.getX() - entity.xOld) * partialTicks - cameraPos.x();
        double y = entity.yOld + (entity.getY() - entity.yOld) * partialTicks - cameraPos.y();
        double z = entity.zOld + (entity.getZ() - entity.zOld) * partialTicks - cameraPos.z();

        AABB axisAlignedBB = entity.getBoundingBox().move(-entity.getX() + x, -entity.getY() + y, -entity.getZ() + z)
                .inflate(0.05D, 0.15D, 0.05D);

        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);

        setColor(color, color.getAlpha());
        drawFilledBox(matrixStack, axisAlignedBB,VertexFormat.Mode.QUADS);

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.depthMask(true);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }


    public static void renderNameTag(PoseStack poseStack, String text, double x, double y, double z, boolean back, Color color) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        Camera camera = mc.gameRenderer.getMainCamera();

        poseStack.pushPose();

        poseStack.translate(x - camera.getPosition().x, y - camera.getPosition().y, z - camera.getPosition().z);
        poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
        poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        poseStack.scale(-0.025f, -0.025f, 0.025f);

        int width = font.width(text) / 2;
        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();

        if (back) {
            font.drawInBatch(
                    text,
                    -width, 0,
                    0,
                    false,
                    matrix,
                    bufferSource,
                    Font.DisplayMode.SEE_THROUGH,
                    HUDSetting.Instance.getColor().getRGB(),
                    15728880
            );
        }
        font.drawInBatch(
                text,
                -width, 0,
                color.getRGB(),
                false,
                matrix,
                bufferSource,
                Font.DisplayMode.SEE_THROUGH,
                0,
                15728880
        );

        bufferSource.endBatch();

        RenderSystem.disableBlend();

        poseStack.popPose();
    }




    private static void drawFilledBox(PoseStack poseStack, AABB box, VertexFormat.Mode mode) {
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(mode, DefaultVertexFormat.POSITION_COLOR);

        Matrix4f matrix = poseStack.last().pose();

        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();

        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();

        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();

        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();

        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();

        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(1.0F, 1.0F, 1.0F, 1.0F).endVertex();

        tesselator.end();
    }

    public static void renderEntity(GuiGraphics pGuiGraphics, Level level, int pX, int pY, int pScale, LivingEntity pEntity) {
        renderEntity(pGuiGraphics, level, pX, pY, pScale, pEntity.getYRot(), pEntity.getXRot(), pEntity);
    }

    public static void renderEntity(GuiGraphics pGuiGraphics, Level level, int pX, int pY, int pScale, float yaw, float pitch, LivingEntity pEntity) {
        Quaternionf quaternionf = (new Quaternionf()).rotateZ((float)Math.PI);
        Quaternionf quaternionf1 = (new Quaternionf()).rotateX(20.0F * ((float)Math.PI / 180F));
        quaternionf.mul(quaternionf1);
        float f2 = pEntity.yBodyRot;
        float f3 = pEntity.getYRot();
        float f4 = pEntity.getXRot();
        float f5 = pEntity.yHeadRotO;
        float f6 = pEntity.yHeadRot;
        pEntity.yBodyRot = yaw;
        pEntity.setYRot(yaw);
        pEntity.setXRot(pitch);
        pEntity.yHeadRot = pEntity.getYRot();
        pEntity.yHeadRotO = pEntity.getYRot();
        livingEntity = pEntity;
        InventoryScreen.renderEntityInInventory(pGuiGraphics, pX, pY, pScale, quaternionf, quaternionf1, pEntity);
        livingEntity = null;
        pEntity.yBodyRot = f2;
        pEntity.setYRot(f3);
        pEntity.setXRot(f4);
        pEntity.yHeadRotO = f5;
        pEntity.yHeadRot = f6;
    }

    public static void drawItem(GuiGraphics guiGraphics, ItemStack stack, Entity pEntity, int x, int y, int r) {
        RenderUtils.drawItem(guiGraphics.pose(), guiGraphics.bufferSource(), stack, pEntity, x, y, r);
    }

    public static void drawItem(PoseStack pPoseStack, MultiBufferSource bufferSource, ItemStack stack, Entity pEntity, int x, int y, int r) {
        BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(stack, pEntity.level(), (LivingEntity) null, pEntity.getId());

        pPoseStack.pushPose();
        pPoseStack.translate((float)(x + r / 2), (float)(y + r / 2), (float)150);
        pPoseStack.mulPoseMatrix((new Matrix4f()).scaling(1.0F, -1.0F, 1.0F));
        pPoseStack.scale(r, r, r);
        Minecraft.getInstance().getItemRenderer().render(stack, ItemDisplayContext.GUI, false, pPoseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, bakedmodel);
        pPoseStack.popPose();
    }

    public static double deltaTime() {
        return Minecraft.getInstance().getFps() > 0 ? (1.0000 / Minecraft.getInstance().getFps()) : 1;
    }


    public static void renderEntity2D(PoseStack poseStack, Entity entity, int color, boolean damage) {
        if (entity instanceof LivingEntity living) {
            EntityRenderDispatcher renderManager = mc.getEntityRenderDispatcher();

            float pt = mc.getFrameTime();
            double x = entity.xOld + (entity.getX() - entity.xOld) * pt - renderManager.camera.getPosition().x();
            double y = entity.yOld + (entity.getY() - entity.yOld) * pt - renderManager.camera.getPosition().y();
            double z = entity.zOld + (entity.getZ() - entity.zOld) * pt - renderManager.camera.getPosition().z();

            float scale = 0.03F;
            int borderColor = color;

            if (damage && living.hurtTime > 0) {
                float hurtProgress = (float) living.hurtTime / 10.0f;

                float a = (color >> 24 & 0xFF) / 255f;
                float r = (color >> 16 & 0xFF) / 255f;
                float g = (color >> 8 & 0xFF) / 255f;
                float b = (color & 0xFF) / 255f;

                g *= (1.0f - hurtProgress * 0.8f);
                b *= (1.0f - hurtProgress * 0.8f);
                r = Math.min(1.0f, r + (1.0f - r) * hurtProgress * 0.8f);

                borderColor = ((int)(a * 255) << 24) | ((int)(r * 255) << 16) | ((int)(g * 255) << 8) | (int)(b * 255);
            }

            RenderSystem.disableDepthTest();
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            float angle = -renderManager.camera.getYRot();
            poseStack.mulPose(new Quaternionf().rotationY((float)Math.toRadians(angle)));
            poseStack.scale(scale, scale, scale);

            int width = 21;
            int height = 73;
            int thickness = 1;

            RenderUtils.drawRect(poseStack, -width, 0, -width - thickness, height, borderColor);
            RenderUtils.drawRect(poseStack, width, 0, width + thickness, height, borderColor);
            RenderUtils.drawRect(poseStack, -width, 0, width, thickness, borderColor);
            RenderUtils.drawRect(poseStack, -width, height - thickness, width, height, borderColor);

            float maxHealth = living.getMaxHealth();
            float health = Math.max(0, Math.min(living.getHealth(), maxHealth));
            float healthRatio = health / maxHealth;

            int barHeight = (int)(height * healthRatio);
            int barWidth = 1;
            int barX = width + 2;
            int barY = height - barHeight;

            int bgColor = 0x80000000;
            int healthColor = healthRatio > 0.5 ? 0xFF00FF00 : healthRatio > 0.25 ? 0xFFFFA500 : 0xFFFF0000;

            RenderUtils.drawRect(poseStack, barX, 0, barX + barWidth, height, bgColor);
            RenderUtils.drawRect(poseStack, barX, barY, barX + barWidth, height, healthColor);

            poseStack.popPose();

            RenderSystem.disableBlend();
            RenderSystem.enableDepthTest();
        }
    }

    public static void drawRect(PoseStack poseStack, int left, int top, int right, int bottom, int color) {
        int j;
        if (left < right) {
            j = left;
            left = right;
            right = j;
        }

        if (top < bottom) {
            j = top;
            top = bottom;
            bottom = j;
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        bufferBuilder.begin(VertexFormat.Mode.QUADS, POSITION_COLOR);
        bufferBuilder.vertex(matrix, (float) left, (float) bottom, 0.0F)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, (color >> 24) & 255)
                .endVertex();
        bufferBuilder.vertex(matrix, (float) right, (float) bottom, 0.0F)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, (color >> 24) & 255)
                .endVertex();
        bufferBuilder.vertex(matrix, (float) right, (float) top, 0.0F)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, (color >> 24) & 255)
                .endVertex();
        bufferBuilder.vertex(matrix, (float) left, (float) top, 0.0F)
                .color((color >> 16) & 255, (color >> 8) & 255, color & 255, (color >> 24) & 255)
                .endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());

        RenderSystem.disableBlend();
    }

    public static void drawRectangle(PoseStack poseStack, float x, float y, float width, float height, int color) {
        float endX = x + width;
        float endY = y + height;

        drawQuads(poseStack, x, endY, endX, endY, endX, y, x, y, color);
    }
    private static void drawQuads(PoseStack poseStack, float x, float y, float x2, float y2, float x3, float y3, float x4, float y4, int color) {
        // 获取颜色的 RGBA 分量
        float red = (float) (color >> 16 & 0xFF) / 255.0f;
        float green = (float) (color >> 8 & 0xFF) / 255.0f;
        float blue = (float) (color & 0xFF) / 255.0f;
        float alpha = (float) (color >> 24 & 0xFF) / 255.0f;

        // 启用混合，设置颜色和着色器
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.setShaderColor(red, green, blue, alpha);

        // 获取 BufferBuilder 用于绘制
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        // 开始绘制矩形
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(matrix, x2, y2, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(matrix, x3, y3, 0.0F).color(red, green, blue, alpha).endVertex();
        bufferBuilder.vertex(matrix, x4, y4, 0.0F).color(red, green, blue, alpha).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());

        // 禁用混合
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
    }

    public static void drawRoundedRect(PoseStack poseStack, double x, double y, double width, double height, double radius, Color color) {
        renderRoundedQuad(poseStack, color, x, y, x + width, y + height, radius, 128); // 进一步增加采样数
    }

    public static void renderRoundedQuad(PoseStack matrices, Color c, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        setupRender();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        renderRoundedQuadInternal(matrices.last().pose(), c.getRed() / 255f, c.getGreen() / 255f, c.getBlue() / 255f, c.getAlpha() / 255f, fromX, fromY, toX, toY, radius, samples);
        endRender();
    }

    public static void renderNameTagRoundedQuad(@NotNull PoseStack matrices, double fromX, double fromY, double toX, double toY, double rad, double samples, @NotNull Color c) {
        int color = c.getRGB();
        Matrix4f matrix = matrices.last().pose();
        float f = (float) (color >> 24 & 255) / 255.0F;
        float g = (float) (color >> 16 & 255) / 255.0F;
        float h = (float) (color >> 8 & 255) / 255.0F;
        float k = (float) (color & 255) / 255.0F;
        RenderSystem.enableBlend();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        renderRoundedQuadInternal(matrix, g, h, k, f, fromX, fromY, toX, toY, rad, samples);
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }
    public static void renderRoundedQuadInternal(Matrix4f matrix, float cr, float cg, float cb, float ca, double fromX, double fromY, double toX, double toY, double radius, double samples) {
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        bufferBuilder.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        if (toX - fromX < radius) {
            toX = fromX + radius;
        }

        if (toY - fromY < radius) {
            toY = fromY + radius;
        }

        double[][] map = new double[][]{
                new double[]{toX - radius, toY - radius, radius},
                new double[]{toX - radius, fromY + radius, radius},
                new double[]{fromX + radius, fromY + radius, radius},
                new double[]{fromX + radius, toY - radius, radius}
        };

        for (int i = 0; i < 4; i++) {
            double[] current = map[i];
            double rad = current[2];

            for (double r = i * 90d; r < (360 / 4d + i * 90d); r += (90 / samples)) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);

                bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).endVertex();
            }

            float rad1 = (float) Math.toRadians((360 / 4d + i * 90d));
            float sin = (float) (Math.sin(rad1) * rad);
            float cos = (float) (Math.cos(rad1) * rad);

            bufferBuilder.vertex(matrix, (float) current[0] + sin, (float) current[1] + cos, 0.0F).color(cr, cg, cb, ca).endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public static void drawPlayerHead(PoseStack poseStack, float x, float y, float width, float height, AbstractClientPlayer player) {
        if (mc.player == null || mc.level == null) return;

        ResourceLocation skin = mc.player.getSkinTextureLocation();

        try {
            skin = player.getSkinTextureLocation();
        } catch (Exception e) {
            // empty
        }

        int hurtTime = player.hurtTime;
        float redTint = 1.0f;

        if (hurtTime > 0) {
            float progress = (float) hurtTime / 10.0f;
            progress = Math.min(progress, 1.0f);
            redTint = lerp(progress, 0.6f, 1.0f);
        }

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderTexture(0, skin);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0f, redTint, redTint, 1.0f);

        // 使用正确的 Minecraft 1.20.1 blit 方法
        blit(poseStack, (int)x, (int)y, (int)width, (int)height, 8, 8, 8, 8, 64, 64);

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    public static void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static void endRender() {
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public static float lerp(float progress, float start, float end) {
        return start + (end - start) * (1.0f - progress);
    }
    private static void blit(PoseStack poseStack, int x, int y, int width, int height, int uOffset, int vOffset, int uWidth, int vHeight, int textureWidth, int textureHeight) {
        float u0 = (float)uOffset / (float)textureWidth;
        float u1 = (float)(uOffset + uWidth) / (float)textureWidth;
        float v0 = (float)vOffset / (float)textureHeight;
        float v1 = (float)(vOffset + vHeight) / (float)textureHeight;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        Matrix4f matrix = poseStack.last().pose();

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.vertex(matrix, x, y + height, 0).uv(u0, v1).endVertex();
        bufferBuilder.vertex(matrix, x + width, y + height, 0).uv(u1, v1).endVertex();
        bufferBuilder.vertex(matrix, x + width, y, 0).uv(u1, v0).endVertex();
        bufferBuilder.vertex(matrix, x, y, 0).uv(u0, v0).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public static void renderItemIcon(PoseStack poseStack, double x, double y, ItemStack itemStack) {
        if (!itemStack.isEmpty()) {
            renderGuiItem(poseStack, itemStack, (int) x, (int) y);
        }
    }
    public static void renderGuiItem(PoseStack poseStack, ItemStack itemStack, int x, int y) {
        renderGuiItem(poseStack, itemStack, x, y, mc.getItemRenderer().getModel(itemStack, null, null, 0));
    }
    public static void renderGuiItem(PoseStack poseStack, ItemStack itemStack, int x, int y, BakedModel model) {
        mc.getTextureManager().getTexture(TextureAtlas.LOCATION_BLOCKS).setFilter(false, false);
        RenderSystem.setShaderTexture(0, TextureAtlas.LOCATION_BLOCKS);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.pushPose();

        // 使用固定的 Z 偏移值 (100.0F) 替代 blitOffset
        poseStack.translate(x, y, 100.0F);
        poseStack.translate(8.0, 8.0, 0.0);
        poseStack.scale(1.0F, -1.0F, 1.0F);
        poseStack.scale(16.0F, 16.0F, 16.0F);
        RenderSystem.applyModelViewMatrix();

        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        boolean flag = !model.usesBlockLight();
        if (flag) {
            Lighting.setupForFlatItems();
        }

        // 在1.20.1中使用 ItemDisplayContext.GUI
        mc.getItemRenderer().render(itemStack, ItemDisplayContext.GUI, false, poseStack, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, model);
        bufferSource.endBatch();
        RenderSystem.enableDepthTest();
        if (flag) {
            Lighting.setupFor3DItems();
        }

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    public static void drawGradientRectL2R(PoseStack poseStack, float x, float y, float width, float height, int startColor, int endColor) {
        drawGradientRect2(poseStack, x, y, x + width, y + height, startColor, endColor);
    }

    public static void drawGradientRect2(PoseStack poseStack, float left, float top, float right, float bottom, int startColor, int endColor) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // 提取颜色的 RGBA 分量
        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;

        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;

        // 绘制渐变矩形（从左到右）
        bufferBuilder.begin(VertexFormat.Mode.QUADS, POSITION_COLOR);

        // 左上角（使用 startColor）
        bufferBuilder.vertex(matrix, left, top, 0.0F)
                .color(startRed, startGreen, startBlue, startAlpha)
                .endVertex();

        // 左下角（使用 startColor）
        bufferBuilder.vertex(matrix, left, bottom, 0.0F)
                .color(startRed, startGreen, startBlue, startAlpha)
                .endVertex();

        // 右下角（使用 endColor）
        bufferBuilder.vertex(matrix, right, bottom, 0.0F)
                .color(endRed, endGreen, endBlue, endAlpha)
                .endVertex();

        // 右上角（使用 endColor）
        bufferBuilder.vertex(matrix, right, top, 0.0F)
                .color(endRed, endGreen, endBlue, endAlpha)
                .endVertex();

        BufferUploader.drawWithShader(bufferBuilder.end());

        RenderSystem.disableBlend();
    }

    public static void drawModel(PoseStack poseStack, float yaw, float pitch, LivingEntity target) {
        Lighting.setupFor3DItems();
        MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();

        float originalYaw = target.getYRot();
        float originalPitch = target.getXRot();
        float originalBodyYaw = target.yBodyRot;

        target.yBodyRot = yaw - 0.4f;
        target.setYRot(yaw - 0.2f);
        target.setXRot(pitch);

        poseStack.pushPose();
        // 使用正确的旋转方式
        poseStack.mulPose(new org.joml.Quaternionf().rotationZ((float)Math.toRadians(180F)));
        poseStack.scale(-50, 50, 50);

        livingEntity = target;
        mc.getEntityRenderDispatcher().render(
                target,
                0.0D, 0.0D, 0.0D,
                0.0F,
                1.0F,
                poseStack,
                buffer,
                15728880
        );

        buffer.endBatch();
        poseStack.popPose();

        target.setYRot(originalYaw);
        target.setXRot(originalPitch);
        target.yBodyRot = originalBodyYaw;
        livingEntity = null;
        Lighting.setupForFlatItems();
    }

    public static void drawGradientRectU2D(PoseStack poseStack, float x, float y, float width, float height, int startColor, int endColor) {
        drawGradientRect(poseStack, x, y, x + width, y + height, startColor, endColor);
    }

    public static void drawGradientRect(PoseStack poseStack, float left, float top, float right, float bottom, int startColor, int endColor) {
        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        float startAlpha = (float) (startColor >> 24 & 255) / 255.0F;
        float startRed = (float) (startColor >> 16 & 255) / 255.0F;
        float startGreen = (float) (startColor >> 8 & 255) / 255.0F;
        float startBlue = (float) (startColor & 255) / 255.0F;

        float endAlpha = (float) (endColor >> 24 & 255) / 255.0F;
        float endRed = (float) (endColor >> 16 & 255) / 255.0F;
        float endGreen = (float) (endColor >> 8 & 255) / 255.0F;
        float endBlue = (float) (endColor & 255) / 255.0F;

        bufferBuilder.begin(VertexFormat.Mode.QUADS, POSITION_COLOR);
        bufferBuilder.vertex(matrix, right, top, 0.0F)
                .color(startRed, startGreen, startBlue, startAlpha)
                .endVertex();
        bufferBuilder.vertex(matrix, left, top, 0.0F)
                .color(startRed, startGreen, startBlue, startAlpha)
                .endVertex();
        bufferBuilder.vertex(matrix, left, bottom, 0.0F)
                .color(endRed, endGreen, endBlue, endAlpha)
                .endVertex();
        bufferBuilder.vertex(matrix, right, bottom, 0.0F)
                .color(endRed, endGreen, endBlue, endAlpha)
                .endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());

        RenderSystem.disableBlend();
    }

    public static void blitTextureWithColor(PoseStack poseStack, int x, int y, float u, float v, int width, int height, float textureWidth, float textureHeight, Color c, Color c1, Color c2, Color c3) {
        float f = 1.0F / textureWidth;
        float f1 = 1.0F / textureHeight;

        Matrix4f matrix = poseStack.last().pose();
        BufferBuilder builder = Tesselator.getInstance().getBuilder();

        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.vertex(matrix, x, y + height, 0).uv(u * f, (v + (float) height) * f1).color(c3.getRed(), c3.getGreen(), c3.getBlue(), c3.getAlpha()).endVertex();
        builder.vertex(matrix, x + width, y + height, 0).uv((u + (float) width) * f, (v + (float) height) * f1).color(c2.getRed(), c2.getGreen(), c2.getBlue(), c2.getAlpha()).endVertex();
        builder.vertex(matrix, x + width, y, 0).uv((u + (float) width) * f, v * f1).color(c1.getRed(), c1.getGreen(), c1.getBlue(), c1.getAlpha()).endVertex();
        builder.vertex(matrix, x, y, 0).uv(u * f, v * f1).color(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha()).endVertex();

        BufferUploader.drawWithShader(builder.end());
    }

    public static void drawHueBar(PoseStack poseStack, float x, float y, float width, float height) {
        for (int i = 0; i < width; i++) {
            float hue = i / width;
            int rgb = Color.HSBtoRGB(hue, 1.0f, 1.0f);
            drawRect(poseStack, (int) (x + i), (int) y, (int) (x + i + 1), (int) (y + height), rgb | 0xFF000000);
        }
    }

    public static void drawSVBox(PoseStack poseStack, float x, float y, float size, float size2, float hue) {
        for (int i = 0; i < size; i++) {
            float sat = i / size;
            for (int j = 0; j < size2; j++) {
                float bright = 1 - j / size2;
                int rgb = Color.HSBtoRGB(hue, sat, bright);
                drawRect(poseStack, (int) (x + i), (int) (y + j), (int) (x + i + 1), (int) (y + j + 1), rgb | 0xFF000000);
            }
        }
    }

    public static void drawCircle(PoseStack poseStack, float x, float y, float radius, Color color) {
        renderCircle(poseStack, color, x, y, radius, 30);
    }

    private static void renderCircle(PoseStack matrices, Color c, float cx, float cy, float r, int samples) {
        setupRender();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        Matrix4f matrix = matrices.last().pose();
        float red = c.getRed() / 255f;
        float green = c.getGreen() / 255f;
        float blue = c.getBlue() / 255f;
        float alpha = c.getAlpha() / 255f;

        BufferBuilder buffer = Tesselator.getInstance().getBuilder();
        buffer.begin(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION_COLOR);

        buffer.vertex(matrix, cx, cy, 0).color(red, green, blue, alpha).endVertex();

        for (int i = 0; i <= samples; i++) {
            double angle = 2 * Math.PI * i / samples;
            float dx = (float) (Math.cos(angle) * r);
            float dy = (float) (Math.sin(angle) * r);
            buffer.vertex(matrix, cx + dx, cy + dy, 0).color(red, green, blue, alpha).endVertex();
        }

        BufferUploader.drawWithShader(buffer.end());
        endRender();
    }

    public static Vec3 getCameraPos() {
        Camera camera = mc.getBlockEntityRenderDispatcher().camera;
        return camera.getPosition();
    }

    public static void drawSolidBox(AABB bb, PoseStack matrixStack) {
        Tesselator tessellator = RenderSystem.renderThreadTesselator();
        BufferBuilder bufferBuilder = tessellator.getBuilder();
        Matrix4f matrix = matrixStack.last().pose();
        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    public static void drawOutlinedBox(AABB bb, PoseStack matrixStack) {
        Matrix4f matrix = matrixStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION);
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.minY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.minY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.minZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.maxX, (float)bb.maxY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.maxZ).endVertex();
        bufferBuilder.vertex(matrix, (float)bb.minX, (float)bb.maxY, (float)bb.minZ).endVertex();
        BufferUploader.drawWithShader(bufferBuilder.end());
    }

}
