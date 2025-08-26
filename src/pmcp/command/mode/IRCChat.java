package pmcp.command.mode;

import dev.yalan.irc.IRCClient;
import dev.yalan.irc.netty.IRCProto;
import pmcp.command.Command;
import pmcp.utils.client.Helper;

public class IRCChat extends Command {
    public IRCChat() {
        super("i");
    }

    @Override
    public void execute(String[] args) {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < args.length; i++) {
            sb.append(args[i]);

            if (i + 1 != args.length) {
                sb.append(' ');
            }
        }


        if (IRCClient.INSTANCE.sendPacket(IRCProto.createChat(sb.toString())) == null) {
            Helper.sendMessage(this.getName(), "您与IRC为建立任何连接！");
        }
    }
}
