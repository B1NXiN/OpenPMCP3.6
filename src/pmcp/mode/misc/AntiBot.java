package pmcp.mode.misc;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import pmcp.event.EventTarget;
import pmcp.event.mode.update.EventUpdate;
import pmcp.event.mode.world.EventWorld;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.utils.client.Helper;

import java.util.ArrayList;
import java.util.List;

public class AntiBot extends Mode {

    public static AntiBot Instance;

    public AntiBot() {
        super("AntiBot", "反假人", "自动清除身边假人", Category.MISC);
        Instance = this;
    }

    public final List<Entity> entityList = new ArrayList<>();

    @Override
    public void onDisable() {
        entityList.clear();
    }

    @EventTarget
    private void onUpdate(EventUpdate eventUpdate) {
        if (mc.player == null || mc.level == null) return;

        for (Entity entity : mc.level.entitiesForRendering()) {

            if (entityList.contains(entity)) continue;

            if (entity == mc.player) continue;

            if (entity instanceof Player pPlayer) {
                if (mc.player.distanceTo(pPlayer) < 3 && pPlayer.getYRot() == 0.0 && pPlayer.getXRot() == 0.0) {
                    pPlayer.ticks++;

                    if (pPlayer.ticks > 5) {
                        Helper.sendMessage(this.getName(), "移除假人ID：" + pPlayer.getId());
                        entityList.add(pPlayer);
                        mc.level.onEntityRemoved(pPlayer);
                    }
                }
            }
        }
    }

    @EventTarget
    private void onWorld(EventWorld eventWorld) {
        entityList.clear();
    }
}
