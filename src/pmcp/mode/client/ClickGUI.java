package pmcp.mode.client;

import com.google.gson.*;
import org.lwjgl.glfw.GLFW;
import pmcp.PMCP;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.utils.category.GuiCategory;
import pmcp.value.values.ModeValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ClickGUI extends Mode {


    public static ClickGUI Instance;

    public ModeValue<language> languageModeValue = new ModeValue<>(this, "语言", language.values(), language.English);

    public enum language {
        Chinese,
        English
    }

    public ClickGUI() {
        super("ClickGUI", "ClickGUI", "调整模块配置的初始界面", Category.CLIENT);
        setKey(GLFW.GLFW_KEY_I);
        Instance = this;
    }

    @Override
    public void onEnable() {
        mc.setScreen(PMCP.Instance.getClickGUI());
        setEnable(false);
    }

}
