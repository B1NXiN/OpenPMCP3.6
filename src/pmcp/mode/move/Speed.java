package pmcp.mode.move;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import pmcp.event.EventTarget;
import pmcp.event.mode.player.EventStrafe;
import pmcp.mode.Mode;
import pmcp.mode.player.Blink;
import pmcp.utils.category.Category;
import pmcp.utils.rotation.SilentRotation;
import pmcp.value.values.ModeValue;
import pmcp.value.values.NumberValue;

public class Speed extends Mode {

    private final ModeValue<mode> modeValue = new ModeValue<>(this, "Mode", mode.values(), mode.GrimEntity);

    enum mode {
        GrimEntity,
        Normal
    }

    private final NumberValue<Number> speed = new NumberValue<>(this,"Speed", 8, 1, 15, 1);

    public Speed() {
        super("Speed", "加速", "用某种办法让你跑更快", Category.MOVE);
    }

    @EventTarget
    private void onStrafe(EventStrafe eventStrafe) {
        if (mc.player == null || mc.level == null) return;

        switch (modeValue.getValue()) {
            case GrimEntity -> {
                AABB playerBox = mc.player.getBoundingBox().inflate(1.0, 1.0, 1.0);
                int count = 0;

                for (Entity entity : mc.level.entitiesForRendering()) {
                    if (entity == null || entity.getId() == mc.player.getId()) continue;

                    if (!(entity instanceof LivingEntity)) continue;

                    if (entity.getId() == -8 || entity.getId() == -1337) continue;

                    if (!playerBox.intersects(entity.getBoundingBox())) continue;

                    count++;
                }

                if (count > 0 && mc.player.isMoving()) {
                    double strafeOffset = Math.min(count, 3) * (speed.getValue().doubleValue() / 100.0);
                    float yaw = this.getMoveYaw();
                    double mx = -Math.sin(Math.toRadians(yaw));
                    double mz = Math.cos(Math.toRadians(yaw));

                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(mx * strafeOffset, 0.0, mz * strafeOffset));
                }
            }
            case Normal -> {
                double strafeOffset = Math.min(1, 3) * (speed.getValue().doubleValue() / 100.0);
                float yaw = this.getMoveYaw();
                double mx = -Math.sin(Math.toRadians(yaw));
                double mz = Math.cos(Math.toRadians(yaw));

                if (mc.player.isMoving()) {
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().add(mx * strafeOffset, 0.0, mz * strafeOffset));
                }
            }
        }
    }

    private float getMoveYaw() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) return 0f;

        Input input = player.input;

        float moveForward = input.up ? 1f : (input.down ? -1f : 0f);
        float moveStrafe = input.left ? 1f : (input.right ? -1f : 0f);
        float moveYaw = SilentRotation.getRotations().getYaw();

        if (moveForward != 0.0f && moveStrafe == 0.0f) {
            moveYaw += moveForward > 0.0f ? 0.0f : 180.0f;
        } else if (moveForward != 0.0f && moveStrafe != 0.0f) {
            if (moveForward > 0.0f) {
                moveYaw += moveStrafe > 0.0f ? -45.0f : 45.0f;
            } else {
                moveYaw -= moveStrafe > 0.0f ? -45.0f : 45.0f;
                moveYaw += 180.0f;
            }
        } else if (moveStrafe != 0.0f && moveForward == 0.0f) {
            moveYaw += moveStrafe > 0.0f ? -70.0f : 70.0f;
        }

        // 判断是否按下跳跃键
        if (mc.options.keyJump.isDown()) {
            moveYaw = SilentRotation.getRotations().getYaw();
        }

        return moveYaw;
    }
}
