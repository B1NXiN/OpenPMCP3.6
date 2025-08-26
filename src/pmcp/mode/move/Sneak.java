package pmcp.mode.move;

import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import pmcp.event.EventTarget;
import pmcp.event.mode.player.EventMotion;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.utils.client.OldNaming;

public class Sneak extends Mode {
    public Sneak() {
        super("Sneak", "蹲跑", "让你跟猴子一样蹲着跑", Category.MOVE);
    }

    @EventTarget
    private void onMotion(EventMotion eventMotion) {
        if (mc.player == null || mc.level == null) return;

        if (eventMotion.getType() == EventMotion.Type.Pre) {

            mc.player.connection.send(OldNaming.C0BPacketEntityAction(mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
            mc.player.connection.send(OldNaming.C0BPacketEntityAction(mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
        }

        if (eventMotion.getType() == EventMotion.Type.Post) {
            mc.player.connection.send(OldNaming.C0BPacketEntityAction(mc.player, ServerboundPlayerCommandPacket.Action.RELEASE_SHIFT_KEY));
            mc.player.connection.send(OldNaming.C0BPacketEntityAction(mc.player, ServerboundPlayerCommandPacket.Action.PRESS_SHIFT_KEY));
        }
    }
}
