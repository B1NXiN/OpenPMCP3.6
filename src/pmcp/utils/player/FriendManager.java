package pmcp.utils.player;

import net.minecraft.world.entity.LivingEntity;
import pmcp.PMCP;
import pmcp.mode.misc.Teams;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendManager {

    private static final List<String> playerNames = new ArrayList<>();

    public static void addPlayer(String name) {
        playerNames.add(name);
    }

    public static void addPlayer(LivingEntity livingEntity) {
        playerNames.add(livingEntity.getName().getString());
    }

    public static void removePlayer(String name) {
        if (playerNames.isEmpty()) return;

        playerNames.removeIf(string -> Objects.equals(string, name));
    }

    public static boolean isFriend(String name) {
        if (playerNames.isEmpty()) return false;

        for (String string : playerNames) {
            if (!string.equals(name)) continue;

            return true;
        }

        return false;
    }

    public static List<String> getPlayerNames() {

        return new ArrayList<>(playerNames);
    }
}
