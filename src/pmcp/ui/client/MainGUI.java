package pmcp.ui.client;

import com.mojang.realmsclient.client.RealmsClient;
import dev.yalan.irc.IRCClient;
import dev.yalan.irc.events.IRCAuthenticationResultEvent;
import dev.yalan.irc.netty.IRCProto;
import jnic.JNICInclude;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.main.GameConfig;
import net.minecraft.client.quickplay.QuickPlay;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import pmcp.PMCP;
import pmcp.config.ConfigManager;
import pmcp.event.EventInterface;
import pmcp.event.EventManager;
import pmcp.event.EventTarget;
import pmcp.utils.category.UserCategory;
import pmcp.utils.client.DevHWID;
import pmcp.utils.client.ClientCredentials;
import pmcp.utils.client.Helper;
import pmcp.utils.render.RenderUtils;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.LogEvent;
import pmcp.utils.time.MSTimer;


public class MainGUI extends Screen implements EventInterface {

    private final RealmsClient pRealmsClient;
    private final ReloadInstance pReloadInstance;
    private final GameConfig.QuickPlayData pQuickPlayData;

    public MainGUI(RealmsClient pRealmsClient, ReloadInstance pReloadInstance, GameConfig.QuickPlayData pQuickPlayData) {
        super(Component.nullToEmpty("MainGUI"));

        this.pRealmsClient = pRealmsClient;
        this.pReloadInstance = pReloadInstance;
        this.pQuickPlayData = pQuickPlayData;

        EventManager.instance.register(this);
    }

    public static String s = null;
    public static int i;

    private EditBox keyEdit;
    private EditBox nameEdit;

    private Button loginButton;
    private Button logisButton;
    private boolean rest;
    private String string = "请登录";


    @EventTarget
    private void onIRCAuthenticationResult(IRCAuthenticationResultEvent e) {
        Minecraft.getInstance().execute(() -> {
            this.loginButton.active = true;

            if (e.getResult() == 0) {
                Helper.sendSystemDeBug("登录成功！欢迎：" + nameEdit.getValue(), Helper.debug.A);

                ClientCredentials.saveCredentials(nameEdit.getValue(), keyEdit.getValue(), ConfigManager.configName);
                PMCP.USERNAME = nameEdit.getValue();
                PMCP.KEY = keyEdit.getValue();

                string = "登录成功，欢迎" + nameEdit.getValue();

                if (DevHWID.isDev(IRCClient.INSTANCE.getHardwareId())) {
                    PMCP.userCategory = UserCategory.DEV;
                } else {
                    PMCP.userCategory = UserCategory.FEE;
                }

                this.setTile();
            } else if (e.getResult() == 1) {
                Helper.sendSystemDeBug("登录失败，账号或密码错误！" + "| " + e.getMessage(), Helper.debug.B);
                string = "登录失败";
            } else {
                Helper.sendSystemDeBug("登录出现异常！" + e.getResult() + "| " + e.getMessage(), Helper.debug.C);
                string = "未知登录代码";
            }
        });
    }

    @Override
    protected void init() {
        super.init();

        String[] previousCredentials = ClientCredentials.loadCredentials();

        this.nameEdit = new EditBox(this.font, this.width / 2 - 25, (height / 2 - 10), 125, 20, Component.translatable("账号"));
        this.nameEdit.setValue(previousCredentials[0]);
        this.addWidget(this.nameEdit);

        this.keyEdit = new EditBox(this.font, this.width / 2 - 25, (height / 2 - 10) + 30, 125, 20, Component.translatable("密码"));
        this.keyEdit.setMaxLength(128);
        this.keyEdit.fix = true;
        this.keyEdit.setValue(previousCredentials[1]);
        this.addWidget(this.keyEdit);

        this.loginButton = this.addRenderableWidget(Button.builder(Component.literal("登录"), (p_96030_) -> {
            ClientCredentials.update();

            if (IRCClient.INSTANCE.isChannelOpen()) {
                p_96030_.active = false;
                IRCClient.INSTANCE.sendPacket(IRCProto.createAuthentication(nameEdit.getValue(), keyEdit.getValue(), IRCClient.INSTANCE.getHardwareId()));
            }
        }).bounds((width / 2 - 25) - 75, (height / 2 - 10) + 60, 50, 20).build());


        logisButton = this.addRenderableWidget(Button.builder(Component.literal("注册"), (p_96030_) -> {
            this.string = "已使用默认浏览器跳转注册网页";
            Helper.sendSystemDeBug("欢迎进入注册~", Helper.debug.A);
            Util.getPlatform().openUri("https://reg.pmcp.lol/html?name=register&hardwareId=" + IRCClient.INSTANCE.getHardwareId());
            this.string = "请登录~";
        }).bounds((width / 2 - 25), (height / 2 - 10) + 60, 50, 20).build());


        this.addRenderableWidget(Button.builder(Component.literal("公益入口"), (p_96030_) -> {
            Helper.sendSystemDeBug("欢迎使用公益，正在部署中...", Helper.debug.A);

            PMCP.userCategory = UserCategory.FREE;
            PMCP.USERNAME = "公益用户";
            PMCP.KEY = "114514";

            this.setTile();
        }).bounds((width / 2 - 25) + 75, (height / 2 - 10) + 60, 50, 20).build());

        this.addRenderableWidget(Button.builder(Component.literal("重连"), (p_96030_) -> {
            Helper.sendSystemDeBug("正在为您尝试重连...", Helper.debug.A);
            IRCClient.INSTANCE.connect();
            rest = true;
        }).bounds((width / 2 - 25) + 75, (height / 2 - 10) + 85, 50, 20).build());
    }

