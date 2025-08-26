package net.minecraft.client.gui.screens;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.realmsclient.RealmsMainScreen;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import pmcp.PMCP;
import pmcp.config.ConfigManager;
import pmcp.utils.client.HoveringUtils;
import pmcp.utils.render.AnimationUtils;
import pmcp.utils.render.RenderUtils;

import java.awt.*;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class TitleScreen extends Screen {
   private static final ResourceLocation LOGO = new ResourceLocation("pmcp/png/pmcp.png");
   public static final CubeMap CUBE_MAP = new CubeMap(new ResourceLocation("textures/gui/title/background/panorama"));
   private static final ResourceLocation PANORAMA_OVERLAY = new ResourceLocation("textures/gui/title/background/panorama_overlay.png");
   private final PanoramaRenderer panorama = new PanoramaRenderer(CUBE_MAP);
   private double moveX_1;
   private double moveX_2;
   private double moveButtonX;
   private long fadeInStart;
   private double moveBackY;
   private boolean mouse0;
   private boolean mouse1;
   private int ticks = 1;
   private double scale;

   enum clickMode {
      Click_1,
      Click_2,
      Click_3,
      Click_4,
      Click_5;
   }

   public TitleScreen() {
      super(Component.literal("PMCP Title"));
   }

   @Override
   protected void init() {
      moveX_1 = -width;
      moveX_2 = this.width + this.width;
      moveButtonX = -width;
      moveBackY = this.height;
      scale = 0;
   }

   @Override
   public void tick() {
      ticks++;
      if (ticks >= 16) ticks = 1;
   }

   @Override
   public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
      if (this.fadeInStart == 0L) {
         this.fadeInStart = Util.getMillis();
      }

      PoseStack poseStack = pGuiGraphics.pose();

      float f = (float) (Util.getMillis() - this.fadeInStart) / 1000.0F;
      GlStateManager._disableDepthTest();
      this.panorama.render(pPartialTick, Mth.clamp(f, 0.0F, 1.0F));
      RenderSystem.enableBlend();

      moveBackY = AnimationUtils.smooth(0, moveBackY, 0.05);

      RenderUtils.drawRect(pGuiGraphics, 0,0,width,(int) moveBackY, Color.BLACK.getRGB());

      if (moveBackY < ((double) height / 6)) {

         scale = AnimationUtils.smooth(1, scale, 8F / Minecraft.getInstance().getFps());

         {
            int x = 230;
            int y = 30;
            int w = width - 280;
            int h = height - 60;

            poseStack.pushPose();
            poseStack.translate(x + (width / 2F), y + (height / 2F), 0);
            poseStack.scale((float) scale, (float) scale, (float) scale);
            poseStack.translate(-(x + (width / 2F)), -(y + (height / 2F)), 0);
            RenderUtils.drawImage(pGuiGraphics, new ResourceLocation("pmcp/png/clickgui.png"), x, y, w, h);
            poseStack.popPose();
         }

         moveX_1 = AnimationUtils.smooth(120, moveX_1, 8F / this.minecraft.getFps());
         RenderUtils.drawRect(pGuiGraphics, 0, 0, (int) moveX_1, height, new Color(11, 28, 42).getRGB());
         for (int i = 1; i < 201; i++) {
            RenderUtils.drawRect(pGuiGraphics, ((int) moveX_1 - 1) + i, 0, 1, height, new Color(11, 28, 42, 255 - i).getRGB());
         }

         moveX_2 = AnimationUtils.smooth(320, moveX_2, 8F / this.minecraft.getFps());
         RenderUtils.drawRect(pGuiGraphics, (int) moveX_2, 0, width - (int) moveX_2, height, new Color(11, 28, 42, 55).getRGB());

         RenderUtils.drawImage(pGuiGraphics, LOGO, 5, -50, 200, 200);
         RenderUtils.drawImage(pGuiGraphics, new ResourceLocation("pmcp/fubuki/" + ticks + ".png"), width - 100, height - 100, 100, 100);

         moveButtonX = AnimationUtils.smooth(20, moveButtonX, 8F / this.minecraft.getFps());

         pGuiGraphics.drawString(font, "MouseX: " + pMouseX + "  MouseY: " + pMouseY, 10, height - font.lineHeight - 1, Color.WHITE.getRGB());

         drawButton(pGuiGraphics, (int) moveButtonX, 150, 180, 50, "pmcp/button/单人有鼠标.png", "pmcp/button/单人无鼠标.png", clickMode.Click_1, pMouseX, pMouseY);
         drawButton(pGuiGraphics, (int) moveButtonX, 220, 180, 50, "pmcp/button/多人有鼠标.png", "pmcp/button/多人无鼠标.png", clickMode.Click_2, pMouseX, pMouseY);
         drawButton(pGuiGraphics, (int) moveButtonX, 290, 180, 50, "pmcp/button/设置有鼠标.png", "pmcp/button/设置无鼠标.png", clickMode.Click_3, pMouseX, pMouseY);
         drawButton(pGuiGraphics, (int) moveButtonX, 350, 180, 50, "pmcp/button/ClickGUI有按钮.png", "pmcp/button/ClickGUI无按钮.png", clickMode.Click_4, pMouseX, pMouseY);
         drawButton(pGuiGraphics, (int) moveButtonX + 220, 350, 50, 50, "pmcp/button/退出有鼠标.png", "pmcp/button/退出无鼠标.png", clickMode.Click_5, pMouseX, pMouseY);
      }
   }

   @Override
   public boolean shouldCloseOnEsc() {
      return false;
   }

   @Override
   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      mouse0 = false;
      mouse1 = false;

      return super.mouseReleased(mouseX, mouseY, button);
   }

   @Override
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      mouse0 = button == 0;
      mouse1 = button == 1;
      return super.mouseClicked(mouseX, mouseY, button);
   }

   private void drawButton(GuiGraphics pGuiGraphics, int x, int y, int width, int height, String hoverImage, String normalImage, clickMode clickMode, double pMouseX, double pMouseY) {
      boolean isHovering = HoveringUtils.isHovering(x, y, width, height, pMouseX, pMouseY);
      ResourceLocation resourceLocation = isHovering ? new ResourceLocation(hoverImage) : new ResourceLocation(normalImage);
      RenderUtils.drawImage(pGuiGraphics, resourceLocation, x, y, width, height);

      if (isHovering && mouse0) {

         switch (clickMode) {
            case Click_1 -> this.minecraft.setScreen(new SelectWorldScreen(this));
            case Click_2 -> this.minecraft.setScreen(new JoinMultiplayerScreen(this));
            case Click_3 -> this.minecraft.setScreen(new OptionsScreen(this, this.minecraft.options));
            case Click_4 -> this.minecraft.setScreen(PMCP.Instance.getClickGUI());
            case Click_5 -> this.minecraft.stop();
         }
         mouse0 = false;
      }
   }

   public static CompletableFuture<Void> preloadResources(TextureManager pTexMngr, Executor pBackgroundExecutor) {
      return CompletableFuture.allOf(pTexMngr.preload(LogoRenderer.MINECRAFT_LOGO, pBackgroundExecutor), pTexMngr.preload(LogoRenderer.MINECRAFT_EDITION, pBackgroundExecutor), pTexMngr.preload(PANORAMA_OVERLAY, pBackgroundExecutor), CUBE_MAP.preload(pTexMngr, pBackgroundExecutor));
   }

}
