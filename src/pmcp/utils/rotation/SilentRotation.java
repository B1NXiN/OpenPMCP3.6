package pmcp.utils.rotation;

import lombok.Getter;
import lombok.Setter;
import pmcp.event.EventInterface;
import pmcp.event.EventTarget;
import pmcp.event.mode.player.EventJump;
import pmcp.event.mode.player.EventKeyMoveInput;
import pmcp.event.mode.player.EventMotion;
import pmcp.event.mode.player.EventStrafe;
import pmcp.event.mode.update.EventTick;
import pmcp.event.mode.update.EventUpdate;
import pmcp.utils.math.MathHelper;
import pmcp.utils.category.MoveCategory;
import pmcp.utils.client.MinecraftInstance;

public class SilentRotation extends MinecraftInstance implements EventInterface {
    @Getter
    @Setter
    public static Rotation targetRotation;
    public static Rotation playerRotation;
    public static int stopRotationTicks;

    @Getter
    private static Rotation rotations = new Rotation(0F,0F);
    @Getter
    private static MoveCategory category;

    private static boolean delay;

    @EventTarget
    private void onJump(EventJump eventJump) {
        if (targetRotation != null) {
            eventJump.setYaw(targetRotation.getYaw());
        }
    }

    @EventTarget
    private void onKey(EventKeyMoveInput event) {
        if (targetRotation != null && category == MoveCategory.Auto) {
            final float forward = event.getMovementForward();
            final float strafe = event.getMovementSideways();

            final double angle = MathHelper.wrapDegrees(Math.toDegrees(direction(playerRotation.getYaw(), forward, strafe)));

            if (forward == 0 && strafe == 0) {
                return;
            }

            float closestForward = 0, closestStrafe = 0, closestDifference = Float.MAX_VALUE;

            for (float predictedForward = -1F; predictedForward <= 1F; predictedForward += 1F) {
                for (float predictedStrafe = -1F; predictedStrafe <= 1F; predictedStrafe += 1F) {
                    if (predictedStrafe == 0 && predictedForward == 0) continue;

                    final double predictedAngle = MathHelper.wrapDegrees(Math.toDegrees(direction(targetRotation.getYaw(), predictedForward, predictedStrafe)));
                    final double difference = Math.abs(angle - predictedAngle);

                    if (difference < closestDifference) {
                        closestDifference = (float) difference;
                        closestForward = predictedForward;
                        closestStrafe = predictedStrafe;
                    }
                }
            }

            event.setMovementForward(closestForward);
            event.setMovementSideways(closestStrafe);
        }
    }

    @EventTarget
    private void onMotion(EventMotion eventMotion) {
        if (mc.player == null || mc.level == null) return;

        if (targetRotation != null) {
            eventMotion.setYaw(targetRotation.getYaw());
            eventMotion.setPitch(targetRotation.getPitch());
            mc.player.yBodyRot = targetRotation.getYaw();
            mc.player.yHeadRot = targetRotation.getYaw();
        }
    }

    @EventTarget
    private void onStrafe(EventStrafe eventStrafe) {
        if (targetRotation != null) {
            eventStrafe.setYaw(targetRotation.getYaw());
        }
    }

    @EventTarget
    private void onTick(EventTick eventTick) {
        if (mc.player ==  null || mc.level == null) return;

        playerRotation = new Rotation(mc.player.getYRot(), mc.player.getXRot());

        rotations = targetRotation != null ? targetRotation : playerRotation;
    }

    @EventTarget
    private void onUpdate(EventUpdate eventUpdate) {
        if (targetRotation != null) {
            stopRotationTicks--;

            if (stopRotationTicks <= 0) {
                stopRotation();
            }
        }
    }

    public static void setRotation(Rotation rotation, MoveCategory category, boolean delay) {
        stopRotationTicks = 5;

        float currentYaw = SilentRotation.getRotations().getYaw();

        float Yaw = rotation.getYaw() - currentYaw;

        if (Yaw > 180) {
            Yaw -= 360;
        } else if (Yaw < -180) {
            Yaw += 360;
        }

        float currentPitch = SilentRotation.getRotations().getPitch();
        float targetPitch = rotation.getPitch();
        float deltaPitch = targetPitch - currentPitch;

        if (deltaPitch > 180) deltaPitch -= 360;
        else if (deltaPitch < -180) deltaPitch += 360;

        SilentRotation.targetRotation = new Rotation(currentYaw + Yaw, currentPitch + deltaPitch);

        SilentRotation.category = category;
    }

    public double direction(float rotationYaw, final double moveForward, final double moveStrafing) {
        if (moveForward < 0F) rotationYaw += 180F;

        float forward = 1F;

        if (moveForward < 0F) forward = -0.5F;
        else if (moveForward > 0F) forward = 0.5F;

        if (moveStrafing > 0F) rotationYaw -= 90F * forward;
        if (moveStrafing < 0F) rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }

    public static float getMovingYaw() {
        return (float) (direction() * 180f / (float) Math.PI);
    }

    public static double direction() {
        float rotationYaw = mc.player.getYRot();
        if (mc.player.input.forwardImpulse < 0f) {
            rotationYaw += 180f;
        }

        float forward = 1f;
        if (mc.player.input.forwardImpulse < 0f) {
            forward = -0.5f;
        } else if (mc.player.input.forwardImpulse > 0f) {
            forward = 0.5f;
        }

        if (mc.player.input.leftImpulse > 0f) {
            rotationYaw -= 90f * forward;
        }
        if (mc.player.input.leftImpulse < 0f) {
            rotationYaw += 90f * forward;
        }

        return Math.toRadians(rotationYaw);
    }


    public static void stopTargetRotation() {
        SilentRotation.stopRotationTicks = 0;
        SilentRotation.targetRotation = null;
    }

    public static void stopRotation() {
        if (SilentRotation.targetRotation != null) {
            SilentRotation.stopRotationTicks = 0;
            float currentYaw = targetRotation.getYaw();

            float Yaw = mc.player.getYRot() - currentYaw;

            if (Yaw > 180) {
                Yaw -= 360;
            } else if (Yaw < -180) {
                Yaw += 360;
            }

            float currentPitch = targetRotation.getPitch();
            float targetPitch = mc.player.getXRot();
            float deltaPitch = targetPitch - currentPitch;

            if (deltaPitch > 180) deltaPitch -= 360;
            else if (deltaPitch < -180) deltaPitch += 360;

            SilentRotation.targetRotation = new Rotation(currentYaw + Yaw, currentPitch + deltaPitch);
        }
        stopTargetRotation();
    }
}
