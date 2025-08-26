package pmcp.utils.player;

import pmcp.event.EventTarget;
import pmcp.event.mode.world.EventWorld;
import pmcp.utils.client.MinecraftInstance;

public class SequenceManager extends MinecraftInstance {
    private int current = 0;

    public int next() {
        return ++current;
    }

    public void reset() {
        current = 0;
    }

    public int get() {
        return current;
    }

    @EventTarget
    private void onWorld(EventWorld eventWorld) {
        reset();
    }
}
