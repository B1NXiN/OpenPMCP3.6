package pmcp.mode.hud;

import net.minecraft.client.gui.Font;
import pmcp.event.EventTarget;
import pmcp.event.mode.render.EventRender2D;
import pmcp.event.mode.update.EventTick;
import pmcp.event.mode.world.EventWorld;
import pmcp.mode.Mode;
import pmcp.ui.hud.Notification.NotiBox.NotiManager;
import pmcp.utils.category.Category;

public class Notification extends Mode {
    public Notification() {
        super("Notification", "模块提示", "显示客户端内的一些模块启用提示或者其他类型提示", Category.HUD);
    }

    @EventTarget
    public void onRender(EventRender2D eventRender2D) {
        if (mc.level == null || mc.player == null) return;

        if (eventRender2D.getType() == EventRender2D.Type.POST) {
            NotiManager.update(eventRender2D.getGuiGraphics());
        }
    }

    @EventTarget
    public void onWorld(EventWorld eventWorld) {
        NotiManager.close();
    }
}
