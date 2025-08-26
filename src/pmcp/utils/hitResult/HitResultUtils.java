package pmcp.utils.hitResult;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import pmcp.utils.client.MinecraftInstance;
import pmcp.utils.rotation.Rotation;
import pmcp.utils.rotation.SilentRotation;

public class HitResultUtils extends MinecraftInstance {

    public static boolean isLookEntity(double range) {
        if (nullWorld()) return false;

        HitResult hitResult = pick(range, mc.player);

        return hitResult != null && hitResult.getType() == HitResult.Type.ENTITY;
    }

    protected static HitResult pick(double range, LivingEntity entity) {
        Vec3 vec3 = entity.getEyePosition();
        Vec3 vec31 = getViewVector(entity);
        Vec3 vec32 = vec3.add(vec31.x * range, vec31.y * range, vec31.z * range);
        return entity.level().clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
    }

    protected static Vec3 getViewVector(LivingEntity entity) {
        return entity.calculateViewVector(SilentRotation.getRotations().getPitch(), SilentRotation.getRotations().getYaw());
    }
}
