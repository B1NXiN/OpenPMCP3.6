package pmcp.utils.player;


import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import pmcp.utils.math.MathHelper;

@Getter
public class FallingPlayer {
    private final Minecraft mc = Minecraft.getInstance();

    private double x, y, z;
    private double motionX, motionY, motionZ;
    private float yaw;
    private float strafe;
    private float forward;
    private float jumpMovementFactor;

    public FallingPlayer(double x, double y, double z, double motionX, double motionY, double motionZ, float yaw, float strafe, float forward, float jumpMovementFactor) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.motionX = motionX;
        this.motionY = motionY;
        this.motionZ = motionZ;
        this.yaw = yaw;
        this.strafe = strafe;
        this.forward = forward;
        this.jumpMovementFactor = jumpMovementFactor;
    }

    public FallingPlayer(LocalPlayer player) {
        this(player.getX(), player.getY(), player.getZ(), player.getMotionX(), player.getMotionY(), player.getMotionZ(), player.getYRot(), player.input.leftImpulse, player.input.forwardImpulse, 0.02f);
    }

    private void calculateForTick() {
        float sr = strafe * 0.98f;
        float fw = forward * 0.98f;
        float v = sr * sr + fw * fw;

        if (v >= 0.0001f) {
            v = (float) Math.sqrt(v);
            if (v < 1.0f) v = 1.0f;

            float fixedJumpFactor = jumpMovementFactor;
            if (mc.player.isSprinting()) {
                fixedJumpFactor *= 1.3f;
            }

            v = fixedJumpFactor / v;
            sr *= v;
            fw *= v;

            float sin = MathHelper.sin(yaw * (float) Math.PI / 180.0f);
            float cos = MathHelper.cos(yaw * (float) Math.PI / 180.0f);

            motionX += sr * cos - fw * sin;
            motionZ += fw * cos + sr * sin;
        }

        motionY -= 0.08;
        motionY *= 0.98;

        x += motionX;
        y += motionY;
        z += motionZ;

        motionX *= 0.91;
        motionZ *= 0.91;
    }

    public void calculate(int ticks) {
        for (int i = 0; i < ticks; i++) {
            calculateForTick();
        }
    }

    public BlockPos findCollision(int ticks) {
        for (int i = 0; i < ticks; i++) {
            Vec3 start = new Vec3(x, y, z);
            calculateForTick();
            Vec3 end = new Vec3(x, y, z);
            double w = mc.player.getBbWidth() / 2.0;

            BlockPos result;
            if ((result = rayTrace(start, end)) != null) return result;
            if ((result = rayTrace(start.add(w, 0, w), end)) != null) return result;
            if ((result = rayTrace(start.add(-w, 0, w), end)) != null) return result;
            if ((result = rayTrace(start.add(w, 0, -w), end)) != null) return result;
            if ((result = rayTrace(start.add(-w, 0, -w), end)) != null) return result;
            if ((result = rayTrace(start.add(w, 0, w / 2), end)) != null) return result;
            if ((result = rayTrace(start.add(-w, 0, w / 2), end)) != null) return result;
            if ((result = rayTrace(start.add(w / 2, 0, w), end)) != null) return result;
            if ((result = rayTrace(start.add(w / 2, 0, -w), end)) != null) return result;
        }

        return null;
    }

    private BlockPos rayTrace(Vec3 start, Vec3 end) {
        ClipContext context = new ClipContext(
                start,
                end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                mc.player
        );
        HitResult hit = mc.level.clip(context);
        if (hit.getType() == HitResult.Type.BLOCK && ((BlockHitResult) hit).getDirection() == Direction.UP) {
            return ((BlockHitResult) hit).getBlockPos();
        }
        return null;
    }
}

