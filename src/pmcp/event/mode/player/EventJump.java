package pmcp.event.mode.player;

import lombok.Getter;
import lombok.Setter;
import pmcp.event.Event;

@Getter
@Setter
public class EventJump extends Event {
    private float yaw;

    public EventJump(float yaw) {
        this.yaw = yaw;
    }
}
