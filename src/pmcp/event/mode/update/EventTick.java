package pmcp.event.mode.update;

import lombok.Getter;
import pmcp.event.Event;

public class EventTick extends Event {

    @Getter
    private final Type type;

    public EventTick(Type type) {
        this.type = type;
    }

    public enum Type {
        PRE,
        POST
    }
}
