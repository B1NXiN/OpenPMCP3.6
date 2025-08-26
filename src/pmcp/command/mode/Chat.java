package pmcp.command.mode;

import net.minecraft.client.Minecraft;
import pmcp.command.Command;

public class Chat extends Command {
    public Chat() {
        super("chat");
    }

    @Override
    public void execute(String[] var1) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.connection.sendChat(var1[0]);
        }
    }
}
