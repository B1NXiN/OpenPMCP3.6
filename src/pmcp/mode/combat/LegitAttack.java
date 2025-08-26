package pmcp.mode.combat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import pmcp.event.EventTarget;
import pmcp.event.mode.player.EventPlace;
import pmcp.event.mode.update.EventTick;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.utils.category.NumberCategory;
import pmcp.utils.client.RandomUtils;
import pmcp.utils.player.EntityUtils;
import pmcp.utils.time.MSTimer;
import pmcp.value.values.NumberValue;

public class LegitAttack extends Mode {

    public final NumberValue<Number> maxCPS = new NumberValue<>(this, "MaxCPS", 10,1,20,1, NumberCategory.MAX, "CPS");
    public final NumberValue<Number> minCPS = new NumberValue<>(this, "MinCPS", 10,1,20,1, NumberCategory.MIN, "CPS");
    private final NumberValue<Number> cpsDelay = new NumberValue<>(this, "CPSDelay", 500,0,1000,10);

    public LegitAttack() {
        super("LegitAttack", "合法自动攻击", "准星瞄准后可以自动攻击人", Category.COMBAT);
    }

    private final MSTimer attackTimer = new MSTimer();
    private final MSTimer cpsTimer = new MSTimer();
    private int cps;

    @EventTarget
    private void onTick(EventTick tick) {

        int oldCPS = RandomUtils.next(minCPS.getValue().intValue(), maxCPS.getValue().intValue());

        if (cpsTimer.hasTimePassed(cpsDelay.getValue().intValue()) && oldCPS != cps) {
            cps = oldCPS;
            cpsTimer.reset();
        }
    }

    @EventTarget
    private void onPlace(EventPlace eventPlace) {
        if (mc.player == null || mc.level == null) return;

        EntityHitResult entityHitResult;

        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.ENTITY) {

            entityHitResult = (EntityHitResult) mc.hitResult;

            LivingEntity entity = (LivingEntity) entityHitResult.getEntity();

            if (EntityUtils.isValidTarget((Player) entity, mc)) {

                if (attackTimer.hasTimePassed((long) (1000.0 / (cps * 1.5)))) {
                    mc.gameMode.attack(mc.player, entityHitResult.getEntity());
                    mc.player.swing(InteractionHand.MAIN_HAND);
                    attackTimer.reset();
                }
            }
        }
    }
}
