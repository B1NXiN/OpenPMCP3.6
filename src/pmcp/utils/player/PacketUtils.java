package pmcp.utils.player;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import pmcp.utils.client.MinecraftInstance;

import java.util.ArrayList;

public class PacketUtils extends MinecraftInstance {

    public static boolean isVelocity(Packet<?> packet) {
        if (mc.player == null) return false;

        return (packet instanceof ClientboundSetEntityMotionPacket velocity
                && velocity.getId() == mc.player.getId()
                && velocity.getYa() > 0
                && (velocity.getXa() != 0 || velocity.getZa() != 0));
    }

    public static boolean isExplosion(Packet<?> packet) {
        if (mc.player == null) return false;

        return (packet instanceof ClientboundExplodePacket explodePacket &&
                (mc.player.getMotionY() + explodePacket.getKnockbackY()) > 0.0
                && ((mc.player.getMotionX() + explodePacket.getKnockbackX()) != 0.0 || (mc.player.getMotionZ() + explodePacket.getKnockbackZ()) != 0.0));
    }

    public static PacketType getPacketType(Packet<?> packet) {
        String className = packet.getClass().getSimpleName();
        if (className.toUpperCase().startsWith("C")) {
            return PacketType.SERVERSIDE;
        } else if (className.toUpperCase().startsWith("S")) {
            return PacketType.CLIENTSIDE;
        }
        return PacketType.UNKNOWN;
    }

    public static void send(Packet<ServerGamePacketListener> packet) {
        if (mc.getConnection() != null) {
            mc.getConnection().send(packet);
        }
    }
    public static void sendNoEvent(Packet<ServerGamePacketListener> packet) {
        if (mc.getConnection() != null) {
            mc.getConnection().sendNoEvent(packet);
        }
    }

    public enum PacketType {
        SERVERSIDE,
        CLIENTSIDE,
        UNKNOWN
    }
}
