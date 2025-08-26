package pmcp.command.mode;

import pmcp.command.Command;
import pmcp.utils.client.Helper;
import pmcp.utils.player.FriendManager;

public class Friend extends Command {
    public Friend() {
        super("friend");
    }

    @Override
    public void execute(String[] var1) {
        if (var1.length == 2) {
            switch (var1[0]) {
                case "add", "a" -> {
                    FriendManager.addPlayer(var1[1]);
                    Helper.sendMessage("Friend", "添加玩家 '" + var1[1] + "' 为好友成功！");
                }
                case "remove", "r" -> {
                    FriendManager.removePlayer(var1[1]);
                    Helper.sendMessage("Friend", "移除好友 '" + var1[1] + "' 成功！");
                }
                case "list", "l" -> Helper.sendMessage("Friend", String.join("| ", FriendManager.getPlayerNames()) + "]");
            }
        }
    }
}
