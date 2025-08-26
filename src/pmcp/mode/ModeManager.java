package pmcp.mode;

import lombok.Getter;
import pmcp.PMCP;
import pmcp.mode.client.ClickGUI;
import pmcp.mode.client.ClientSetting;
import pmcp.mode.client.HUDSetting;
import pmcp.mode.client.IRCSetting;
import pmcp.mode.combat.*;
import pmcp.mode.exploit.AutoDisMode;
import pmcp.mode.exploit.Cape;
import pmcp.mode.exploit.FakeName;
import pmcp.mode.exploit.KillerESP;
import pmcp.mode.hud.*;
import pmcp.mode.misc.AntiBot;
import pmcp.mode.misc.Disabler;
import pmcp.mode.misc.Teams;
import pmcp.mode.move.*;
import pmcp.mode.player.*;
import pmcp.mode.render.*;
import pmcp.mode.world.Scaffold;
import pmcp.mode.world.Stuck;
import pmcp.utils.category.Category;
import pmcp.mode.render.Glow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ModeManager {

    private final List<Mode> modes = new ArrayList<>();

    public void load() {
        switch (PMCP.userCategory) {
            case FEE -> loadFEE();

            case DEV -> {
                loadFEE();
                modes.add(new IRCSetting());
            }
            case FREE -> loadFREE();
        }
    }

    private void loadFEE() {
        // client
        modes.add(new ClickGUI());
        modes.add(new ClientSetting());
        modes.add(new HUDSetting());


        // combat
        modes.add(new ArmorBreak());
        modes.add(new AutoClick());
        modes.add(new KillAura());
        modes.add(new AutoProjecitle());
        modes.add(new BackTrack());
        modes.add(new LegitAttack());
        modes.add(new AutoHit());
        modes.add(new Velocity());
        modes.add(new WTap());


        // exploit
        modes.add(new AutoDisMode());
        modes.add(new Cape());
        modes.add(new FakeName());
        modes.add(new KillerESP());


        // HUD
        modes.add(new Effects());
        modes.add(new DynamicIsLand());
        modes.add(new Armor());
        modes.add(new ModeArray());
        modes.add(new Notification());
        modes.add(new TargetHUD());


        // misc
        modes.add(new Teams());
        modes.add(new AntiBot());
        modes.add(new Disabler());


        // move
        modes.add(new Fly());
        modes.add(new NoJumpDelay());
        modes.add(new NoSlow());
        modes.add(new Speed());
        modes.add(new Sneak());
        modes.add(new NoWeb());
        modes.add(new Sprint());


        // player
        modes.add(new AutoTool());
        modes.add(new ChestStealer());
        modes.add(new AutoPotion());
        modes.add(new AutoPail());
        modes.add(new Blink());
        modes.add(new Eagle());
        modes.add(new FastPlace());
        modes.add(new InvManager());
        modes.add(new LegitPlace());
        modes.add(new NoFall());


        // render
        modes.add(new ESP());
        modes.add(new NameTags());
        modes.add(new CheatESP());
        modes.add(new FullBright());
        modes.add(new Glow());
        modes.add(new PlayerVIP());
        modes.add(new Projectiles());
        modes.add(new EntityHealth());
        modes.add(new TargetMarker());

        // world
        modes.add(new Scaffold());
        modes.add(new Stuck());
    }

    private void loadFREE() {
        // client
        modes.add(new ClickGUI());
        modes.add(new ClientSetting());
        modes.add(new HUDSetting());

        // combat
        modes.add(new AutoClick());
        modes.add(new LegitAttack());
        modes.add(new WTap());

        // exploit
        modes.add(new AutoDisMode());
        modes.add(new KillerESP());
        modes.add(new FakeName());
        modes.add(new Cape());

        // HUD
        modes.add(new Effects());
        modes.add(new Armor());
        modes.add(new DynamicIsLand());
        modes.add(new ModeArray());
        modes.add(new Notification());
        modes.add(new TargetHUD());

        // misc
        modes.add(new Teams());
        modes.add(new Disabler());

        // move
        modes.add(new NoJumpDelay());
        modes.add(new Sprint());

        // player
        modes.add(new AutoTool());
        modes.add(new AutoPotion());
        modes.add(new ChestStealer());
        modes.add(new Blink());
        modes.add(new Eagle());
        modes.add(new LegitPlace());

        // render
        modes.add(new ESP());
        modes.add(new Glow());
        modes.add(new FullBright());
        modes.add(new CheatESP());
        modes.add(new NameTags());
        modes.add(new PlayerVIP());
        modes.add(new Projectiles());
        modes.add(new EntityHealth());
        modes.add(new TargetMarker());


        // world
    }

    public List<Mode> getEnableMods() {
        return modes.stream().filter(Mode::isEnable).collect(Collectors.toList());
    }


    public void onKey(int key) {

        for (Mode enableMod : modes) {
            if (enableMod.getKey() == key) {
                enableMod.setEnable(!enableMod.isEnable());
            }
        }
    }

    public Mode getByName(String name) {
        for (Mode mod : modes) {
            if (name.equalsIgnoreCase(mod.getModeName())) {
                return mod;
            }
        }
        return null;
    }

    public Mode getByClass(Class<? extends Mode> modClass) {
        for (Mode mod : modes) {
            if (mod.getClass() == modClass) {
                return mod;
            }
        }
        return null;
    }

    public Mode getByCategory(Category category) {
        for (Mode mod : modes) {
            if (mod.getCategory() == category) {
                return mod;
            }
        }
        return null;
    }



    public void addClass(Mode mode) {
        modes.add(mode);
    }
}
