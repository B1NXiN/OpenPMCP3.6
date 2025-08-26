package pmcp.mode.misc;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import pmcp.event.EventTarget;
import pmcp.event.mode.packet.EventPacket;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;

public class Disabler extends Mode {

    public static Disabler Instance;
    public Disabler() {
        super("Disabler", "禁用器", "禁用反作弊的一些功能", Category.MISC);
        Instance = this;
    }

    private float playerYaw;
    private float deltaYaw;
    private float lastPlacedDeltaYaw;
    private boolean rotated = false;

    @EventTarget
    private void onPacket(EventPacket event) {
        Packet<?> packet = event.getPacket();

        if (mc.player == null || mc.level == null) return;

        if (!event.isCancelled()) {
            if (packet instanceof ServerboundMovePlayerPacket movePacket) {
                if (!movePacket.hasRotation()) return;

                float yaw = movePacket.getYRot(0.0F);
                float pitch = movePacket.getXRot(0.0F);

                if (yaw > -360.0F && yaw < 360.0F) {
                    if (movePacket.hasPosition()) {
                        event.setPacket(new ServerboundMovePlayerPacket.PosRot(
                                movePacket.getX(0.0D),
                                movePacket.getY(0.0D),
                                movePacket.getZ(0.0D),
                                yaw + 720.0F,
                                pitch,
                                movePacket.isOnGround()
                        ));
                    } else {
                        event.setPacket(new ServerboundMovePlayerPacket.Rot(
                                yaw + 720.0F,
                                pitch,
                                movePacket.isOnGround()
                        ));
                    }
                }

                // 计算 yaw 差值
                float lastPlayerYaw = this.playerYaw;
                this.playerYaw = movePacket.getYRot(0.0F);
                this.deltaYaw = Math.abs(this.playerYaw - lastPlayerYaw);
                this.rotated = true;

                // 检查重复 yaw
                if (this.deltaYaw > 2.0F) {
                    float yawDiff = Math.abs(this.deltaYaw - this.lastPlacedDeltaYaw);
                    if (yawDiff < 1.0E-4D) {
                        if (movePacket.hasPosition()) {
                            event.setPacket(new ServerboundMovePlayerPacket.PosRot(movePacket.getX(0.0D), movePacket.getY(0.0D), movePacket.getZ(0.0D), movePacket.getYRot(0.0F) + 0.002F, movePacket.getXRot(0.0F), movePacket.isOnGround()));
                        } else {
                            event.setPacket(new ServerboundMovePlayerPacket.Rot(movePacket.getYRot(0.0F) + 0.002F, movePacket.getXRot(0.0F), movePacket.isOnGround()));
                        }
                    }
                }
            } else if (packet instanceof ServerboundUseItemOnPacket && this.rotated) {
                this.lastPlacedDeltaYaw = this.deltaYaw;
                this.rotated = false;
            }
        }
    }



}
