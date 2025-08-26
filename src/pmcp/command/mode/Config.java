package pmcp.command.mode;

import pmcp.command.Command;
import pmcp.config.ConfigManager;
import pmcp.utils.client.Helper;

public class Config extends Command {
    public Config() {
        super("config");
    }

    @Override
    public void execute(String[] var1) {
        if (var1.length <= 2) {
            switch (var1[0]) {
                case "load" -> {
                    if (ConfigManager.load(var1[1])) {
                        Helper.sendMessage("Config", "成功登录配置 '" + var1[1] + "'！");
                    } else {
                        Helper.sendMessage("Config", "配置 '" + var1[1] + "' 登录失败，请查看配置是否为可用标准！");
                    }
                }
                case "save" -> {
                    if (ConfigManager.save(var1[1])) {
                        Helper.sendMessage("Config", "配置 '" + var1[1] + "' 保存成功！");
                    } else {
                        Helper.sendMessage("Config", "配置 '" + var1[1] + "' 保存失败！");
                    }
                }
                case "list" -> Helper.sendMessage("Config", (Object) ConfigManager.getList());
            }
        } else {
            Helper.sendMessage("Config", "方法错误，仅支持指令为：load、save、list");
        }
    }
}
