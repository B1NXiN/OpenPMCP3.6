package pmcp.mode.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import pmcp.PMCP;
import pmcp.event.EventTarget;
import pmcp.event.mode.render.EventRender2D;
import pmcp.mode.Mode;
import pmcp.mode.client.HUDSetting;
import pmcp.ui.font.FontManager;
import pmcp.utils.category.Category;
import pmcp.utils.render.RenderUtils;
import pmcp.value.values.BooleanValue;
import pmcp.value.values.ModeValue;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ModeArray extends Mode {

    private final Map<Mode, Float> alphaMap = new ConcurrentHashMap<>();
    private final BooleanValue<Boolean> colorful = new BooleanValue<>(this, "Colorful", false);

    public ModeArray() {
        super("ModeArray", "模块列表", "Displays enabled modules", Category.HUD);
    }

    @EventTarget
    private void onRender2D(EventRender2D render2D) {
        List<Mode> modes = PMCP.Instance.getModeManager().getEnableMods();
        FontManager font = PMCP.Instance.getFontManager();

        if (modes.isEmpty() || mc.player == null || mc.level == null) return;


        if (render2D.getType() == EventRender2D.Type.PRE) {

            List<Mode> modeList = new ArrayList<>(modes);
            modeList.removeIf(mode -> switch (mode.getCategory()) {
                case HUD, RENDER, EXPLOIT, CLIENT -> true;
                default -> false;
            });
            if (modeList.isEmpty()) return;

            // 按宽度排序
            modeList.sort((a, b) -> {
                String s1 = a.getName() + (a.getSuffix().isEmpty() ? "" : " [" + a.getSuffix() + "]");
                String s2 = b.getName() + (b.getSuffix().isEmpty() ? "" : " [" + b.getSuffix() + "]");
                return Float.compare(font.font18.getStringWidth(s2), font.font18.getStringWidth(s1));
            });

            int y = 30;
            int paddingX = 6;
            int paddingY = 4;
            int spacing = 2;
            int index = 0;

            for (Mode mode : modeList) {
                float current = alphaMap.getOrDefault(mode, 0f);
                float target = mode.isEnable() ? 1f : 0f;
                float lerped = RenderUtils.lerp(current, target, 0.1f);
                alphaMap.put(mode, lerped);

                if (lerped <= 0.01f) continue;

                String name = mode.getName();
                String suffix = mode.getSuffix();
                boolean hasSuffix = !suffix.isEmpty();
                String fullText = name + (hasSuffix ? " [" + suffix + "]" : "");

                float textWidth = font.font18.getStringWidth(fullText);
                float boxX = render2D.getGuiGraphics().guiWidth() - textWidth - paddingX * 2 - 10;
                float boxY = y - paddingY;
                float boxW = textWidth + paddingX * 2;
                float boxH = font.font18.getHeight() + paddingY * 2;

                Color bg = new Color(30, 30, 30, (int) (120 * lerped));
                RenderUtils.drawRoundedRect(render2D.getGuiGraphics().pose(), boxX + 6, boxY, boxW, boxH, 4f, bg);

                Color nameColor = colorful.getValue() ? getPositionDynamicGradientColor(index, modeList.size(), lerped) : new Color(80, 255, 120, (int) (255 * lerped));
                Color suffixColor = new Color(180, 180, 180, (int) (255 * lerped));

                float nameW = font.font18.getStringWidth(name);
                float suffixW = hasSuffix ? font.font18.getStringWidth(" [" + suffix + "]") : 0;

                font.font18.drawString(render2D.getGuiGraphics().pose(), name, render2D.getGuiGraphics().guiWidth() - nameW - suffixW - 10, y, nameColor.getRGB());
                if (hasSuffix) {
                    font.font18.drawString(render2D.getGuiGraphics().pose(), " [" + suffix + "]", render2D.getGuiGraphics().guiWidth() - suffixW - 10, y, suffixColor.getRGB());
                }

                y += boxH + spacing;
                index++;
            }
        }
    }

    private Color getPositionDynamicGradientColor(int index, int total, float alpha) {
        float scroll = (System.currentTimeMillis() % 5000L) / 5000f; // 动态偏移：5秒周期
        float t = total <= 1 ? 0f : ((float) index / (float) (total - 1) + scroll) % 1.0f;
        t = easeInOutCubic(t);

        Color from = new Color(80, 255, 120); // 绿色
        Color to = new Color(255, 255, 255);  // 白色

        int r = (int)(from.getRed() + (to.getRed() - from.getRed()) * t);
        int g = (int)(from.getGreen() + (to.getGreen() - from.getGreen()) * t);
        int b = (int)(from.getBlue() + (to.getBlue() - from.getBlue()) * t);

        return new Color(r, g, b, (int)(alpha * 255));
    }

    private float easeInOutCubic(float x) {
        return x < 0.5 ? 4 * x * x * x : 1 - (float)Math.pow(-2 * x + 2, 3) / 2;
    }
}

