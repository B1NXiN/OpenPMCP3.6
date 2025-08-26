package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;
import net.optifine.entity.model.IEntityRenderer;
import net.optifine.reflect.Reflector;
import net.optifine.reflect.ReflectorForge;
import net.optifine.util.Either;
import org.checkerframework.checker.units.qual.C;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import pmcp.PMCP;
import pmcp.event.EventManager;
import pmcp.mode.client.HUDSetting;
import pmcp.mode.hud.TargetHUD;
import pmcp.mode.render.NameTags;
import pmcp.ui.font.FontManager;
import pmcp.utils.player.NameTag;
import pmcp.utils.render.RenderUtils;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public abstract class EntityRenderer<T extends Entity> implements IEntityRenderer {
   protected static final float NAMETAG_SCALE = 0.025F;
   protected final EntityRenderDispatcher entityRenderDispatcher;
   private final Font font;
   public float shadowRadius;
   public float shadowStrength = 1.0F;
   private EntityType entityType = null;
   private ResourceLocation locationTextureCustom = null;
   public float shadowOffsetX;
   public float shadowOffsetZ;

   protected EntityRenderer(EntityRendererProvider.Context pContext) {
      this.entityRenderDispatcher = pContext.getEntityRenderDispatcher();
      this.font = pContext.getFont();
   }

   public final int getPackedLightCoords(T pEntity, float pPartialTicks) {
      BlockPos blockpos = BlockPos.containing(pEntity.getLightProbePosition(pPartialTicks));
      return LightTexture.pack(this.getBlockLightLevel(pEntity, blockpos), this.getSkyLightLevel(pEntity, blockpos));
   }

   protected int getSkyLightLevel(T pEntity, BlockPos pPos) {
      return pEntity.level().getBrightness(LightLayer.SKY, pPos);
   }

   protected int getBlockLightLevel(T pEntity, BlockPos pPos) {
      return pEntity.isOnFire() ? 15 : pEntity.level().getBrightness(LightLayer.BLOCK, pPos);
   }

   public boolean shouldRender(T pLivingEntity, Frustum pCamera, double pCamX, double pCamY, double pCamZ) {
      if (!pLivingEntity.shouldRender(pCamX, pCamY, pCamZ)) {
         return false;
      } else if (pLivingEntity.noCulling) {
         return true;
      } else {
         AABB aabb = pLivingEntity.getBoundingBoxForCulling().inflate(0.5D);
         if (aabb.hasNaN() || aabb.getSize() == 0.0D) {
            aabb = new AABB(pLivingEntity.getX() - 2.0D, pLivingEntity.getY() - 2.0D, pLivingEntity.getZ() - 2.0D, pLivingEntity.getX() + 2.0D, pLivingEntity.getY() + 2.0D, pLivingEntity.getZ() + 2.0D);
         }

         return pCamera.isVisible(aabb);
      }
   }

   public Vec3 getRenderOffset(T pEntity, float pPartialTicks) {
      return Vec3.ZERO;
   }

   public void render(T pEntity, float pEntityYaw, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {

      if (!Reflector.RenderNameTagEvent_Constructor.exists()) {
         if (this.shouldShowName(pEntity)) {
            this.renderNameTag(pEntity, pEntity.getDisplayName(), pPoseStack, pBuffer, pPackedLight);
         }

      } else {
         Object object = Reflector.newInstance(Reflector.RenderNameTagEvent_Constructor, pEntity, pEntity.getDisplayName(), this, pPoseStack, pBuffer, pPackedLight, pPartialTick);
         Reflector.postForgeBusEvent(object);
         Object object1 = Reflector.call(object, Reflector.Event_getResult);
         if (object1 != ReflectorForge.EVENT_RESULT_DENY && (object1 == ReflectorForge.EVENT_RESULT_ALLOW || this.shouldShowName(pEntity))) {
            Component component = (Component)Reflector.call(object, Reflector.RenderNameTagEvent_getContent);
            this.renderNameTag(pEntity, component, pPoseStack, pBuffer, pPackedLight);
         }

      }
   }

   protected boolean shouldShowName(T pEntity) {
      return pEntity.shouldShowName() && pEntity.hasCustomName();
   }

   public abstract ResourceLocation getTextureLocation(T pEntity);

   public Font getFont() {
      return this.font;
   }

   protected void renderNameTag(T pEntity, Component pDisplayName, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight) {
      FontManager fontManager = PMCP.Instance.getFontManager();

      double d0 = this.entityRenderDispatcher.distanceToSqr(pEntity);
      boolean flag = !(d0 > 4096.0D);
      if (Reflector.ForgeHooksClient_isNameplateInRenderDistance.exists()) {
         flag = Reflector.ForgeHooksClient_isNameplateInRenderDistance.callBoolean(pEntity, d0);
      }

      float f = pEntity.getNameTagOffsetY();
      int i = "deadmau5".equals(pDisplayName.getString()) ? -10 : 0;
      boolean flag1 = !pEntity.isDiscrete();
      float f1 = Minecraft.getInstance().options.getBackgroundOpacity(0.25F);

      int j = (int) (f1 * 255.0F) << 24;
      Font font = this.getFont();

      float f2 = (float) (-font.width(pDisplayName) / 2);

      pPoseStack.pushPose();
      pPoseStack.translate(0.0F, f, 0.0F);
      pPoseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());

      if (RenderUtils.livingEntity != pEntity) {

         if (NameTags.Instance.isEnable()) {

            switch (NameTags.Instance.modeValue.getValue()) {
               case PMCP -> {
                  if (!(pEntity instanceof Player player)) {
                     drawName(pDisplayName, pPoseStack, pBuffer, pPackedLight, flag, i, flag1, j, font, f2);
                  } else {

                  }
               }
               case Minecraft -> drawName(pDisplayName, pPoseStack, pBuffer, pPackedLight, flag, i, flag1, j, font, f2);

               case Modern -> {
                  if (pEntity instanceof Player player) {



                  } else {
                     drawName(pDisplayName, pPoseStack, pBuffer, pPackedLight, flag, i, flag1, j, font, f2);
                  }
               }
            }
         } else {
            drawName(pDisplayName, pPoseStack, pBuffer, pPackedLight, flag, i, flag1, j, font, f2);
         }
      }

      pPoseStack.popPose();
   }


   private void drawName(Component pDisplayName, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, boolean flag, float i, boolean flag1, int j, Font font, float f2) {
      if (flag) {
         pPoseStack.scale(-0.025F, -0.025F, 0.025F);
         Matrix4f matrix4f = pPoseStack.last().pose();
         font.drawInBatch(pDisplayName, f2, i, 553648127, false, matrix4f, pBuffer, flag1 ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, j, pPackedLight);
         if (flag1) {
            font.drawInBatch(pDisplayName, f2, i, -1, false, matrix4f, pBuffer, Font.DisplayMode.NORMAL, 0, pPackedLight);
         }
      }
   }


   public Either<EntityType, BlockEntityType> getType() {
      return this.entityType == null ? null : Either.makeLeft(this.entityType);
   }

   public void setType(Either<EntityType, BlockEntityType> type) {
      this.entityType = type.getLeft().get();
   }

   public ResourceLocation getLocationTextureCustom() {
      return this.locationTextureCustom;
   }

   public void setLocationTextureCustom(ResourceLocation locationTextureCustom) {
      this.locationTextureCustom = locationTextureCustom;
   }
}