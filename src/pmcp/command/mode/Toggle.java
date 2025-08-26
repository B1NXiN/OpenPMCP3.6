package pmcp.command.mode;

import net.minecraft.ChatFormatting;
import pmcp.PMCP;
import pmcp.command.Command;
import pmcp.mode.Mode;
import pmcp.utils.client.Helper;

public class Toggle extends Command {

    public Toggle() {
        super("toggle");
    }

    @Override
    public void execute(String[] var1) {
        boolean found = false;

        Mode m = PMCP.Instance.getModeManager().getByName(var1[0].replaceAll(" ",""));

        if (m != null) {
            m.setEnable(!m.isEnable());
            found = true;
            if (m.isEnable()) {
                Helper.sendMessage(m.getModeName() + ChatFormatting.GRAY + " 设置为" + ChatFormatting.GREEN + " 打开的");
            } else {
                Helper.sendMessage(m.getModeName() + ChatFormatting.GRAY + " 设置为" + ChatFormatting.RED + " 关闭的");
            }
        }
        if (!found) {
            Helper.sendMessage("模块：" + ChatFormatting.RED + var1[0] + ChatFormatting.GRAY + " 无效！");
        }
    }
}
