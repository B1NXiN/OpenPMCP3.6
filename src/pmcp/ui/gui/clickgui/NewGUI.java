package pmcp.ui.gui.clickgui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import pmcp.config.ConfigManager;
import pmcp.mode.client.ClickGUI;
import pmcp.ui.gui.clickgui.exploit.CategoryBox;
import pmcp.utils.category.Category;
import pmcp.utils.category.GuiCategory;

import java.util.ArrayList;
import java.util.List;

public class NewGUI extends Screen {

    private final List<CategoryBox> categoryBoxes = new ArrayList<>();

    private boolean mouse0;
    private boolean mouse1;

    public NewGUI() {
        super(Component.literal("ClickGUI"));
        categoryBoxes.clear();
    }


    @Override
    protected void init() {
        ConfigManager.save(ConfigManager.configName);
        if (categoryBoxes.isEmpty()) {
            float startX = 10f;

            for (Category category : Category.values()) {
                CategoryBox box = new CategoryBox(category, startX, 5);
                categoryBoxes.add(box);
                startX += box.getWidth() + 2;
            }
        }

        mouse0 = false;
        mouse1 = false;
    }

    @Override
    public void onClose() {
        ConfigManager.save(ConfigManager.configName);
        super.onClose();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.fill(0, 0, width, height, 0x88000000);

        for (CategoryBox box : categoryBoxes) {
            box.render(guiGraphics, mouseX, mouseY, mouse0, mouse1);
            box.doDrag(mouseX, mouseY, mouse0);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        mouse0 = button == 0;
        mouse1 = button == 1;

        for (CategoryBox box : categoryBoxes) {
            if (box.mouseClicked(mouseX, mouseY, button)) return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        float scrollAmount = (float) delta * -10f;

        for (CategoryBox box : categoryBoxes) {
            box.setY(box.getY() + scrollAmount);
        }

        return true;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        mouse0 = false;
        mouse1 = false;

        for (CategoryBox box : categoryBoxes) {
            if (box.mouseReleased(mouseX, mouseY, button)) return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        categoryBoxes.forEach(categoryBox -> categoryBox.keyPressed(pKeyCode, pScanCode, pModifiers));
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }
}