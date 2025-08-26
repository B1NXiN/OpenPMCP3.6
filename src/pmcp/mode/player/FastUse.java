package pmcp.mode.player;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.item.Items;
import pmcp.event.EventTarget;
import pmcp.event.mode.packet.EventPacket;
import pmcp.event.mode.update.EventUpdate;
import pmcp.mode.Mode;
import pmcp.mode.move.Fly;
import pmcp.utils.category.Category;
import pmcp.utils.client.OldNaming;
import pmcp.utils.rotation.SilentRotation;
import pmcp.value.values.ModeValue;

public class FastUse extends Mode {

    private final ModeValue<mode> modeValue = new ModeValue<>(this, "Mode", mode.values(), mode.Grim);

    enum mode {
        Grim,
        Packet
    }

    public FastUse() {
        super("FastUse", "快速使用", "加速你的使用物品速度", Category.PLAYER);
    }

    private int ticks = 0;
    private boolean grim = false;
    private boolean isUse;

    @Override
    public void onEnable() {
        isUse = false;
        ticks = 0;
    }

    @Override
    public void onDisable() {
        ticks = 0;
    }

    @EventTarget
    private void onTick(EventUpdate eventUpdate) {
        if (mc.player == null || mc.level == null) return;


        if (((mc.player.getMainHandItem().getItem() == Items.GOLDEN_APPLE || mc.player.getMainHandItem().getItem() == Items.ENCHANTED_GOLDEN_APPLE)
                || (mc.player.getOffhandItem().getItem() == Items.GOLDEN_APPLE || mc.player.getOffhandItem().getItem() == Items.ENCHANTED_GOLDEN_APPLE))
                && mc.player.isUsingItem()) {

            switch (modeValue.getValue()) {
                case Grim -> {
                    ticks++;

                    if (grim || ticks == 2) {
                        grim = false;
                    }

                    if (ticks > 2) {
                        mc.player.connection.sendNoEvent(OldNaming.C06PacketPlayerPosLook());
                        mc.player.connection.sendNoEvent(OldNaming.C03PacketPlayer());
                        mc.timer.lastMs += 50L;
                        isUse = true;
                    }
                }
                case Packet -> {
                    for (int i = 0; i < 32; i++) {
                        mc.player.connection.send(OldNaming.C03PacketPlayer());
                    }
                }
            }
        } else {
            if (isUse) {
                ticks = 0;
                mc.timer.timerSpeed = 1f;
                isUse = false;
            }
        }
    }

    @EventTarget
    private void onPacket(EventPacket eventPacket) {
        Packet<?> packet = eventPacket.getPacket();

        if (modeValue.getValue().equals(mode.Grim)) {
            if (packet instanceof ClientboundPlayerPositionPacket) {
                grim = true;
            }
        }
    }
}
