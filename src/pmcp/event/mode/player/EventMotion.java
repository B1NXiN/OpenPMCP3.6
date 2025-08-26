package pmcp.event.mode.player;

import lombok.Getter;
import lombok.Setter;
import pmcp.event.Event;

@Getter
@Setter
public class EventMotion extends Event {
    private Type type;
    private float yaw;
    private float pitch;

    private double x;
    private double y;
    private double z;

    private boolean onGround;

    public EventMotion(final Type type, final float yaw, final float pitch, final  double x, final double y, final double z, final boolean onGround) {
        this.type = type;
        this.yaw = yaw;
        this.pitch = pitch;
        this.x = x;
        this.y = y;
        this.z = z;
        this.onGround = onGround;
    }

    public EventMotion(final Type type, final  double x, final double y, final double z, final boolean onGround) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
        this.onGround = onGround;
    }

    public enum Type {
        Pre,
        Post,
    }
}
