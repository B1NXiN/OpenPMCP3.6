package pmcp.mode.move;

import pmcp.mode.Mode;
import pmcp.utils.category.Category;

public class NoJumpDelay extends Mode {
    public NoJumpDelay() {
        super("NoJumpDelay", "无跳跃延迟", "移除你的跳跃延迟", Category.MOVE);
    }
}
