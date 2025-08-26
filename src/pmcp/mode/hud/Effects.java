package pmcp.mode.hud;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.alchemy.Potion;
import org.checkerframework.checker.units.qual.C;
import pmcp.PMCP;
import pmcp.event.EventTarget;
import pmcp.event.mode.render.EventRender2D;
import pmcp.mode.Mode;
import pmcp.mode.client.HUDSetting;
import pmcp.ui.font.FontManager;
import pmcp.utils.category.Category;
import pmcp.utils.render.RenderUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Effects extends Mode {
    public Effects() {
        super("Effects", "药水效果显示", "显示你身上的药水效果", Category.HUD);
    }


    private final Map<String, Integer> potionMaxDurations = new HashMap<>();

    @EventTarget
    private void onRender2D(EventRender2D render2D) {
        if (mc.player == null) return;

        if (render2D.getType() == EventRender2D.Type.PRE) {
            GuiGraphics guiGraphics = render2D.getGuiGraphics();
            List<MobEffectInstance> effects = new ArrayList<>(mc.player.getActiveEffects());
            if (effects.isEmpty()) return;

            int centerY = mc.getWindow().getGuiScaledHeight() / 2 - 30;
            int totalHeight = effects.size() * 40;
            int y = centerY - totalHeight / 2;

            for (MobEffectInstance effect : effects) {
                if (potionMaxDurations.get(effect.getDescriptionId()) == null || potionMaxDurations.get(effect.getDescriptionId()) < effect.getDuration()) {
                    potionMaxDurations.put(effect.getDescriptionId(), effect.getDuration());
                }

                renderEffect(guiGraphics, effect, 5, y);
                y += 26;
            }
        }
    }

    public void renderEffect(GuiGraphics guiGraphics, MobEffectInstance effect, int x, int y) {
        FontManager fontManager = PMCP.Instance.getFontManager();
        int duration = effect.getDuration();

        String s = formatDuration(duration);

        int w = 22 + (int) fontManager.font20.getStringWidth(s);

        Color color = new Color(effect.getEffect().getColor());

        RenderUtils.drawRoundedRect(guiGraphics.pose(), x - 1, y, w + 1, 20, 3, HUDSetting.Instance.getColor());
        RenderUtils.drawRect(guiGraphics, x -2, y, 3, 20, color.getRGB());

        TextureAtlasSprite sprite = mc.getMobEffectTextures().get(effect.getEffect());
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
        guiGraphics.blit(x + 2, y + 2, 0, 16, 16, sprite);

        fontManager.font20.drawString(guiGraphics.pose(),s, x + 20, y + 20 / 2f - fontManager.font20.getHeight() / 2f, Color.WHITE.getRGB());
    }

    private static String toRoman(int num) {
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length && num > 0; i++) {
            while (values[i] <= num) {
                num -= values[i];
                sb.append(symbols[i]);
            }
        }
        return sb.toString();
    }

    private static String formatDuration(int duration) {
        int seconds = duration / 20;
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
