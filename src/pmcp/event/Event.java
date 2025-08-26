package pmcp.event;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class Event {
    private boolean cancelled;

    public void cancelEvent() {
        this.cancelled = true;
    }

}
