package pmcp.mode.combat;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.projectile.Fireball;
import pmcp.event.EventTarget;
import pmcp.event.mode.update.EventUpdate;
import pmcp.mode.Mode;
import pmcp.mode.move.Fly;
import pmcp.mode.world.Scaffold;
import pmcp.utils.category.Category;
import pmcp.utils.category.MoveCategory;
import pmcp.utils.client.Helper;
import pmcp.utils.client.OldNaming;
import pmcp.utils.rotation.RotationUtils;
import pmcp.utils.rotation.SilentRotation;
import pmcp.value.values.BooleanValue;
import pmcp.value.values.NumberValue;

public class AutoHit extends Mode {

    private final NumberValue<Number> rangeEndCrystal = new NumberValue<>(this, "RangeEndCrystal", 4,1,6,1);
    private final BooleanValue<Boolean> debug = new BooleanValue<>(this, "DeBug", false);

    public AutoHit() {
        super("AntiEndCrystal", "反水晶", "自动将附近水晶打爆", Category.COMBAT);
    }

    @EventTarget
    private void onUpdate(EventUpdate eventUpdate) {
        if (mc.player == null || mc.level == null) return;

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof EndCrystal) {
                if (mc.player.distanceTo(entity) < rangeEndCrystal.getValue().doubleValue()) {
                    sendC02(entity);

                    if (mc.player.distanceTo(entity) > 3.3) {
                        lod(debug.getValue(), "你可能叠了vl，因为你的距离大于3.3");
                    }
                    break;
                }
            }
        }
    }

    private void sendC02(Entity entity) {
        if (mc.player == null) return;


        if (KillAura.target != null) {
            KillAura.target = null;
            SilentRotation.stopTargetRotation();
        }
        if (Scaffold.isScaffold) {
            Scaffold.isScaffold = false;
            SilentRotation.stopTargetRotation();
        }

        SilentRotation.setRotation(RotationUtils.getNCPRotations(entity.getPosition(), true), MoveCategory.Silent, true);

        mc.player.connection.send(OldNaming.C02PacketUseEntity(entity));
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    private void lod(boolean send, String string) {
        if (send) Helper.sendMessage(this.getName(), string);
    }
}
