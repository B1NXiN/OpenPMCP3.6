package pmcp.command.mode;


import pmcp.PMCP;
import pmcp.command.Command;
import pmcp.mode.Mode;
import pmcp.utils.client.Helper;

import java.awt.event.KeyEvent;
import java.util.Objects;

public class Binds extends Command {
    public Binds() {
        super("Binds");
    }

    @Override
    public void execute(String[] var1) {
        for (Mode module : PMCP.Instance.getModeManager().getModes()) {
            String key = module.getKey() == 0 ? "NONE" : KeyEvent.getKeyText(module.getKey());
            if (!Objects.equals(key, "NONE")) {
                Helper.sendMessage("Binds",module.getName() + ": " + key);
            }
        }
    }
}
