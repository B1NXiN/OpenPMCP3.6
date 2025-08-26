package pmcp.utils.client;

import dev.yalan.irc.IRCClient;
import jnic.JNICInclude;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import pmcp.PMCP;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.Properties;
import java.util.stream.Collectors;

@JNICInclude
public class ClientCredentials {
    private static final Logger logger = LogManager.getLogger("ClientCredentials");

    public static void saveCredentials(String username, String key, String configName) {
        Properties properties = new Properties();
        properties.setProperty("Name", username);
        properties.setProperty("Key", key);
        properties.setProperty("Config", configName);

        try (FileOutputStream out = new FileOutputStream(PMCP.Instance.getCONFIG_FILE())) {
            properties.store(out, "User-Credentials");
        } catch (IOException e) {
            logger.error("Can't save credentials", e);
        }
    }

    public static String[] loadCredentials() {
        Properties properties = new Properties();
        String username = "";
        String key = "";
        String configName = "";

        try (FileInputStream in = new FileInputStream(PMCP.Instance.getCONFIG_FILE())) {
            properties.load(in);
            username = properties.getProperty("Name", "");
            key = properties.getProperty("Key", "");
            configName = properties.getProperty("Config", "");
        } catch (IOException e) {
            logger.error("Can't read credentials", e);
        }

        return new String[]{username, key, configName};
    }

    public static boolean checkKeyWithRemote(String key) {
        try {
            return new BufferedReader(new InputStreamReader(new URL("https://raw.gitcode.com/wssss23/paimeng/raw/main/hwid").openStream()))
                    .lines()
                    .collect(Collectors.joining())
                    .contains(key);
        } catch (Exception e) {
            Helper.sendSystemDeBug("您无法连接至IRC，您的连接已被切断", Helper.debug.C);
            System.exit(-1);
        }
        return false;
    }

    public static void update() {
        try {
            if (!ClientCredentials.checkKeyWithRemote(IRCClient.generateHardwareId())) {
                Helper.sendSystemDeBug("您的HWID->" + IRCClient.generateHardwareId(), Helper.debug.C);
                Helper.sendSystemDeBug("请联系dev上验证", Helper.debug.A);
            }
        } catch (Exception ignored) {
            Helper.sendSystemDeBug("您的HWID出现异常", Helper.debug.C);
            System.exit(-1);
        }
    }
}
