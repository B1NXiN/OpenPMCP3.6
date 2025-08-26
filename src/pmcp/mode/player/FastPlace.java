package pmcp.mode.player;

import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.value.values.BooleanValue;
import pmcp.value.values.NumberValue;

public class FastPlace extends Mode {

    public final NumberValue<Number> delayValue = new NumberValue<>(this, "Delay", 0,0,4,1);
    public final BooleanValue<Boolean> onlyBlockValue = new BooleanValue<>(this, "OnlyBlock", true);

    public FastPlace() {
        super("FastPlace", "快速放置", "将原版放置延迟减缓", Category.PLAYER);
    }
}
