package pmcp.mode.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.opengl.GL11;
import pmcp.PMCP;
import pmcp.event.EventTarget;

import pmcp.event.mode.render.EventRender2D;
import pmcp.event.mode.render.EventRender3D;
import pmcp.mode.Mode;
import pmcp.mode.client.HUDSetting;
import pmcp.mode.exploit.KillerESP;
import pmcp.mode.misc.Teams;
import pmcp.ui.font.FontManager;
import pmcp.utils.category.Category;
import pmcp.utils.player.EntityUtils;
import pmcp.utils.player.FriendManager;
import pmcp.utils.player.NameTag;
import pmcp.utils.render.RenderUtils;
import pmcp.value.values.ColorValue;
import pmcp.value.values.ModeValue;
import pmcp.value.values.NumberValue;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class NameTags extends Mode {

    public static NameTags Instance;

    public ModeValue<mode> modeValue = new ModeValue<>(this, "Mode", mode.values(), mode.PMCP);

    public enum mode {
        Minecraft,
        Modern,
        PMCP
    }

    public NameTags() {
        super("NameTags", "名称标签", "显示自定义名称标签", Category.RENDER);
        Instance = this;
    }

    @EventTarget
    private void onRender3D(EventRender3D eventRender3D) {
        if (mc.player == null || mc.level == null) return;


        PoseStack poseStack = eventRender3D.getPoseStack();

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Player pPlayer) {
                FontManager fontManager = PMCP.Instance.getFontManager();

                Camera camera = mc.gameRenderer.getMainCamera();

                RenderSystem.enableBlend();
                RenderSystem.setShader(GameRenderer::getPositionShader);
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableCull();
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);

                poseStack.pushPose();

                double xD = pPlayer.xOld + (pPlayer.getX() - pPlayer.xOld) * mc.getFrameTime();
                double yD = pPlayer.yOld + (pPlayer.getY() - pPlayer.yOld) * mc.getFrameTime();
                double zD = pPlayer.zOld + (pPlayer.getZ() - pPlayer.zOld) * mc.getFrameTime();

                poseStack.translate(xD - camera.getPosition().x, (yD - camera.getPosition().y) + pPlayer.getNameTagOffsetY(), zD - camera.getPosition().z);
                poseStack.mulPose(Axis.YP.rotationDegrees(-camera.getYRot()));
                poseStack.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));

                switch (modeValue.getValue()) {
                    case PMCP -> {
                        float s = (Minecraft.getInstance().player.distanceTo(entity) / 100F);

                        if (s > 0.05f) {
                            s = 0.05f;
                        }

                        poseStack.scale(-s, -s, s);

                        NameTag nameTags = NameTags.Instance.getName(entity);
                        String name = nameTags.rank() + nameTags.name();

                        Color color = Color.WHITE;
                        if (Teams.Instance.isEnable() && Teams.Instance.isTeam(pPlayer)) {
                            color = new Color(0x56CC9D);
                        }

                        if (EntityUtils.isValidTarget(pPlayer, mc)) {
                            color = new Color(255, 0, 0);
                        }

                        if (FriendManager.isFriend(pPlayer.getName().getString())) {
                            color = new Color(255, 255, 0);
                        }

                        if (pPlayer.ircUser != null) {
                            color = new Color(82, 188, 255);
                        }

                        float width = fontManager.big18.getStringWidth(name) + 6;
                        float height = fontManager.big18.getHeight() + 4;
                        float x = -(width / 2);
                        float y = -height;

                        RenderUtils.drawRoundedRect(poseStack, x, y, width, height, 5, HUDSetting.Instance.getColor());
                        fontManager.big18.drawString(poseStack, name, x + 3, y + 2, color.getRGB());

                    }
                    case Modern -> renderNameTag(poseStack, pPlayer);
                }

                poseStack.popPose();

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                RenderSystem.depthMask(true);
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.enableCull();
                RenderSystem.enableDepthTest();
                RenderSystem.disableBlend();
            }
        }
    }

    public NameTag getName(Entity entity) {
        NameTag nameTag = new NameTag(entity.getName().getString(),"『NPC』", ChatFormatting.WHITE);

        if (entity instanceof Player player && player != mc.player) {

            if (Teams.Instance.isEnable() && Teams.Instance.isTeam(player)) {
                nameTag = new NameTag(player.getName().getString(),"『队友』", ChatFormatting.GREEN);
            }

            if (EntityUtils.isValidTarget(player, mc)) {
                nameTag = new NameTag(player.getName().getString(),"『敌人』", ChatFormatting.RED);
            }

            if (FriendManager.isFriend(player.getName().getString())) {
                nameTag = new NameTag(player.getName().getString(),"『好友』", ChatFormatting.YELLOW);
            }

            if (player.ircUser != null) {
                nameTag = new NameTag(player.getName().getString(),"『IRC』", ChatFormatting.BOLD);
            }

            if (KillerESP.Instance != null && KillerESP.Instance.detectedKillers.contains(player.getName().getString())) {
                nameTag = new NameTag(player.getName().getString(),"『杀手』", ChatFormatting.RED);
            }
        }

        return nameTag;
    }

    public NameTag getName(Entity entity, boolean t) {
        NameTag nameTag = new NameTag(entity.getName().getString(),"[NPC] ", ChatFormatting.WHITE);

        if (entity instanceof Player player && player != mc.player) {

            if (Teams.Instance.isEnable() && Teams.Instance.isTeam(player)) {
                nameTag = new NameTag(player.getName().getString(),"[队友] ", ChatFormatting.GREEN);
            }

            if (EntityUtils.isValidTarget(player, mc)) {
                nameTag = new NameTag(player.getName().getString(),"[敌人] ", ChatFormatting.RED);
            }

            if (FriendManager.isFriend(player.getName().getString())) {
                nameTag = new NameTag(player.getName().getString(),"[好友] ", ChatFormatting.YELLOW);
            }

            if (player.ircUser != null) {
                nameTag = new NameTag(player.getName().getString(),"[IRC] ", ChatFormatting.BOLD);
            }

            if (KillerESP.Instance != null && KillerESP.Instance.detectedKillers.contains(player.getName().getString())) {
                nameTag = new NameTag(player.getName().getString(),"[杀手] ", ChatFormatting.RED);
            }
        }

        return nameTag;
    }

    private void renderNameTag(PoseStack poseStack, Player entity) {
        FontManager fontManager = PMCP.Instance.getFontManager();

        float s = (Minecraft.getInstance().player.distanceTo(entity) / 100F);

        if (s > 0.05f) {
            s = 0.05f;
        }

        poseStack.scale(-s, -s, s);

        NameTag nameTags = NameTags.Instance.getName(entity);

        String name = nameTags.color() + nameTags.rank() + ChatFormatting.WHITE + nameTags.name();

        final DecimalFormat decimalFormat = new DecimalFormat("##0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        float width = Math.max(fontManager.font26.getStringWidth(name), fontManager.font18.getStringWidth(decimalFormat.format(entity.getHealth()) + " HP")) + 10;
        float height = fontManager.font26.getHeight() + fontManager.font18.getHeight() + 17;

        RenderUtils.drawRectangle(poseStack, -(width / 2), -height, width, height, new Color(20, 20, 20, 120).getRGB());
        RenderUtils.drawRectangle(poseStack, -(width / 2), -3, width * (entity.getHealth() / entity.getMaxHealth()), 3, -1);
        RenderUtils.drawRectangle(poseStack, -(width / 2), -3, width, 3, new Color(20, 20, 20, 80).getRGB());
        fontManager.font26.drawString(poseStack, name, -(width / 2) + 5, -height + 5, -1);
        fontManager.font18.drawString(poseStack, decimalFormat.format(entity.getHealth()) + " HP", -(width / 2) + 5, -height + 8 + fontManager.font26.getHeight(), -1);
    }
}
