package pmcp;

import com.mojang.logging.LogUtils;
import dev.yalan.irc.IRCHelper;
import jnic.JNICInclude;
import lombok.Getter;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;

import pmcp.command.CommandManager;
import pmcp.config.ConfigManager;
import pmcp.event.EventManager;
import pmcp.mode.Mode;
import pmcp.mode.ModeManager;
import pmcp.ui.font.FontManager;
import pmcp.ui.gui.clickgui.NewGUI;
import pmcp.utils.category.UserCategory;
import pmcp.utils.client.ClientCredentials;
import pmcp.utils.client.Helper;
import pmcp.utils.client.MinecraftInstance;
import pmcp.utils.player.PacketUtils;
import pmcp.utils.player.SequenceManager;
import pmcp.utils.player.SpoofSlotManager;
import pmcp.utils.resource.ResourcesManager;
import pmcp.utils.rotation.SilentRotation;
import pmcp.utils.time.MSTimer;


@Getter
@JNICInclude
public class PMCP {

    public static PMCP Instance;

    private static final Logger LOGGER = LogUtils.getLogger();

    public final String clientName = "PMCP";
    public final String clientVersion = "v3.6";
    private Screen clickGUI;

    private final String CONFIG_FILE = "UserConfig.properties";
    public static UserCategory userCategory = null;
    public static String USERNAME;
    public static String KEY;

    private MinecraftInstance minecraftInstance;
    private SequenceManager sequenceManager;
    private CommandManager commandManager;
    private ConfigManager configManager;
    private ModeManager modeManager;
    private PacketUtils packetUtils;
    private FontManager fontManager;
    private IRCHelper ircHelper;
    private ResourcesManager resourcesManager;
    private SpoofSlotManager spoofSlotManager;
    private final MSTimer timer = new MSTimer();


    public void onEnableClient() {
        LOGGER.debug(clientName + "-" + clientVersion + "_1.20.1");

        minecraftInstance = new MinecraftInstance();
        sequenceManager = new SequenceManager();
        modeManager = new ModeManager();
        packetUtils = new PacketUtils();
        commandManager = new CommandManager();
        configManager = new ConfigManager();
        spoofSlotManager = new SpoofSlotManager();
        resourcesManager = new ResourcesManager();
        fontManager = new FontManager();
        ircHelper = new IRCHelper();

        clickGUI = new NewGUI();
        EventManager.instance.register(new SilentRotation());

        resourcesManager.init();
        modeManager.load();
        commandManager.init();
        configManager.init();
        fontManager.init();
        timer.reset();

        String[] previousCredentials = ClientCredentials.loadCredentials();

        if (!previousCredentials[2].isEmpty()) {
            if (ConfigManager.load(previousCredentials[2])) {
                //
            } else {
                configManager.loadAllConfig();
            }
        } else {
            configManager.loadAllConfig();
        }

        if (USERNAME.isEmpty()) {
            Helper.sendSystemDeBug("你不可能在没有名字的情况下进行游玩", Helper.debug.B);
            System.exit(-1);
        } else {

        }
    }

    public void onDisableClient() {
        LOGGER.debug(clientName + "-" + clientVersion + "_ close client");

        if (PMCP.USERNAME != null && !PMCP.USERNAME.isEmpty()) {
            ConfigManager.save(ConfigManager.configName);
            ClientCredentials.saveCredentials(PMCP.USERNAME, PMCP.KEY, ConfigManager.configName);
        }
    }

    public Mode getMode(Class<? extends Mode> modClass) {
        return modeManager.getByClass(modClass);
    }

    public Mode getMode(String modeName) {
        return modeManager.getByName(modeName);
    }
}
