package pmcp.event.mode.player;

import lombok.Getter;
import net.minecraft.world.entity.Entity;
import pmcp.event.Event;


@Getter
public class EventAttack extends Event {
    private final Entity target;
    private final Type type;

    public EventAttack(Entity target, Type type) {
        this.target = target;
        this.type = type;
    }

    public enum Type {
        Pre,
        Post
    }
}
