package pmcp.event.mode.client;

import lombok.Getter;
import pmcp.event.Event;

@Getter
public class EventChat extends Event {
    private final String message;

    public EventChat(String message) {
        this.message = message;
    }
}
