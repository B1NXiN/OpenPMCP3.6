package pmcp.mode.render;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Scoreboard;
import pmcp.event.EventTarget;
import pmcp.event.mode.update.EventTick;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.utils.client.Helper;

public class EntityHealth extends Mode {


    public EntityHealth() {
        super("EntityHealth", "实体血条修复", "修复假血条问题", Category.RENDER);
    }

    @EventTarget
    public void onTick(EventTick eventTick) {
        if (mc.player == null || mc.level == null) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof Player player && player != mc.player) {
                float health = getHealth(player);

                if (health == 0 && !player.isRemoved()) continue;

                player.setHealth(health);
            }
        }
    }

    public static float getHealth(LivingEntity target) {
        if (target.getHealth() > 0.6 || target.isRemoved() || target.getHealth() == 0) {
            return target.getHealth();
        }

        Scoreboard scoreboard = target.level().getScoreboard();
        Objective objective = scoreboard.getDisplayObjective(Scoreboard.DISPLAY_SLOT_BELOW_NAME);

        return objective != null ? scoreboard.getOrCreatePlayerScore(target.getScoreboardName(), objective).getScore() : 20;
    }
}
