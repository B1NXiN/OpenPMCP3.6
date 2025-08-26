package pmcp.mode;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import pmcp.PMCP;
import pmcp.event.EventInterface;
import pmcp.event.EventManager;
import pmcp.mode.client.ClickGUI;
import pmcp.ui.hud.Notification.NotiBox.NotiCategory;
import pmcp.ui.hud.Notification.NotiBox.NotiManager;
import pmcp.utils.category.Category;
import pmcp.utils.client.MinecraftInstance;
import pmcp.value.Value;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class Mode implements EventInterface {

    private final String modeName;
    private final String modeChineseName;
    private final String modeDescribes;
    private final Category category;

    public Minecraft mc = MinecraftInstance.mc;
    public ClientLevel world = MinecraftInstance.world;

    private boolean enable = false;
    @Setter
    private String suffix = "";
    @Setter
    private int key;

    private final List<Value<?>> values = new ArrayList<>();

    public Mode(String modeName, String modeChineseName, String modeDescribes, Category category) {
        this.modeName = modeName;
        this.modeChineseName = modeChineseName;
        this.modeDescribes = modeDescribes;
        this.category = category;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;

        if (enable) {
            onEnable();
            NotiManager.addNoti("Enable", this.getName(), NotiCategory.OPEN, 2000);
            if (mc.level != null && mc.player != null) mc.level.playSound(mc.player, mc.player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER);
            EventManager.instance.register(this);
        } else {
            onDisable();
            NotiManager.addNoti("Disable", this.getName(), NotiCategory.CLOSE, 2000);
            if (mc.level != null && mc.player != null) mc.level.playSound(mc.player, mc.player.blockPosition(), SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.MASTER);
            EventManager.instance.unregister(this);
        }
    }


    public String getName() {
        ClickGUI clickGUI = (ClickGUI) PMCP.Instance.getModeManager().getByClass(ClickGUI.class);

        if (clickGUI.languageModeValue.getValue().equals(ClickGUI.language.Chinese)) {
            return this.modeChineseName;
        }

        return this.modeName;
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void key(int key) {
    }

    public void addValues(Value<?>... values) {
        Collections.addAll(this.values, values);
    }

    @Override
    public boolean handleEvents() {
        return true;
    }
}
