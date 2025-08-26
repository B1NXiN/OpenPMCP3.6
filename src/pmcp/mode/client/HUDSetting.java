package pmcp.mode.client;

import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.value.values.BooleanValue;
import pmcp.value.values.ColorValue;
import pmcp.value.values.NumberValue;

import java.awt.*;

public class HUDSetting extends Mode {

    public static HUDSetting Instance;

    public final NumberValue<Number> alpha = new NumberValue<>(this, "Alpha", 125,1,255,1);

    public HUDSetting() {
        super("HUDSetting", "用户平视界面设置", "设置视觉的一些基础配置", Category.CLIENT);
        Instance = this;
    }

    @Override
    public void onEnable() {
        setEnable(false);
    }

    public Color getColor() {
        return new Color(30, 30, 30, alpha.getValue().intValue());
    }
}
