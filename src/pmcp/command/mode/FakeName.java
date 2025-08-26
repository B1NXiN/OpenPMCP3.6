package pmcp.command.mode;

import pmcp.command.Command;
import pmcp.utils.client.Helper;

public class FakeName extends Command {
    public FakeName() {
        super("fakeName");
    }

    @Override
    public void execute(String[] var1) {
        if (!var1[0].isEmpty()) {
            if (!pmcp.mode.exploit.FakeName.Instance.isEnable()) {
                Helper.sendMessage(this.getName(), "你未开启假名称保护！");
            } else {
                pmcp.mode.exploit.FakeName.Instance.fakeName = var1[0];
                Helper.sendMessage(this.getName(), "设置成功，您的新ID为：" + var1[0]);
            }
        } else {
            Helper.sendMessage(this.getName(), "输入文本为空，请输入文本！");
        }
    }
}
