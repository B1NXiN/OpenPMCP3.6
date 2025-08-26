package pmcp.ui.hud.Notification.NotiBox;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import pmcp.ui.hud.Notification.NotiRender;
import pmcp.utils.render.Animation;
import pmcp.utils.render.AnimationUtils;
import pmcp.utils.render.Direction;
import pmcp.utils.render.EaseBackIn;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class NotiManager {

    public static final List<NotiRender> notiRenderList = new ArrayList<>();

    public static void addNoti(String tile, String content, NotiCategory category, int delay) {
        if (Minecraft.getInstance() != null && Minecraft.getInstance().player != null && Minecraft.getInstance().level != null) {
            notiRenderList.add(new NotiRender(tile, content, category, delay));
        }
    }

    public static void update(GuiGraphics guiGraphics) {
        if (notiRenderList.isEmpty()) return;

        Iterator<NotiRender> iterator = notiRenderList.iterator();

        int notiY = 0;
        while (iterator.hasNext()) {
            NotiRender notiRender = iterator.next();
            if (notiRender.isRest()) {
                iterator.remove();
            } else {
                if (!notiRender.isRestTimer()) {
                    notiRender.animation = AnimationUtils.smooth(1, notiRender.animation, 0.01);
                } else {
                    notiRender.animation = AnimationUtils.smooth(0, notiRender.animation, 0.01);
                }

                notiRender.render(guiGraphics, notiY);
                notiY += 16;
            }
        }
    }

    public static void close() {
        notiRenderList.clear();
    }
}

