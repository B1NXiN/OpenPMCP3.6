package pmcp.mode.combat;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.stats.StatsCounter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import pmcp.PMCP;
import pmcp.event.EventTarget;
import pmcp.event.mode.packet.EventPacket;
import pmcp.event.mode.player.EventAttack;
import pmcp.event.mode.player.EventKeyMoveInput;
import pmcp.event.mode.player.EventMotion;
import pmcp.event.mode.player.EventTickMotion;
import pmcp.event.mode.render.EventRender2D;
import pmcp.event.mode.render.EventRender3D;
import pmcp.event.mode.update.EventTick;
import pmcp.event.mode.update.EventUpdate;
import pmcp.event.mode.world.EventWorld;
import pmcp.mode.Mode;
import pmcp.mode.world.Scaffold;
import pmcp.utils.category.Category;
import pmcp.utils.category.UserCategory;
import pmcp.utils.client.Helper;
import pmcp.utils.player.PacketUtils;
import pmcp.utils.render.RenderUtils;
import pmcp.value.values.BooleanValue;
import pmcp.value.values.NumberValue;

import java.awt.*;
import java.util.concurrent.LinkedBlockingQueue;

public class BackTrack extends Mode {
    private final NumberValue<Number> range = new NumberValue<>(this, "Range", 6.0,0.1,8.0,0.1);
    private final BooleanValue<Boolean> sendVelocity = new BooleanValue<>(this, "Velocity", false);

    public BackTrack() {
        super("BackTrack", "回溯", "使你能打到其他玩家之前的位置", Category.COMBAT);
    }

    private final LinkedBlockingQueue<Packet<PacketListener>> packets = new LinkedBlockingQueue<>();
    private Entity entity = null;
    private Entity oldEntity = null;
    private LocalPlayer vec3 = null;

    @Override
    public void onEnable() {
        packets.clear();
        vec3 = null;
        entity = null;
    }

    @Override
    public void onDisable() {
        send();
    }

    @EventTarget
    private void onAttack(EventAttack eventAttack) {
        if (eventAttack.getTarget() != null) {
            if (oldEntity != eventAttack.getTarget()) {
                send();

            }

            entity = eventAttack.getTarget();

            if (vec3 == null) {
                vec3 = mc.gameMode.createPlayer(mc.level, new StatsCounter(), new ClientRecipeBook());
                vec3.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
                vec3.setSprinting(entity.isSprinting());
                vec3.setShiftKeyDown(entity.isShiftKeyDown());
            }

            oldEntity = eventAttack.getTarget();
        }
    }


    @EventTarget
    private void onUpdate(EventTick eventTick) {
        if (mc.player == null || mc.level == null || mc.getConnection() == null) return;

        if (entity != null && KillAura.target == null) {
            entity = null;
            vec3 = null;
        }

        if (Velocity.val) {
            entity = null;
            vec3 = null;
            send();
            return;
        }

        if (vec3 != null) {
            if (mc.player.distanceTo(vec3) > range.getValue().doubleValue() || (mc.player.fallDistance - entity.fallDistance) > 2.5F) {
                send();
            }
        } else {
            send();
        }
    }

    @EventTarget
    private void onRender(EventRender3D eventRender3D) {
        if (mc.player == null || mc.level == null) return;

        if (vec3 != null) {
            RenderUtils.drawEntityBox3D(eventRender3D.getPoseStack(), vec3, new Color(0, 255, 0, 50));
        }
    }

    @EventTarget
    private void onPacket(EventPacket eventPacket) {
        Packet<?> packet = eventPacket.getPacket();

        if (mc.player == null || mc.level == null) return;

        if (Velocity.val) return;

        if (entity != null) {
            if (packet instanceof ClientboundPingPacket) {
                eventPacket.cancelEvent();
                packets.add((Packet<PacketListener>) packet);
            }

            if (packet instanceof ClientboundPlayerPositionPacket) {
                Helper.sendMessage(this.getName(), "Lag! - timer: " + System.nanoTime());
                entity = null;
                vec3 = null;
            }

            if (packet instanceof ClientboundExplodePacket) {
                if (sendVelocity.getValue()) {
                    eventPacket.cancelEvent();
                    packets.add((Packet<PacketListener>) packet);
                }
            }

            if (packet instanceof ClientboundSetEntityMotionPacket entityMotionPacket && entityMotionPacket.getId() == mc.player.getId()) {
                if (sendVelocity.getValue()) {
                    eventPacket.cancelEvent();
                    packets.add((Packet<PacketListener>) packet);
                }

            }

            if (packet instanceof ClientboundMoveEntityPacket moveEntityPacket && moveEntityPacket.getEntity(mc.level) == entity) {
                eventPacket.cancelEvent();
                packets.add((Packet<PacketListener>) packet);

                if (entity != null) {
                    if (!entity.isControlledByLocalInstance()) {
                        if (moveEntityPacket.hasPosition()) {
                            VecDeltaCodec vecdeltacodec = vec3.getPositionCodec();
                            Vec3 vec3 = vecdeltacodec.decode((long)moveEntityPacket.getXa(), (long)moveEntityPacket.getYa(), (long)moveEntityPacket.getZa());
                            vecdeltacodec.setBase(vec3);
                            this.vec3.moveTo(vec3.x, vec3.y, vec3.z, moveEntityPacket.getyRot(), moveEntityPacket.getxRot());
                        }
                    }
                }
            }

            if (packet instanceof ClientboundTeleportEntityPacket teleportEntityPacket && teleportEntityPacket.getId() == entity.getId()) {
                eventPacket.cancelEvent();
                packets.add((Packet<PacketListener>) packet);

                if (entity != null) {
                    double d0 = teleportEntityPacket.getX();
                    double d1 = teleportEntityPacket.getY();
                    double d2 = teleportEntityPacket.getZ();
                    vec3.syncPacketPositionCodec(d0, d1, d2);
                    if (!entity.isControlledByLocalInstance()) {
                        float f = (float)(teleportEntityPacket.getyRot() * 360) / 256.0F;
                        float f1 = (float)(teleportEntityPacket.getxRot() * 360) / 256.0F;
                        vec3.lerpTo(d0, d1, d2, f, f1, 3, true);
                        vec3.setOnGround(teleportEntityPacket.isOnGround());
                    }

                }
            }

            if (packet instanceof ClientboundSetEntityMotionPacket entityMotionPacket && entityMotionPacket.getId() == entity.getId()) {
                vec3.lerpMotion((double)entityMotionPacket.getXa() / 8000.0D, (double)entityMotionPacket.getYa() / 8000.0D, (double)entityMotionPacket.getZa() / 8000.0D);
            }
        }
    }

    @EventTarget
    private void onWorld(EventWorld eventWorld) {
        send();
    }

    private void send() {
        vec3 = null;
        entity = null;
        while (!packets.isEmpty()) {
            Packet<PacketListener> packet = packets.poll();

            packet.handle(mc.getConnection());
        }
    }
}
