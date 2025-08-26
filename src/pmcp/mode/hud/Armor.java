package pmcp.mode.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import pmcp.event.EventTarget;
import pmcp.event.mode.render.EventRender2D;
import pmcp.mode.Mode;
import pmcp.mode.client.HUDSetting;
import pmcp.utils.category.Category;
import pmcp.utils.render.RenderUtils;
import pmcp.value.values.NumberValue;

import java.awt.*;

public class Armor extends Mode {
    public Armor() {
        super("Armor", "盔甲", "显示你身上的盔甲", Category.HUD);
    }

    @EventTarget
    private void onRender2D(EventRender2D eventRender2D) {
        GuiGraphics guiGraphics = eventRender2D.getGuiGraphics();

        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        if (eventRender2D.getType() == EventRender2D.Type.PRE) {
            int x = eventRender2D.getGuiGraphics().guiWidth() / 2 + 10;
            int y = eventRender2D.getGuiGraphics().guiHeight() - 60;

            int w = 2;

            for (int i = 5; i < 9; i++) {
                ItemStack stack = mc.player.containerMenu.getSlot(i).getItem();

                if (!stack.isEmpty()) {
                    w += 16;
                }
            }

            if (!mc.player.getMainHandItem().isEmpty()) {
                w += 16;
            }

            int h = 18;


            RenderUtils.drawRoundedRect(guiGraphics.pose(), x, y, w + 2, h + 2, 2, HUDSetting.Instance.getColor());
            RenderUtils.drawRect(guiGraphics, x, y, w + 2, 2, new Color(0x56CC9D).getRGB());

            int xW = 0;
            for (int i = 5; i < 9; i++) {
                ItemStack stack = mc.player.containerMenu.getSlot(i).getItem();

                if (!stack.isEmpty()) {
                    RenderUtils.renderItemIcon(guiGraphics.pose(), x + 2 + xW, y + 2, stack);
                    xW += 16;
                }
            }

            if (!mc.player.getMainHandItem().isEmpty()) {
                RenderUtils.renderItemIcon(guiGraphics.pose(), x + 2 + xW, y + 2, mc.player.getMainHandItem());
            }
        }
    }
}
