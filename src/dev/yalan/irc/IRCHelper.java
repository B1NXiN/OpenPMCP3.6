package dev.yalan.irc;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.entity.player.Player;
import pmcp.event.EventTarget;
import pmcp.event.mode.packet.EventPacket;
import pmcp.mode.client.IRCSetting;
import pmcp.utils.client.Helper;
import pmcp.utils.client.MinecraftInstance;

public class IRCHelper extends MinecraftInstance {

    @EventTarget
    public void onPacket(EventPacket eventPacket) {
        Packet<?> packet = eventPacket.getPacket();

        if (mc.player == null || mc.level == null || mc.level.getServer() == null) return;

        if (IRCSetting.Instance == null || IRCSetting.Instance.ircFiend.getValue()) {
            if (eventPacket.getType() == EventPacket.Type.Client) {
                if (packet instanceof ServerboundInteractPacket interactPacket) {
                    if (interactPacket.getTarget(mc.level.getServer().overworld()) instanceof Player player) {
                        if (player.ircUser != null) {
                            eventPacket.cancelEvent();
                            Helper.sendMessage("IRC", "你无法攻击同一个IRC的玩家！");
                        }
                    }
                }
            }
        }
    }
}
