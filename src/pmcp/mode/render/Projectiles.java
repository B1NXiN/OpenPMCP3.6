package pmcp.mode.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import pmcp.event.EventTarget;
import pmcp.event.mode.render.EventRender3D;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.utils.projectile.BasicProjectileData;
import pmcp.utils.projectile.EntityArrowData;
import pmcp.utils.projectile.EntityPotionData;
import pmcp.utils.projectile.ProjectileData;
import pmcp.utils.render.RenderUtils;
import pmcp.utils.rotation.RayTraceUtils;
import pmcp.utils.rotation.SilentRotation;
import pmcp.value.values.BooleanValue;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Projectiles extends Mode {

    private final BooleanValue<Boolean> showArrows = new BooleanValue<>(this, "Arrows", true);
    private final BooleanValue<Boolean> showPearls = new BooleanValue<>(this, "Pearls", true);
    private final BooleanValue<Boolean> showPotions = new BooleanValue<>(this, "Potions", true);
    private final BooleanValue<Boolean> showEggs = new BooleanValue<>(this, "Eggs", true);
    private final BooleanValue<Boolean> showSnowballs = new BooleanValue<>(this, "Snowballs", true);

    private final EntityArrowData arrowsColor = new EntityArrowData();
    private final EntityPotionData potionsColor = new EntityPotionData();
    private final BasicProjectileData enderPearlColor = new BasicProjectileData(Collections.singleton(ThrownEnderpearl.class), new Color(173, 12, 255));
    private final BasicProjectileData eggColor = new BasicProjectileData(Collections.singleton(ThrownEgg.class), new Color(255, 238, 154));
    private final BasicProjectileData snowballColor = new BasicProjectileData(Collections.singleton(Snowball.class), new Color(255, 255, 255));

    public Projectiles() {
        super("Projectiles", "抛物线", "渲染出抛物线", Category.RENDER);
    }

    @EventTarget
    private void onRender3D(EventRender3D eventRender3D) {
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof net.minecraft.world.entity.projectile.Projectile) {
                ProjectileData var8 = this.getProjectileDataByEntity(entity);
                if (var8 != null) {
                    PoseStack stack = eventRender3D.getPoseStack();
                    stack.pushPose();
                    GL11.glEnable(3042);
                    GL11.glBlendFunc(770, 771);
                    GL11.glDisable(2929);
                    GL11.glDepthMask(false);
                    GL11.glEnable(2848);
                    RenderSystem.setShader(GameRenderer::getPositionShader);
                    Color color = var8.getColor(entity);
                    RenderSystem.setShaderColor(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, 1.0F);
                    this.render(stack, entity, var8);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    GL11.glDisable(3042);
                    GL11.glEnable(2929);
                    GL11.glDepthMask(true);
                    GL11.glDisable(2848);
                    stack.popPose();
                }
            }
        }

        Projectiles.Path pathResult = this.getPath(eventRender3D.getPartialTicks());
        if (pathResult != null) {
            List<Vec3> path = pathResult.getPath();
            if (path.size() >= 2) {
                PoseStack stack = eventRender3D.getPoseStack();
                stack.pushPose();
                GL11.glEnable(3042);
                GL11.glBlendFunc(770, 771);
                GL11.glDisable(2929);
                GL11.glDepthMask(false);
                GL11.glEnable(2848);
                RenderSystem.setShader(GameRenderer::getPositionShader);
                Vec3 camPos = path.get(0);
                this.drawLine(stack, path, camPos);
                if (!path.isEmpty()) {
                    Vec3 end = path.get(path.size() - 1);
                    this.drawEndOfLine(stack, end, camPos, pathResult.result, eventRender3D.getPartialTicks());
                }

                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                GL11.glDisable(3042);
                GL11.glEnable(2929);
                GL11.glDepthMask(true);
                GL11.glDisable(2848);
                stack.popPose();
            }
        }
    }

    private void drawLine(PoseStack matrixStack, List<Vec3> path, Vec3 camPos) {
        Matrix4f matrix = matrixStack.last().pose();
        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
        RenderSystem.setShader(GameRenderer::getPositionShader);
        bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION);
        float[] colorF = new float[]{1.0F, 1.0F, 1.0F};
        RenderSystem.setShaderColor(colorF[0], colorF[1], colorF[2], 1.0F);

        for (Vec3 point : path) {
            bufferBuilder.vertex(
                            matrix, (float)(point.x - camPos.x), (float)(point.y - camPos.y), (float)(point.z - camPos.z)
                    )
                    .endVertex();
        }

        BufferUploader.drawWithShader(bufferBuilder.end());
    }

    private void drawEndOfLine(PoseStack matrixStack, Vec3 end, Vec3 camPos, HitResult result, float partialTicks) {
        AABB bb = new AABB(0.15, 0.15, 0.15, 0.35, 0.35, 0.35);
        float[] colorF = new float[]{1.0F, 1.0F, 1.0F};
        if (result != null) {
            if (result.getType() == HitResult.Type.BLOCK) {
                BlockHitResult blockHitResult = (BlockHitResult)result;
                Direction direction = blockHitResult.getDirection();
                if (direction == Direction.SOUTH) {
                    bb = new AABB(0.0, 0.0, 0.0, 0.5, 0.5, 0.1);
                } else if (direction == Direction.NORTH) {
                    bb = new AABB(0.0, 0.0, 0.4, 0.5, 0.5, 0.5);
                } else if (direction == Direction.EAST) {
                    bb = new AABB(0.0, 0.0, 0.0, 0.1, 0.5, 0.5);
                } else if (direction == Direction.WEST) {
                    bb = new AABB(0.4, 0.0, 0.0, 0.5, 0.5, 0.5);
                } else if (direction == Direction.UP) {
                    colorF = new float[]{0.0F, 1.0F, 0.0F};
                    bb = new AABB(0.0, 0.0, 0.0, 0.5, 0.1, 0.5);
                } else if (direction == Direction.DOWN) {
                    bb = new AABB(0.0, 0.4, 0.0, 0.5, 0.5, 0.5);
                }
            } else if (result.getType() == HitResult.Type.ENTITY) {
                EntityHitResult entityHitResult = (EntityHitResult)result;
                colorF = new float[]{1.0F, 0.0F, 0.0F};
                RenderSystem.setShaderColor(colorF[0], colorF[1], colorF[2], 0.5F);
                Entity entity = entityHitResult.getEntity();
                double motionX = entity.getX() - entity.xo;
                double motionY = entity.getY() - entity.yo;
                double motionZ = entity.getZ() - entity.zo;
                Vec3 cameraPos = RenderUtils.getCameraPos();
                AABB move = entity.getBoundingBox()
                        .move(-cameraPos.x, -cameraPos.y, -cameraPos.z)
                        .move(-motionX, -motionY, -motionZ)
                        .move(partialTicks * motionX, partialTicks * motionY, partialTicks * motionZ)
                        .inflate(0.1);
                RenderUtils.drawSolidBox(move, matrixStack);
            }
        }

        double renderX = end.x - camPos.x;
        double renderY = end.y - camPos.y;
        double renderZ = end.z - camPos.z;
        matrixStack.pushPose();
        matrixStack.translate(renderX - 0.25, renderY - 0.25, renderZ - 0.25);
        RenderSystem.setShaderColor(colorF[0], colorF[1], colorF[2], 0.25F);
        RenderUtils.drawSolidBox(bb, matrixStack);
        RenderSystem.setShaderColor(colorF[0], colorF[1], colorF[2], 0.75F);
        RenderUtils.drawOutlinedBox(bb, matrixStack);
        matrixStack.popPose();
    }

    private Projectiles.Path getPath(float partialTicks) {
        Player player = mc.player;
        ArrayList<Vec3> path = new ArrayList<>();
        ItemStack stack = player.getMainHandItem();
        Item item = stack.getItem();
        if (!stack.isEmpty() && this.isThrowable(item)) {
            double arrowPosX = player.xOld + (player.getX() - player.xOld) * partialTicks;
            double arrowPosY = player.yOld + (player.getY() - player.yOld) * partialTicks + player.getEyeHeight() - 0.1;
            double arrowPosZ = player.zOld + (player.getZ() - player.zOld) * partialTicks;
            double arrowMotionFactor = item instanceof ProjectileWeaponItem ? 1.0 : 0.4;
            double yaw;
            double pitch;
            if (SilentRotation.targetRotation != null) {
                yaw = Math.toRadians(Mth.lerp(partialTicks, SilentRotation.targetRotation.getYaw(), SilentRotation.targetRotation.getYaw()));
                pitch = Math.toRadians(Mth.lerp(partialTicks, SilentRotation.targetRotation.getPitch(), SilentRotation.targetRotation.getPitch()));
            } else {
                yaw = Math.toRadians(Mth.lerp(partialTicks, player.yRotO, player.getYRot()));
                pitch = Math.toRadians(Mth.lerp(partialTicks, player.xRotO, player.getXRot()));
            }

            double arrowMotionX = -Math.sin(yaw) * Math.cos(pitch) * arrowMotionFactor;
            double arrowMotionY = -Math.sin(pitch) * arrowMotionFactor;
            double arrowMotionZ = Math.cos(yaw) * Math.cos(pitch) * arrowMotionFactor;
            double arrowMotion = Math.sqrt(arrowMotionX * arrowMotionX + arrowMotionY * arrowMotionY + arrowMotionZ * arrowMotionZ);
            arrowMotionX /= arrowMotion;
            arrowMotionY /= arrowMotion;
            arrowMotionZ /= arrowMotion;
            if (item instanceof ProjectileWeaponItem) {
                float bowPower = (72000 - player.getUseItemRemainingTicks()) / 20.0F;
                bowPower = (bowPower * bowPower + bowPower * 2.0F) / 3.0F;
                if (bowPower > 1.0F || bowPower <= 0.1F) {
                    bowPower = 1.0F;
                }

                bowPower *= 3.0F;
                arrowMotionX *= bowPower;
                arrowMotionY *= bowPower;
                arrowMotionZ *= bowPower;
            } else {
                arrowMotionX *= 1.5;
                arrowMotionY *= 1.5;
                arrowMotionZ *= 1.5;
            }

            double gravity = this.getProjectileGravity(item);

            for (int i = 0; i < 1000; i++) {
                Vec3 arrowPos = new Vec3(arrowPosX, arrowPosY, arrowPosZ);
                Vec3 postArrowPos = new Vec3(arrowPosX + arrowMotionX, arrowPosY + arrowMotionY, arrowPosZ + arrowMotionZ);
                path.add(arrowPos);
                ClipContext context = new ClipContext(arrowPos, postArrowPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player);
                BlockHitResult clip = mc.level.clip(context);
                if (clip.getType() != HitResult.Type.MISS) {
                    return new Projectiles.Path(path, clip);
                }

                Arrow fakeArrow = new Arrow(mc.level, arrowPosX, arrowPosY, arrowPosZ);
                EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
                        mc.level,
                        fakeArrow,
                        arrowPos,
                        postArrowPos,
                        fakeArrow.getBoundingBox().expandTowards(new Vec3(arrowMotionX, arrowMotionY, arrowMotionZ)).inflate(1.0),
                        entity -> entity != player && entity instanceof LivingEntity
                );
                if (entityHitResult != null && entityHitResult.getType() == HitResult.Type.ENTITY) {
                    return new Projectiles.Path(path, entityHitResult);
                }

                arrowPosX += arrowMotionX;
                arrowPosY += arrowMotionY;
                arrowPosZ += arrowMotionZ;
                arrowMotionX *= 0.99;
                arrowMotionY *= 0.99;
                arrowMotionZ *= 0.99;
                arrowMotionY -= gravity;
            }

            return new Projectiles.Path(path, null);
        } else {
            return null;
        }
    }

    private double getProjectileGravity(Item item) {
        if (item instanceof BowItem || item instanceof CrossbowItem) {
            return 0.05;
        } else if (item instanceof PotionItem) {
            return 0.4;
        } else if (item instanceof FishingRodItem) {
            return 0.15;
        } else {
            return item instanceof TridentItem ? 0.015 : 0.03;
        }
    }

    private boolean isThrowable(Item item) {
        return item instanceof BowItem
                || item instanceof CrossbowItem
                || item instanceof SnowballItem
                || item instanceof EggItem
                || item instanceof EnderpearlItem
                || item instanceof SplashPotionItem
                || item instanceof LingeringPotionItem
                || item instanceof FishingRodItem
                || item instanceof TridentItem;
    }

    private void render(PoseStack matrix, Entity entity, ProjectileData projectileInfo) {
        if (entity != null) {
            LocalPlayer thePlayer = mc.player;
            ClientLevel theWorld = mc.level;
            Color color = projectileInfo.getColor(entity);
            if (color == null) {
                color = new Color(255, 255, 255);
            }

            Tesselator tesselator = Tesselator.getInstance();
            BufferBuilder builder = tesselator.getBuilder();
            builder.begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            double posX = entity.getX();
            double posY = entity.getY();
            double posZ = entity.getZ();
            double motionX = entity.getDeltaMovement().x;
            double motionY = entity.getDeltaMovement().y;
            double motionZ = entity.getDeltaMovement().z;
            this.drawVertex(color, builder, matrix, posX, posY, posZ);

            while (true) {
                float data1 = projectileInfo.getData1();
                float data2 = projectileInfo.getData2();
                AABB aabb = new AABB(posX - data1, posY, posZ - data1, posX + data1, posY + data2, posZ + data1);
                Vec3 vec3 = new Vec3(posX, posY, posZ);
                Vec3 vec3WithMotion = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
                HitResult movingObj = RayTraceUtils.rayTraceBlocks(vec3, vec3WithMotion, false, entity instanceof Arrow, false, entity);
                if (!movingObj.getType().equals(HitResult.Type.MISS)) {
                    vec3WithMotion = new Vec3(movingObj.getLocation().x(), movingObj.getLocation().y(), movingObj.getLocation().z());
                }

                List<Entity> getByAABBEntitys = theWorld.getEntities(thePlayer, aabb.contract(motionX, motionY, motionZ).expandTowards(1.0, 1.0, 1.0));
                double lastMinDistance = 0.0;

                for (Entity aabbEntity : getByAABBEntitys) {
                    if (aabbEntity instanceof LivingEntity && !(aabbEntity instanceof EnderMan) && aabbEntity.canBeCollidedWith() && !aabbEntity.equals(thePlayer)) {
                        aabb = aabbEntity.getBoundingBox().expandTowards(0.3, 0.3, 0.3);
                        EntityHitResult aabbMovingObj = RayTraceUtils.calculateIntercept(aabb, vec3, vec3WithMotion);
                        if (aabbMovingObj != null) {
                            double distance = vec3.distanceTo(aabbMovingObj.getLocation());
                            if (distance < lastMinDistance || lastMinDistance == 0.0) {
                                lastMinDistance = distance;
                                movingObj = aabbMovingObj;
                            }
                        }
                    }
                }

                posX += motionX;
                posY += motionY;
                posZ += motionZ;
                if (!movingObj.getType().equals(HitResult.Type.MISS)) {
                    posX = movingObj.getLocation().x();
                    posY = movingObj.getLocation().y();
                    posZ = movingObj.getLocation().z();
                    break;
                }

                if (posY < -128.0) {
                    break;
                }

                motionX *= entity.isInWater() ? 0.8 : 0.99;
                double var39 = motionY * (entity.isInWater() ? 0.8 : 0.99);
                motionZ *= entity.isInWater() ? 0.8 : 0.99;
                motionY = var39 - projectileInfo.getGravity();
                this.drawVertex(color, builder, matrix, posX + motionX, posY + motionY, posZ + motionZ);
            }

            tesselator.end();
        }
    }

    private void drawVertex(Color color, BufferBuilder builder, PoseStack stack, double x, double y, double z) {
        Entity entity = mc.getCameraEntity();
        double d0 = entity.xOld + (entity.getX() - entity.xOld) * mc.getFrameTime();
        double d1 = entity.yOld + (entity.getY() - entity.yOld) * mc.getFrameTime();
        double d2 = entity.zOld + (entity.getZ() - entity.zOld) * mc.getFrameTime();
        builder.vertex(stack.last().pose(), (float)(x - d0), (float)(y - d1) - 1.5F, (float)(z - d2)).color(color.getRGB()).endVertex();
    }

    private ProjectileData getProjectileDataByEntity(Entity entity) {
        if (entity.onGround()) {
            return null;
        } else if (entity.getX() == entity.xOld && entity.getZ() == entity.zOld) {
            return null;
        } else {
            for (ProjectileData data : this.getProjectileInfos()) {
                if (data.isTargetEntity(entity)) {
                    return data;
                }
            }

            return null;
        }
    }

    private List<ProjectileData> getProjectileInfos() {
        ArrayList<ProjectileData> infos = new ArrayList<>();
        if (this.showArrows.getValue()) {
            infos.add(this.arrowsColor);
        }

        if (this.showPotions.getValue()) {
            infos.add(this.potionsColor);
        }

        if (this.showPearls.getValue()) {
            infos.add(this.enderPearlColor);
        }

        if (this.showEggs.getValue()) {
            infos.add(this.eggColor);
        }

        if (this.showSnowballs.getValue()) {
            infos.add(this.snowballColor);
        }

        return infos;
    }

    public static class Path {
        private final List<Vec3> path;
        private final HitResult result;

        public Path(List<Vec3> path, HitResult result) {
            this.path = path;
            this.result = result;
        }

        public List<Vec3> getPath() {
            return this.path;
        }

        public HitResult getResult() {
            return this.result;
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Projectiles.Path other)) {
                return false;
            } else if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$path = this.getPath();
                Object other$path = other.getPath();
                if (this$path == null ? other$path == null : this$path.equals(other$path)) {
                    Object this$result = this.getResult();
                    Object other$result = other.getResult();
                    return this$result == null ? other$result == null : this$result.equals(other$result);
                } else {
                    return false;
                }
            }
        }

        protected boolean canEqual(Object other) {
            return other instanceof Projectiles.Path;
        }

        @Override
        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Object $path = this.getPath();
            result = result * 59 + ($path == null ? 43 : $path.hashCode());
            Object $result = this.getResult();
            return result * 59 + ($result == null ? 43 : $result.hashCode());
        }

        @Override
        public String toString() {
            return "Projectile.Path(path=" + this.getPath() + ", result=" + this.getResult() + ")";
        }
    }
}
