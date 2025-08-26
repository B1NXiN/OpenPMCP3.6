package pmcp.utils.heypixel;

import net.minecraft.ChatFormatting;

import java.util.HashMap;
import java.util.Map;

public class VIPUtils {
    public static final Map<Character, String> vips = new HashMap<>();
    public static final Map<Character, String> vips2 = new HashMap<>();

    static {
        vips.put('\ue0f8', "vip1");
        vips.put('\ue0f9', "vip2");
        vips.put('\ue0fa', "vip3");
        vips.put('\ue0fb', "vip4");
        vips.put('\ue0fc', "vip5");
        vips.put('\ue0fd', "vip6");
        vips.put('\ue0fe', "vip7");
        vips.put('\ue0ff', "vip8");

        vips2.put('\ue0f8', "[" + ChatFormatting.GRAY + "VIP1" + ChatFormatting.WHITE + "]" + ChatFormatting.RESET);
        vips2.put('\ue0f9', "[" + ChatFormatting.AQUA + "VIP2" + ChatFormatting.WHITE + "]" + ChatFormatting.RESET);
        vips2.put('\ue0fa', "[" + ChatFormatting.BLUE + "VIP3" + ChatFormatting.WHITE + "]" + ChatFormatting.RESET);
        vips2.put('\ue0fb', "[" + ChatFormatting.GREEN + "VIP4" + ChatFormatting.WHITE + "]" + ChatFormatting.RESET);
        vips2.put('\ue0fc', "[" + ChatFormatting.DARK_PURPLE + "VIP5" + ChatFormatting.WHITE + "]" + ChatFormatting.RESET);
        vips2.put('\ue0fd', "[" + ChatFormatting.LIGHT_PURPLE + "VIP6" + ChatFormatting.WHITE + "]" + ChatFormatting.RESET);
        vips2.put('\ue0fe', "[" + ChatFormatting.RED + "VIP7" + ChatFormatting.WHITE + "]" + ChatFormatting.RESET);
        vips2.put('\ue0ff', "[" + ChatFormatting.GOLD + "VIP8" + ChatFormatting.WHITE + "]" + ChatFormatting.RESET);
    }

    public static String replaceVIPChars(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            result.append(vips2.getOrDefault(c, String.valueOf(c)));
        }
        return result.toString();
    }

    public static String replaceVIP(String input) {
        if (input == null) return "";

        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (vips.containsKey(c)) {
                result.append(vips.get(c));
            }
        }
        return result.toString();
    }
}
