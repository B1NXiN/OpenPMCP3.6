package pmcp.event.mode.player;

import lombok.Getter;
import lombok.Setter;
import pmcp.event.Event;

@Getter
@Setter
public class EventKeyMoveInput extends Event {
    private float movementForward;
    private float movementSideways;
    private boolean jumping;
    private boolean shiftKeyDown;
    private boolean up;
    private boolean down;
    private boolean left;
    private boolean right;

    public EventKeyMoveInput(float movementForward, float movementSideways, boolean up, boolean down, boolean left, boolean right, boolean jumping, boolean shiftKeyDown) {
        this.movementForward = movementForward;
        this.movementSideways = movementSideways;
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
        this.jumping = jumping;
        this.shiftKeyDown = shiftKeyDown;
    }
}