    @Override
    public void resize(Minecraft pMinecraft, int pWidth, int pHeight) {
        String s = this.keyEdit.getValue();
        String s1 = this.nameEdit.getValue();
        this.init(pMinecraft, pWidth, pHeight);
        this.keyEdit.setValue(s);
        this.nameEdit.setValue(s1);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        pGuiGraphics.blit(new ResourceLocation("pmcp/png/back.png"), 0, 0, this.width, this.height, 0.0F, 0.0F, this.width, this.height, this.width, this.height);

        RenderUtils.drawRect(pGuiGraphics, (width / 2 - 25) - 85, (height / 2 - 20), 215, 100 + font.lineHeight + 10, new Color(0,0,0,180).getRGB());

        nameEdit.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        keyEdit.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        pGuiGraphics.drawString(font, "账号:", ((width / 2) - 75) - font.width("账号") / 2, (height / 2 - 10) + 5, Color.WHITE.getRGB());
        pGuiGraphics.drawString(font, "密码:", ((width / 2) - 75) - font.width("密码") / 2, (height / 2 - 10) + 35, Color.WHITE.getRGB());

        RenderUtils.drawRect(pGuiGraphics, (width / 2 - 25) - 80, (height / 2 - 20) + 100, font.width("提示: " + string) + 6, font.lineHeight + 6, Color.DARK_GRAY.getRGB());
        pGuiGraphics.drawString(font,"提示: " + string, (width / 2 - 25) - 77, (height / 2 - 20) + 103, Color.WHITE.getRGB());

        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        int logX = 5;
        int logY = 5;
        int lineHeight = font.lineHeight + 2;

        List<String> strings = new ArrayList<>(logBuffer);

        strings.sort((o1, o2) -> font.width(o2) - font.width(o1));

        int width = 6 + font.width(strings.getFirst());

        RenderUtils.drawRect(pGuiGraphics, logX - 3, logY - 3, width, logBuffer.size() * lineHeight + 6, new Color(0, 0, 0, 120).getRGB());

        for (int i = 0; i < logBuffer.size(); i++) {
            String logLine = logBuffer.get(i);
            pGuiGraphics.drawString(font, logLine, logX, logY + i * lineHeight, Color.WHITE.getRGB());
        }
    }

    @Override
    public void tick() {
        keyEdit.tick();
        nameEdit.tick();
        super.tick();

        if (rest) {
            string = "正在重连";
            nameEdit.active = false;
            logisButton.active = false;
            loginButton.active = false;
            keyEdit.active = false;

            if (IRCClient.INSTANCE.isChannelOpen()) {
                Helper.sendSystemDeBug("IRC重连完成！", Helper.debug.A);
                rest = false;

                string = "请操作";
                nameEdit.active = true;
                logisButton.active = true;
                loginButton.active = true;
                keyEdit.active = true;
            }
        }
    }


    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (pScanCode == 1) {
            return false;
        }

        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this);
    }

    @Override
    public void removed() {
        EventManager.instance.unregister(this);
    }

    private void setTile() {
        if (pQuickPlayData.isEnabled()) {
            QuickPlay.connect(Minecraft.getInstance(), pQuickPlayData, pReloadInstance, pRealmsClient);
        } else if (Minecraft.getInstance().options.onboardAccessibility) {
            Minecraft.getInstance().setScreen(new AccessibilityOnboardingScreen(Minecraft.getInstance().options));
        } else {
            Minecraft.getInstance().setScreen(new TitleScreen());
        }

        PMCP.Instance.onEnableClient();
    }

    @Override
    public boolean handleEvents() {
        return true;
    }

    public static final CopyOnWriteArrayList<String> logBuffer = new CopyOnWriteArrayList<>();

    static {
        final Logger rootLogger = (Logger) LogManager.getRootLogger();

        Appender appender = new AbstractAppender("GuiLogAppender", null, null, false, null) {
            @Override
            public void append(LogEvent event) {
                String message = "[" + MSTimer.getTime() + "] " + event.getMessage().getFormattedMessage();
                if (logBuffer.size() >= 10) {
                    logBuffer.removeFirst();
                }
                logBuffer.add(message);
            }
        };
        appender.start();
        rootLogger.addAppender(appender);
    }
}
