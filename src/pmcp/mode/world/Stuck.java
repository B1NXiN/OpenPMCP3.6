package pmcp.mode.world;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import pmcp.event.EventTarget;
import pmcp.event.mode.packet.EventPacket;
import pmcp.event.mode.player.EventMove;
import pmcp.event.mode.player.EventTickMotion;
import pmcp.event.mode.world.EventWorld;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.utils.client.OldNaming;
import pmcp.utils.rotation.SilentRotation;
import pmcp.value.values.ModeValue;

public class Stuck extends Mode {

    private final ModeValue<mode> modeValue = new ModeValue<>(this, "Mode", mode.values(), mode.Normal);

    enum mode {
        GrimTick,
        Normal
    }

    public Stuck() {
        super("Stuck", "卡空", "允许你卡在空中", Category.WORLD);
    }

    @EventTarget
    private void onTickMotion(EventTickMotion eventTickMotion) {
        if (mc.player == null || mc.level == null) return;

        if (modeValue.getValue().equals(mode.GrimTick)) {
            if (!mc.player.onGround()) {
                eventTickMotion.cancelEvent();
            } else {
                setEnable(false);
            }
        }
    }

    @EventTarget
    private void onWorld(EventWorld eventWorld) {
        setEnable(false);
    }

    @EventTarget
    private void onMove(EventMove eventMove) {
        if (modeValue.getValue().equals(mode.Normal)) {
            eventMove.cancelEvent();
        }
    }

    @EventTarget
    private void onPacket(EventPacket eventPacket) {
        Packet<?> packet = eventPacket.getPacket();

        if (mc.player == null || mc.level == null) return;

        if (packet instanceof ClientboundPlayerLookAtPacket) {
            setEnable(false);
        }

        if (modeValue.getValue().equals(mode.Normal)) {
            if (packet instanceof ServerboundMovePlayerPacket) {
                eventPacket.cancelEvent();
            }

            if (packet instanceof ServerboundUseItemPacket useItemPacket) {
                eventPacket.cancelEvent();

                Item i = mc.player.getItemInHand(useItemPacket.getHand()).getItem();

                if (i == Items.ENDER_PEARL || i == Items.EGG || i == Items.SNOWBALL) {
                    mc.player.connection.sendNoEvent(OldNaming.C05PacketPlayerLook(SilentRotation.getRotations().getYaw(), SilentRotation.getRotations().getPitch(), mc.player.onGround()));
                    mc.player.connection.sendNoEvent(packet);
                } else {
                    mc.player.connection.sendNoEvent(packet);
                }
            }
        }
    }
}
