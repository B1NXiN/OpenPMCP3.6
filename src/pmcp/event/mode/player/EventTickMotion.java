package pmcp.event.mode.player;

import lombok.Getter;
import lombok.Setter;
import pmcp.event.Event;
import pmcp.event.mode.update.EventTick;

@Getter
@Setter
public class EventTickMotion extends Event {
    private int ticks;

    public EventTickMotion(int ticks) {
        this.ticks = ticks;
    }
}
