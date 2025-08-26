package pmcp.command.mode;

import dev.yalan.irc.IRCClient;
import dev.yalan.irc.netty.IRCProto;
import pmcp.PMCP;
import pmcp.command.Command;
import pmcp.utils.category.UserCategory;
import pmcp.utils.client.Helper;

public class IRCKick extends Command {
    public IRCKick() {
        super("ikick");
    }

    @Override
    public void execute(String[] args) {
        if (args.length < 1) {
            Helper.sendMessage(".ikick <username> <reason (optional)>");
            return;
        }

        if (PMCP.userCategory != UserCategory.DEV) {
            Helper.sendMessage("无法使用该命令");
            return;
        }

        final String username = args[0];
        final String reason;

        if (args.length == 1) {
            reason = "";
        } else {
            final StringBuilder sb = new StringBuilder();

            for (int i = 1; i < args.length; i++) {
                sb.append(args[i]);

                if (i + 1 != args.length) {
                    sb.append(' ');
                }
            }

            reason = sb.toString();
        }

        IRCClient.INSTANCE.sendPacket(IRCProto.createKickMC(username, reason));
    }
}
