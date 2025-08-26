package pmcp.mode.combat;

import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.world.entity.LivingEntity;
import pmcp.event.EventTarget;
import pmcp.event.mode.player.EventAttack;
import pmcp.event.mode.player.EventKeyMoveInput;
import pmcp.event.mode.player.EventMotion;
import pmcp.event.mode.update.EventUpdate;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.utils.client.OldNaming;
import pmcp.value.values.ModeValue;
import pmcp.value.values.NumberValue;

public class WTap extends Mode {

    private final ModeValue<mode> modeValue = new ModeValue<>(this, "Mode", mode.values(), mode.Legit);

    enum mode {
        Wtap,
        Legit,

        Packet
    }

    public final NumberValue<Number> hurtTime = new NumberValue<>(this,"HurtTime", 10, 0, 10, 1);

    public WTap() {
        super("SuperKnockBack", "超级击退", "自动打出更大的击退", Category.COMBAT);
    }

    public int tick;


    @EventTarget
    private void onAttack(EventAttack attack) {
        if (mc.player == null || mc.level == null) return;

        LivingEntity entity = (LivingEntity) attack.getTarget();

        if (entity != null && entity.hurtTime >= hurtTime.getValue().intValue()) {
            switch (modeValue.getValue()) {
                case Legit, Wtap -> tick = 2;

                case Packet -> {
                    if (mc.player.isSprinting()) {
                        mc.player.connection.send(OldNaming.C0BPacketEntityAction(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                    }

                    mc.player.connection.send(OldNaming.C0BPacketEntityAction(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));
                    mc.player.connection.send(OldNaming.C0BPacketEntityAction(mc.player, ServerboundPlayerCommandPacket.Action.STOP_SPRINTING));
                    mc.player.connection.send(OldNaming.C0BPacketEntityAction(mc.player, ServerboundPlayerCommandPacket.Action.START_SPRINTING));

                    mc.player.setSprinting(true);
                    mc.player.setWasSprinting(true);
                }
            }
        }
    }

    @EventTarget
    private void onUpdate(EventKeyMoveInput eventKeyMoveInput) {
        if (modeValue.getValue().equals(mode.Wtap)) {
            if (tick == 2) {
                eventKeyMoveInput.setUp(false);
                tick = 1;
            } else if (tick == 1) {
                eventKeyMoveInput.setUp(mc.options.keyUp.isDown());
                tick = 0;
            }
        }
    }

    @EventTarget
    private void onUpdate(EventUpdate eventUpdate) {
        if (mc.player == null) return;

        if (modeValue.getValue().equals(mode.Legit)) {
            if (tick == 2) {
                mc.player.setSprinting(false);
                tick = 1;
            } else if (tick == 1) {
                mc.player.setSprinting(true);
                tick = 0;
            }
        }
    }
}
