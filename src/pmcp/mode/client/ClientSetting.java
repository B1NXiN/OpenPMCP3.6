package pmcp.mode.client;

import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.value.values.BooleanValue;
import pmcp.value.values.NumberValue;

public class ClientSetting extends Mode {

    public static ClientSetting Instance;

    public final BooleanValue<Boolean> heypixelWorldValue = new BooleanValue<>(this, "Heypixel_World-Fix", true);
    public final BooleanValue<Boolean> heypixelSinkaValue = new BooleanValue<>(this, "Heypixel_Skin-Fix", true);
    public final BooleanValue<Boolean> heypixelGameprofileValue = new BooleanValue<>(this, "Heypixel_Gameprofile-Fix", true);

    public final BooleanValue<Boolean> physical = new BooleanValue<>(this, "物理掉落", false);

    public ClientSetting() {
        super("ClientSetting", "客户端多功能设置", "对你的客户端进行一些微调，例如改变原效果", Category.CLIENT);
        Instance = this;
    }

    @Override
    public void onEnable() {
        setEnable(false);

    }
}
