package pmcp.event.mode.player;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.phys.Vec3;
import pmcp.event.Event;

@Getter
@Setter
public class EventStrafe extends Event {
    private float forward, strafe;
    private float friction, p_191958_2_;

    private float yaw;

    public EventStrafe(Vec3 movementInput, float yaw, float speed) {
        this.strafe = (float) movementInput.x;
        this.forward = (float) movementInput.z;
        this.yaw = yaw;
        this.p_191958_2_ = (float) movementInput.y;
        this.friction = speed;
    }

    public Vec3 getMovementInput() {
        return new Vec3(strafe, p_191958_2_ , forward);
    }
}
