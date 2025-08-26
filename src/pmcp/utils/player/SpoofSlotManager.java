package pmcp.utils.player;

import net.minecraft.world.item.ItemStack;
import pmcp.utils.client.MinecraftInstance;

public class SpoofSlotManager extends MinecraftInstance {

    private static int slot = -1;

    public static ItemStack getSlotItem() {
        return mc.player.containerMenu.getSlot(slot + 36).getItem();
    }

    public static void setSlot(int slot) {
        SpoofSlotManager.slot = slot;
    }

    public static boolean isSpoof() {
        return getSlot() != -1;
    }

    public static void stop() {
        SpoofSlotManager.slot = -1;
    }

    public static int getSlot() {
        return SpoofSlotManager.slot;
    }
}
