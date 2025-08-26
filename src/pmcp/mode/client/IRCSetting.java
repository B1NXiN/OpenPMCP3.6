package pmcp.mode.client;

import dev.yalan.irc.IRCClient;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.value.values.BooleanValue;

public class IRCSetting extends Mode {

    public static IRCSetting Instance;

    public final BooleanValue<Boolean> ircFiend = new BooleanValue<>(this, "IRC互不攻击", true);

    public IRCSetting() {
        super("IRCSetting", "IRC部分设置", "设置IRC的一些东西", Category.CLIENT);
        Instance = this;
    }

    @Override
    public void onEnable() {
        setEnable(false);
    }
}
