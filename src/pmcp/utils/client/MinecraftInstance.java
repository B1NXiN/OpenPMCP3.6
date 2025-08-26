package pmcp.utils.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import pmcp.event.EventInterface;
import pmcp.event.EventManager;

public class MinecraftInstance implements EventInterface {
    public static final Minecraft mc = Minecraft.getInstance();
    public static final ClientLevel world = Minecraft.getInstance().level;

    public MinecraftInstance() {
        EventManager.instance.register(this);
    }

    public static boolean nullWorld() {
        return mc.level == null || mc.player == null || mc.gameMode == null;
    }

    @Override
    public boolean handleEvents() {
        return true;
    }
}
