package pmcp.utils.rotation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.*;
import org.joml.Vector2f;
import pmcp.utils.math.MathHelper;
import pmcp.utils.client.MinecraftInstance;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import static java.lang.Math.sqrt;

public class RotationUtils extends MinecraftInstance {

    public static float[] getRotations(final Entity entity, boolean legit, int max, int min) {
        if (entity == null) {
            return new float[]{mc.player.getXRot(), mc.player.getYRot()};
        }
        final double diffX = entity.getX() - mc.player.getX();
        final double diffZ = entity.getZ() - mc.player.getZ();
        final double diffY = (entity.getY() + entity.getEyeHeight()) - (mc.player.getY() + mc.player.getEyeHeight());

        final double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);
        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) (-(Math.atan2(diffY, dist) * 180.0 / Math.PI));

        if (legit) {
            Random random = new Random();

            float yawOffset = (random.nextFloat() * max) - min;
            float pitchOffset = (random.nextFloat() * max) - min;

            yaw = yaw + yawOffset;
            pitch = pitch + pitchOffset;
        }

        return new float[]{yaw, pitch};
    }

    private static Rotation getRotationsByVec(final Vec3 origin, final Vec3 position) {
        final Vec3 difference = position.subtract(origin);
        final double distance = difference.flat().lengthVector();
        final float yaw = (float) Math.toDegrees(Math.atan2(difference.z, difference.x)) - 90.0f;
        final float pitch = (float) (-Math.toDegrees(Math.atan2(difference.y, distance)));
        return new Rotation(yaw, pitch);
    }

    public static Rotation getNCPRotations(final Vec3 vec, final boolean predict) {
        final Vec3 eyesPos = new Vec3(mc.player.getX(), mc.player.getBoundingBox().minY + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());

        if (predict) {
            eyesPos.add(mc.player.getDeltaMovement().x, mc.player.getDeltaMovement().y, mc.player.getDeltaMovement().z);
        }

        final double diffX = vec.x - eyesPos.x;
        final double diffY = vec.y + (mc.player.getBbHeight() / 2.0) - eyesPos.y;
        final double diffZ = vec.z - eyesPos.z;
        double hypotenuse = sqrt(diffX * diffX + diffZ * diffZ);

        return new Rotation(
                (float)(Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f,
                (float)(-Math.atan2(diffY, hypotenuse) * 180.0 / Math.PI)
        );
    }

    public static Rotation getRotation(Vec3 targetPos, Vec3 playerPos) {
        if (targetPos == null || playerPos == null) {
            throw new IllegalArgumentException("Target position or player position is null.");
        }

        double diffX = targetPos.x - playerPos.x;
        double diffY = targetPos.y - playerPos.y;
        double diffZ = targetPos.z - playerPos.z;

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;

        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, Math.sqrt(diffX * diffX + diffZ * diffZ)));

        return new Rotation(MathHelper.wrapDegrees(yaw), MathHelper.wrapDegrees(pitch));
    }

    public static Rotation getRotationBlock(final BlockPos pos) {
        return getRotationsByVec(mc.player.getPositionVector().add(0.0, mc.player.getEyeHeight(), 0.0), new Vec3(pos.getX() + 0.51, pos.getY() + 0.51, pos.getZ() + 0.51));
    }

    public static boolean isVisibleFOV(final Entity e, final float fov) {
        return ((Math.abs(RotationUtils.getRotations(e, false,0,0)[0] - mc.player.getYRot()) % 360.0f > 180.0f) ? (360.0f - Math.abs(RotationUtils.getRotations(e,false,0,0)[0] - mc.player.getYRot()) % 360.0f) : (Math.abs(RotationUtils.getRotations(e,false,0,0)[0] - mc.player.getYRot()) % 360.0f)) <= fov;
    }

    public static boolean isVisibleFOV(final BlockPos e, Direction facing, final float fov) {
        return ((Math.abs(RotationUtils.getRotationBlockLegit(e, facing).getYaw() - mc.player.getYRot()) % 360.0f > 180.0f) ? (360.0f - Math.abs(RotationUtils.getRotationBlockLegit(e, facing).getYaw() - mc.player.getYRot()) % 360.0f) : (Math.abs(RotationUtils.getRotationBlockLegit(e, facing).getYaw() - mc.player.getYRot()) % 360.0f)) <= fov;
    }

    public static Rotation getRotationBlockLegit(BlockPos targetPos, Direction facing) {
        Vec3 playerEyePosition = mc.player.getPosition(1.0F);

        double x = targetPos.getX() + 0.5D;
        double y = targetPos.getY() + 0.5D;
        double z = targetPos.getZ() + 0.5D;

        x += (double) facing.getNormal().getX() * 0.5D;
        y += (double) facing.getNormal().getY() * 0.5D;
        z += (double) facing.getNormal().getZ() * 0.5D;

        Vec3 to = new Vec3(x, y, z);

        final Vec3 diff = to.subtract(playerEyePosition);
        final double distance = Math.hypot(diff.x, diff.z);
        final float yaw = (float) (MathHelper.atan2(diff.z, diff.x) * (180.0D / Math.PI)) - 90.0F;
        final float pitch = (float) (-(MathHelper.atan2(diff.y, distance) *(180.0D / Math.PI)));

        return new Rotation(yaw, pitch);
    }


    public static double getAngleDifference(float a, float b) {
        return ((a - b) % 360f + 540f) % 360f - 180f;
    }


    public static Rotation getRotationDataToEntity(Entity target) {
        // 获取玩家眼睛位置
        Vec3 playerPos = new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        Vec3 eyePos = playerPos.add(0.0D, mc.player.getEyeHeight(), 0.0D);

        // 获取目标实体的包围盒
        AABB targetBox = getTargetBoundingBox(target);
        double minX = targetBox.minX;
        double minY = targetBox.minY;
        double minZ = targetBox.minZ;
        double maxX = targetBox.maxX;
        double maxY = targetBox.maxY;
        double maxZ = targetBox.maxZ;
        double spacing = 0.1D;

        // 构造采样点集
        Set<Vec3> points = new LinkedHashSet<>();

        // 添加：包围盒中心点
        points.add(new Vec3((minX + maxX) / 2.0D, (minY + maxY) / 2.0D, (minZ + maxZ) / 2.0D));

        // 添加：眼睛位置朝向包围盒最近点
        points.add(getClosestPoint(eyePos, targetBox));

        // 添加：六个面的每个方向网格点（每 0.1 采样）
        for (double x = minX; x <= maxX; x += spacing) {
            for (double y = minY; y <= maxY; y += spacing) {
                points.add(new Vec3(x, y, minZ));
                points.add(new Vec3(x, y, maxZ));
            }
        }

        for (double x = minX; x <= maxX; x += spacing) {
            for (double z = minZ; z <= maxZ; z += spacing) {
                points.add(new Vec3(x, minY, z));
                points.add(new Vec3(x, maxY, z));
            }
        }

        for (double y = minY; y <= maxY; y += spacing) {
            for (double z = minZ; z <= maxZ; z += spacing) {
                points.add(new Vec3(minX, y, z));
                points.add(new Vec3(maxX, y, z));
            }
        }

        // 遍历所有采样点，寻找第一个可视命中点
        for (Vec3 point : points) {
            Vector2f rotation = getRotations(eyePos, point); // yaw, pitch
            HitResult hit = rayTrace(rotation);

            if (checkHitResult(eyePos, hit, target)) {
                Vec3 hitPos = hit.getLocation();
                double distance = hitPos.distanceTo(eyePos);
                Vector2f fixedRotation = getFixedRotation(
                        rotation.x, rotation.y,
                        SilentRotation.getRotations().getYaw(),
                        SilentRotation.getRotations().getPitch()
                );

                return new Rotation(fixedRotation.x, fixedRotation.y);
            }
        }

        return null;
    }

    private static AABB getTargetBoundingBox(Entity entity) {
        return entity.getBoundingBox();
    }

    private static boolean checkHitResult(Vec3 eyePos, HitResult result, Entity target) {
        // 如果射线命中类型是实体，且命中的就是目标实体
        if (result.getType() == HitResult.Type.ENTITY && ((EntityHitResult) result).getEntity() == target) {
            Vec3 hitPos = result.getLocation();

            // 如果玩家眼睛在目标包围盒内部，或者与命中点的距离 ≤ 3 格，则认为命中有效
            return isVecInside(getTargetBoundingBox(target), eyePos)
                    || hitPos.distanceTo(eyePos) <= 3.0D;
        }

        // 否则不算命中
        return false;
    }

    public static Vector2f getFixedRotation(float yaw, float pitch, float lastYaw, float lastPitch) {
        // 从 Minecraft 控制设置中获取鼠标灵敏度
        double sensitivity = mc.options.sensitivity().get(); // 原: mc.f_91066_.m_231964_().m_231551_()

        // 根据灵敏度计算角度分辨率（GCD 值）
        float f = (float)(sensitivity * 0.6 + 0.2);
        float gcd = f * f * f * 1.2F;

        // 当前角度变化量（相对 last）
        float deltaYaw = yaw - lastYaw;
        float deltaPitch = pitch - lastPitch;

        // 将角度变化量限制为 gcd 的整数倍
        float fixedDeltaYaw = deltaYaw - (deltaYaw % gcd);
        float fixedDeltaPitch = deltaPitch - (deltaPitch % gcd);

        // 修正后的角度 = 上一帧角度 + 对齐后的变化量
        float fixedYaw = lastYaw + fixedDeltaYaw;
        float fixedPitch = lastPitch + fixedDeltaPitch;

        return new Vector2f(fixedYaw, fixedPitch);
    }


    public static boolean isVecInside(AABB box, Vec3 vec) {
        return vec.x > box.minX && vec.x < box.maxX
                && vec.y > box.minY && vec.y < box.maxY
                && vec.z > box.minZ && vec.z < box.maxZ;
    }

    public static Vec3 getClosestPoint(Vec3 vec, AABB aabb) {
        double closestX = Math.max(aabb.minX, Math.min(vec.x, aabb.maxX));
        double closestY = Math.max(aabb.minY, Math.min(vec.y, aabb.maxY));
        double closestZ = Math.max(aabb.minZ, Math.min(vec.z, aabb.maxZ));

        return new Vec3(closestX, closestY, closestZ);
    }

    public static Vector2f getRotations(Vec3 eye, Vec3 target) {
        // 差值向量：目标点相对于观察点的偏移
        double x = target.x - eye.x;
        double y = target.y - eye.y;
        double z = target.z - eye.z;

        // XZ 平面上的距离
        double diffXZ = Math.sqrt(x * x + z * z);

        // 计算 yaw：z/x 的反正切，转换为角度并偏移 -90°
        float yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90.0F;

        // 计算 pitch：y / xz 斜边的反正切，转换为角度并取负
        float pitch = (float) -Math.toDegrees(Math.atan2(y, diffXZ));

        return new Vector2f(
                Mth.wrapDegrees(yaw),
                Mth.wrapDegrees(pitch)
        );
    }

    private static HitResult rayTrace(Vector2f rotations) {
        // 最大追踪距离（视角设置，通常为 3.0 ~ 6.0）
        double reach = mc.gameMode.getPickRange(); // was: m_105286_()
        HitResult blockHit = RayTraceUtils.rayCast(reach, false, rotations);

        // 玩家视角位置（眼睛位置）
        Vec3 eyePos = mc.player.getEyePosition(1.0F);

        boolean overReach = false;

        if (reach > 3.0D) {
            overReach = true; // 用于后面判断超远距离实体命中
        }

        // 初始有效命中距离平方（来自 blockHit，若有）
        double maxSquaredDistance = reach * reach;
        if (blockHit != null) {
            maxSquaredDistance = blockHit.getLocation().distanceToSqr(eyePos);
        }

        // 朝指定角度旋转后的方向向量
        Vec3 lookVec = getLook(rotations.x, rotations.y);
        Vec3 traceEnd = eyePos.add(
                lookVec.x * reach,
                lookVec.y * reach,
                lookVec.z * reach
        );

        // 以玩家为起点的膨胀碰撞箱，用于实体筛选
        AABB boundingBox = mc.player.getBoundingBox()
                .expandTowards(lookVec.scale(reach))
                .inflate(1.0D); // 膨胀 1 格

        // 尝试在射线上检测实体（忽略 spectator 和非选中目标）
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                mc.player,
                eyePos,
                traceEnd,
                boundingBox,
                entity -> !entity.isSpectator() && entity.isPickable(),
                maxSquaredDistance
        );

        // 若命中实体
        if (entityHit != null) {
            Vec3 hitVec = entityHit.getLocation();
            double entityDist = eyePos.distanceToSqr(hitVec);

            // 如果距离过远，强制转换为方块命中
            if (overReach && entityDist > 81.0D) { // 9.0²
                Direction dir = Direction.getNearest(lookVec.x, lookVec.y, lookVec.z);
                return BlockHitResult.miss(hitVec, dir, BlockPos.containing(hitVec));
            }

            // 若实体更近，优先返回实体命中
            if (entityDist < maxSquaredDistance || blockHit == null) {
                return entityHit;
            }
        }

        // 否则返回原始的方块命中（可能为 MISS）
        return blockHit;
    }

    public static Vec3 getLook(float yaw, float pitch) {
        float yawRad = -yaw * ((float)Math.PI / 180F) - (float)Math.PI;
        float pitchRad = -pitch * ((float)Math.PI / 180F);

        float xz = Mth.cos(pitchRad);
        float y = Mth.sin(pitchRad);
        float x = Mth.cos(yawRad) * xz;
        float z = Mth.sin(yawRad) * xz;

        return new Vec3(x, y, z);
    }


    public static final Vec3 getEyePosition()
    {
        double d0 = mc.player.getX();
        double d1 = mc.player.getY() + (double) mc.player.getEyeHeight();
        double d2 = mc.player.getZ();
        return new Vec3(d0, d1, d2);
    }

}
