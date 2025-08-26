package pmcp.mode.player;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.world.level.block.AirBlock;
import pmcp.event.EventTarget;
import pmcp.event.mode.packet.EventPacket;
import pmcp.event.mode.player.EventKeyMoveInput;
import pmcp.event.mode.player.EventMotion;
import pmcp.event.mode.player.EventStrafe;
import pmcp.event.mode.update.EventTick;
import pmcp.event.mode.update.EventUpdate;
import pmcp.event.mode.world.EventWorld;
import pmcp.mode.Mode;
import pmcp.mode.world.Scaffold;
import pmcp.utils.category.Category;
import pmcp.utils.player.PlayerUtils;
import pmcp.value.values.ModeValue;

public class NoFall extends Mode {

    private final ModeValue<mode> modeValue = new ModeValue<>(this, "Mode", mode.values(), mode.NoGround);

    enum mode {
        NoGround,
        OnGround,
        Grim
    }

    public NoFall() {
        super("NoFall", "无摔落伤害", "让你摔落无伤害", Category.PLAYER);
    }

    private boolean falling = false;
    public static boolean shouldJump = false;

    @Override
    public void onEnable() {
        falling = false;
        shouldJump = false;
    }

    @EventTarget
    private void onPacket(EventPacket eventPacket) {
        if (mc.player == null || mc.level == null) return;

        Packet<?> packet = eventPacket.getPacket();

        switch (modeValue.getValue()) {
            case OnGround -> {
                if (packet instanceof ServerboundMovePlayerPacket playerPacket) {
                    if (mc.player.fallDistance > 2f) {
                        playerPacket.setOnGround(true);
                    }
                }
            }
            case NoGround -> {
                if (packet instanceof ServerboundMovePlayerPacket playerPacket) {
                    playerPacket.setOnGround(false);
                }
            }
        }
    }

    @EventTarget
    private void onTick(EventStrafe eventStrafe) {
        if (mc.player == null || mc.level == null) return;

        if (modeValue.getValue().equals(mode.Grim)) {
            if (shouldJump) {
                if (!mc.player.input.jumping) {
                    mc.player.jumpFromGround();
                }
                shouldJump = false;
            }
        }
    }

    @EventTarget
    private void onMotion(EventMotion eventMotion) {
        if (mc.player == null || mc.level == null) return;

        if (modeValue.getValue().equals(mode.Grim)) {

            if (eventMotion.getType() == EventMotion.Type.Pre) {
                if (Scaffold.Instance.isEnable()) {
                    return;
                }

                if (!mc.player.onGround() && mc.player.fallDistance > 4.0f) {
                    falling = true;
                    eventMotion.setOnGround(false);
                }

                if (mc.player.isInWater()) {
                    falling = false;
                }

                if (eventMotion.isOnGround() && falling && !shouldJump) {
                    eventMotion.setX(eventMotion.getX() + 10000);
                    eventMotion.setY(eventMotion.getY() + 10000);
                    eventMotion.setZ(eventMotion.getZ() + 10000);
                    eventMotion.setOnGround(false);
                    falling = false;
                    shouldJump = true;
                }
            }
        }
    }

    @EventTarget
    private void onWorld(EventWorld eventWorld) {
        falling = false;
        shouldJump = false;
    }
}
