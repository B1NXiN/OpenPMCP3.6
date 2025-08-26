package pmcp.event.mode.packet;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import pmcp.event.Event;

@Setter
@Getter
public class EventPacket extends Event {
    private Packet<?> packet;
    private Type type;

    public EventPacket(Packet<?> packet, Type type) {
        this.packet = packet;
        this.type = type;
    }

    public enum Type {
        Client,
        Server
    }
}
