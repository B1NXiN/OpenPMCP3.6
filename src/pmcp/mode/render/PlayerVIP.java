package pmcp.mode.render;

import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;
import pmcp.event.EventTarget;
import pmcp.event.mode.update.EventUpdate;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;

public class PlayerVIP extends Mode {
    public PlayerVIP() {
        super("PlayerVIP", "玩家VIP显示", "显示玩家在服务器内的VIP信息", Category.RENDER);
    }

    @EventTarget
    private void onUpdate(EventUpdate eventUpdate) {
        if (mc.player == null || mc.level == null || mc.getConnection() == null) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Player player && player != mc.player) {
                for (PlayerInfo info : mc.getConnection().getOnlinePlayers()) {

                    if (player.getGameProfile().getId().equals(info.getProfile().getId())) {
                        player.setCustomName(getNameForDisplay(info));
                    }
                }
            }
        }
    }

    private Component getNameForDisplay(PlayerInfo p_94550_) {
        return p_94550_.getTabListDisplayName() != null ? this.decorateName(p_94550_, p_94550_.getTabListDisplayName().copy()) : this.decorateName(p_94550_, PlayerTeam.formatNameForTeam(p_94550_.getTeam(), Component.literal(p_94550_.getProfile().getName())));
    }

    private Component decorateName(PlayerInfo pPlayerInfo, MutableComponent pName) {
        return pPlayerInfo.getGameMode() == GameType.SPECTATOR ? pName.withStyle(ChatFormatting.ITALIC) : pName;
    }
}
