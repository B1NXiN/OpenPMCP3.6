package pmcp.utils.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import pmcp.PMCP;

public class Helper extends MinecraftInstance {

    public enum debug {
        A,
        B,
        C,
        D
    }

    public static void sendMessage(Object msg) {
        String message = ChatFormatting.WHITE + "[" + ChatFormatting.YELLOW + PMCP.Instance.getClientName() + ChatFormatting.WHITE + "] ";

        if (Minecraft.getInstance().player != null) {
            mc.gui.getChat().addMessage(Component.literal(message + msg));
        }
    }

    public static void sendMessage(String message, Object msg) {
        message = ChatFormatting.WHITE + "[" + ChatFormatting.YELLOW + message + ChatFormatting.WHITE + "] ";

        if (Minecraft.getInstance().player != null) {
            mc.gui.getChat().addMessage(Component.literal(message + msg));
        }
    }

    public static void sendSystemDeBug(String message, debug debug) {
        switch (debug) {
            case A -> {
                System.out.println("====================提示====================");
                System.out.println("====================" + message);
                System.out.println("====================提示====================");
            }
            case B -> {
                System.out.println("====================警告====================");
                System.out.println("====================" + message);
                System.out.println("====================警告====================");
            }
            case C -> {
                System.out.println("====================问题====================");
                System.out.println("====================" + message);
                System.out.println("====================问题====================");
            }
            case D -> {
                System.out.println("====================日志====================");
                System.out.println("====================" + message);
                System.out.println("====================日志====================");
            }
        }
    }
}
