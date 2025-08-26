package pmcp.event.mode.player;

import lombok.Getter;
import lombok.Setter;
import pmcp.event.Event;

@Getter
@Setter
public class EventSlow extends Event {

    private float leftImpulse;
    private float forwardImpulse;

    public EventSlow(float leftImpulse, float forwardImpulse) {
        this.leftImpulse = leftImpulse;
        this.forwardImpulse = forwardImpulse;
    }
}
