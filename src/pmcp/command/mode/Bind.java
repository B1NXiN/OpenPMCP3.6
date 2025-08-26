package pmcp.command.mode;

import org.lwjgl.glfw.GLFW;
import pmcp.PMCP;
import pmcp.command.Command;
import pmcp.mode.Mode;
import pmcp.utils.client.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

public class Bind extends Command {

    private final Map<String, Integer> bindMap = new HashMap<>();

    public Bind() {
        super("bind");

        bindMap.put("none", 0);
        bindMap.put("rshift", GLFW.GLFW_KEY_RIGHT_SHIFT);
        bindMap.put("lshift", GLFW.GLFW_KEY_LEFT_SHIFT);
        bindMap.put("rctrl", GLFW.GLFW_KEY_RIGHT_CONTROL);
        bindMap.put("lctrl", GLFW.GLFW_KEY_LEFT_CONTROL);
        bindMap.put("ralt", GLFW.GLFW_KEY_RIGHT_ALT);
        bindMap.put("lalt", GLFW.GLFW_KEY_LEFT_ALT);

        for (Field field : GLFW.class.getFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getName().startsWith("GLFW_KEY_")) {
                field.setAccessible(true);
                try {
                    String keyName = field.getName().substring(9).toLowerCase();
                    bindMap.put(keyName, (Integer) field.get(null));
                } catch (IllegalAccessException ignored) {
                }
            }
        }
    }

    @Override
    public void execute(String[] params) {
        if (params.length == 2) {
            String keyName = params[1].trim().toLowerCase();
            String modName = params[0].trim().toLowerCase();

            Mode module = PMCP.Instance.getModeManager().getByName(params[0].replaceAll(" ",""));

            if (module != null) {
                Integer key = bindMap.get(keyName);
                if (key != null) {
                    module.setKey(key);
                    Helper.sendMessage("Bind", modName + " 绑定至 " + keyName);
                } else {
                    module.setKey(0);
                    Helper.sendMessage("Bind", modName + " 已移除绑定");
                }
            } else {
                Helper.sendMessage("Bind", modName + " 不存在");
            }
        }
    }
}
