package pmcp.mode.render;

import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.value.values.NumberValue;

public class FullBright extends Mode {

    public static FullBright Instance;

    public final NumberValue<Number> brightness = new NumberValue<>(this, "brightness", 1.0, 0.0, 1.0, 0.1);

    public FullBright() {
        super("FullBright", "高亮显示", "让你游戏变的更明亮", Category.RENDER);
        Instance = this;
    }
}
