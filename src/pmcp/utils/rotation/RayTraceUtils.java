package pmcp.utils.rotation;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector2f;

import java.util.Optional;

public class RayTraceUtils {

    private static final Minecraft mc = Minecraft.getInstance();

    /**
     * 默认 Ray Trace 方法，使用当前玩家朝向与设定角度。
     */
    public static HitResult rayCast(Vector2f rotations) {
        Entity viewer = mc.getCameraEntity();
        if (viewer != null && mc.level != null) {
            double reach = mc.gameMode.getPickRange(); // Usually 3.0 - 6.0
            return pick(reach, true, rotations.x(), rotations.y());
        }
        return null;
    }

    /**
     * 自定义距离 + 是否检测液体 的 Ray Trace 方法。
     */
    public static HitResult rayCast(double range, boolean hitFluids, Vector2f rotations) {
        Entity viewer = mc.getCameraEntity();
        if (viewer != null && mc.level != null) {
            return pick(range, hitFluids, rotations.x(), rotations.y());
        }
        return null;
    }

    /**
     * 计算角度对应的朝向向量。
     */
    public static Vec3 calculateViewVector(float pitch, float yaw) {
        float pitchRad = pitch * ((float) Math.PI / 180F);
        float yawRad = -yaw * ((float) Math.PI / 180F);

        float cosYaw = Mth.cos(yawRad);
        float sinYaw = Mth.sin(yawRad);
        float cosPitch = Mth.cos(pitchRad);
        float sinPitch = Mth.sin(pitchRad);

        return new Vec3(sinYaw * cosPitch, -sinPitch, cosYaw * cosPitch);
    }

    /**
     * 使用自定义 yaw/pitch 角度，构造出 ray 并返回方块/实体命中结果。
     */
    public static HitResult pick(double distance, boolean hitFluids, float yaw, float pitch) {
        Vec3 eyePos = new Vec3(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(), mc.player.getZ());
        Vec3 look = calculateViewVector(pitch, yaw);
        Vec3 to = eyePos.add(look.x * distance, look.y * distance, look.z * distance);

        ClipContext context = new ClipContext(
                eyePos,
                to,
                ClipContext.Block.OUTLINE,
                hitFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                mc.player
        );

        return mc.level.clip(context);
    }

    public static HitResult pick(double distance, boolean hitFluids, Rotation rotation) {
        Vec3 eyePos = new Vec3(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(), mc.player.getZ());
        Vec3 look = calculateViewVector(rotation.getPitch(), rotation.getYaw());
        Vec3 to = eyePos.add(look.x * distance, look.y * distance, look.z * distance);

        ClipContext context = new ClipContext(
                eyePos,
                to,
                ClipContext.Block.OUTLINE,
                hitFluids ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE,
                mc.player
        );

        return mc.level.clip(context);
    }

    /**
     * 更复杂的方块 RayTrace（可设置是否跳过流体/虚无/不可碰撞方块等）。
     */
    public static HitResult rayTraceBlocks(Vec3 from, Vec3 to, boolean stopOnLiquid, boolean ignoreNoBB, boolean returnLastUncollidable, Entity viewer) {
        ClipContext.Block blockMode = ignoreNoBB
                ? ClipContext.Block.COLLIDER
                : (returnLastUncollidable ? ClipContext.Block.VISUAL : ClipContext.Block.OUTLINE);
        ClipContext.Fluid fluidMode = stopOnLiquid ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;

        ClipContext context = new ClipContext(from, to, blockMode, fluidMode, viewer);
        return mc.level.clip(context);
    }

    /**
     * 基于 AABB 和两点判断是否被 Box 拦截（返回拦截点）。
     */
    public static EntityHitResult calculateIntercept(AABB box, Vec3 from, Vec3 to) {
        Optional<Vec3> intercept = box.clip(from, to);
        return intercept.map(vec -> new EntityHitResult(null, vec)).orElse(null);
    }
}

