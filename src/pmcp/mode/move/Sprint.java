package pmcp.mode.move;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import pmcp.event.EventTarget;
import pmcp.event.mode.player.EventMotion;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.value.values.BooleanValue;

import java.util.ArrayList;
import java.util.List;

public class Sprint extends Mode {

    public static Sprint Instance;

    public final BooleanValue<Boolean> food = new BooleanValue<>(this, "Food", true);
    public final BooleanValue<Boolean> keep = new BooleanValue<>(this, "Keep", true);
    public final BooleanValue<Boolean> fullFoodLevel = new BooleanValue<>(this, "FullFoodLevel", false);

    public Sprint() {
        super("Sprint", "疾跑", "让你保持疾跑", Category.MOVE);
        Instance = this;
    }

    @EventTarget
    private void onMotion(EventMotion eventMotion) {
        if (mc.player == null || mc.level == null) return;

        boolean hasSlowness = mc.player.getActiveEffects().stream()
                .anyMatch(effectInstance -> effectInstance.getEffect() == MobEffect.byId(2));

        if (hasSlowness && mc.player.isSprinting()) {
            mc.player.setSprinting(false);
        }
    }
}
