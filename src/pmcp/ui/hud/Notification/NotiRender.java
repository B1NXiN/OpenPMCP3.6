package pmcp.ui.hud.Notification;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import pmcp.PMCP;
import pmcp.mode.client.HUDSetting;
import pmcp.ui.font.FontManager;
import pmcp.ui.hud.Notification.NotiBox.NotiCategory;
import pmcp.utils.render.*;
import pmcp.utils.time.MSTimer;

import javax.annotation.Nullable;
import java.awt.*;

public class NotiRender {

    public double animation;
    private final int x;
    private final int y;
    private final FontManager fontManager;
    private String string;
    private final NotiCategory category;
    private final int delay;
    private final int width;
    private final int height;
    private final MSTimer timer;

    public NotiRender(String tile, String content, NotiCategory category, int delay) {
        this.animation = 0.0;
        this.fontManager = PMCP.Instance.getFontManager();
        this.string = "| " + tile + "  [" + content + "]";
        this.timer = new MSTimer();
        this.category = category;
        this.delay = delay;

        this.width = (int) (fontManager.noit20.getStringWidth(category.getTile()) + 6 + fontManager.font16.getStringWidth(string));
        this.height = fontManager.noit20.getHeight() + 4;

        this.x = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 - width / 2;
        this.y = Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 + 20;

        this.timer.reset();
    }

    public void render(@Nullable GuiGraphics guiGraphics, int notiY) {
        if (guiGraphics == null || string == null || category == null) return;

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();
        poseStack.translate((x + (double) width / 2) * (1 - animation), ((y + notiY) + 20) * (1 - animation), 0.0);
        poseStack.scale((float) animation, (float) animation, 0);

        RenderUtils.drawRoundedRect(poseStack, x, (y + notiY), width, height, 2, HUDSetting.Instance.getColor());
        fontManager.noit20.drawString(poseStack,category.getTile(), x + 2, (y + notiY) + 3, Color.WHITE.getRGB());

        fontManager.font16.drawString(poseStack, string, x + fontManager.noit20.getStringWidth(category.getTile()) + 4, (y + notiY) + height / 2f - fontManager.font16.getHeight() / 2f, Color.WHITE.getRGB());

        poseStack.popPose();

    }

    public boolean isRestTimer() {
        return timer.hasTimePassed(delay);
    }

    public boolean isRest() {
        return isRestTimer() && animation == 0;
    }
}
