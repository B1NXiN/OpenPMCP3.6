package pmcp.mode.move;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.phys.Vec3;
import org.lwjgl.glfw.GLFW;
import pmcp.event.EventManager;
import pmcp.event.EventTarget;
import pmcp.event.mode.packet.EventPacket;
import pmcp.event.mode.render.EventRender2D;
import pmcp.event.mode.update.EventTick;
import pmcp.event.mode.update.EventUpdate;
import pmcp.event.mode.world.EventWorld;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.utils.client.Helper;
import pmcp.utils.client.OldNaming;
import pmcp.utils.player.PacketUtils;
import pmcp.utils.render.AnimationUtils;
import pmcp.utils.render.RenderUtils;
import pmcp.utils.rotation.SilentRotation;
import pmcp.value.values.BooleanValue;
import pmcp.value.values.ModeValue;
import pmcp.value.values.NumberValue;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

public class Fly extends Mode {

    public static Fly Instance;

    public final ModeValue<mode> modeValue = new ModeValue<>(this, "Mode", mode.values(), mode.Vanilla);

    public final NumberValue<Number> vanillaSpeed = new NumberValue<>(this, "VanillaSpeed", 2, 0, 10, 1, () -> modeValue.getValue().equals(mode.Vanilla));

    public enum mode {
        Jump,
        Vanilla,
        LongJump,
        GrimTicks
    }


    public Fly() {
        super("Fly", "飞行", "允许你飞行", Category.MOVE);
        Instance = this;
    }

    public final List<Packet<PacketListener>> s12Packets = new ArrayList<>();
    private final LinkedBlockingQueue<Packet<PacketListener>> s32Packets = new LinkedBlockingQueue<>();


    public double maxVelS12;
    private boolean isSendPacketIng;
    private boolean isKeyIng;

    private int ticks = 0;
    private boolean grim = false;


    @Override
    public void onEnable() {
        maxVelS12 = 0;

        ticks = 0;
        grim = false;
        mc.timer.timerSpeed = 1f;

        s12Packets.clear();
        s32Packets.clear();
        isSendPacketIng = false;
        isKeyIng = false;

        if (modeValue.getValue().equals(mode.LongJump)) {
            Helper.sendMessage("Fly提醒", "您正在使用LongJump，赞够击退后按G开始进行释放");
        }
    }

    @Override
    public void onDisable() {
        if (mc.getConnection() != null) {
            stop();
        }

        ticks = 0;
        grim = false;
        mc.timer.timerSpeed = 1f;

    }

    @EventTarget
    private void onTick(EventUpdate eventUpdate) {
        if (mc.player == null || mc.level == null) return;

        if (s12Packets.size() > maxVelS12) {
            maxVelS12 = s12Packets.size();
        }

        if (modeValue.getValue().equals(mode.GrimTicks)) {
            if (ticks == 0) {
                if (mc.player.onGround()) {
                    mc.player.jumpFromGround();
                }
            } else if (ticks <= 5) {
                mc.timer.timerSpeed = 0.45f;
            } else {
                mc.timer.timerSpeed = 1f;
            }
            ticks++;

            if (grim || ticks == 2) {
                grim = false;
            }

            if (ticks > 2) {
                mc.player.connection.sendNoEvent(OldNaming.C04PacketPlayerPosition(mc.player.getX() + 114514, 99999.0, mc.player.getZ() + 114514, mc.player.onGround()));
                mc.player.resetFallDistance();
                mc.timer.lastMs += 50L;
            }
        }
    }

    @EventTarget
    private void onUpdate(EventUpdate update) {
        if (mc.player == null || mc.level == null) return;

        setSuffix(modeValue.getValue().name());

        switch (modeValue.getValue()) {
            case Jump -> {
                if (mc.options.keyJump.isDown()) {
                    mc.player.jumpFromGround();
                }
            }
            case LongJump -> {
                if ((isSendPacketIng && s12Packets.isEmpty()) || mc.player.isSpectator() || mc.player.isRemoved()) {
                    setEnable(false);
                }

                if (isKeyIng) {
                    isSendPacketIng = true;
                    Packet<PacketListener> s12 = s12Packets.getFirst();
                    s12.handle(mc.getConnection());
                    s12Packets.removeFirst();

                    if (poll()) {
                        Helper.sendMessage("Fly提醒", "发出一次释放指令！");
                        isKeyIng = false;
                    }
                }

            }

            case Vanilla -> mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(vanillaSpeed.getValue().doubleValue(), vanillaSpeed.getValue().doubleValue(), vanillaSpeed.getValue().doubleValue()));
        }
    }

    @EventTarget
    private void onWorld(EventWorld eventWorld) {
        setEnable(false);
    }

    @EventTarget
    private void onPacket(EventPacket eventPacket) {
        Packet<?> packet = eventPacket.getPacket();

        if (mc.player == null || mc.level == null) return;

        switch (modeValue.getValue()) {
            case LongJump -> {
                if (eventPacket.getType() == EventPacket.Type.Server) {
                    if (PacketUtils.isVelocity(packet)) {
                        eventPacket.cancelEvent();
                        s12Packets.add((Packet<PacketListener>) packet);
                        s32Packets.add((Packet<PacketListener>) packet);
                    }
                    if (PacketUtils.isExplosion(packet)) {
                        eventPacket.cancelEvent();
                        s12Packets.add((Packet<PacketListener>) packet);
                        s32Packets.add((Packet<PacketListener>) packet);
                    }

                    if (packet instanceof ClientboundPingPacket) {
                        eventPacket.cancelEvent();
                        s32Packets.add((Packet<PacketListener>) packet);
                    }
                }
            }
            case GrimTicks -> {
                if (packet instanceof ClientboundPlayerPositionPacket) {
                    grim = true;
                }

                if (packet instanceof ServerboundPlayerActionPacket actionPacket && actionPacket.getAction() == ServerboundPlayerActionPacket.Action.RELEASE_USE_ITEM) {
                    eventPacket.cancelEvent();
                }
            }
        }
    }


    @Override
    public void key(int key) {
        if (modeValue.getValue().equals(mode.LongJump)) {
            if (!isKeyIng) {
                if (key == GLFW.GLFW_KEY_G) {
                    isKeyIng = true;
                }
            }
        }
        super.key(key);
    }

    private void stop() {
        while (!s12Packets.isEmpty()) {

            Packet<PacketListener> packet = s12Packets.getFirst();
            packet.handle(mc.getConnection());

            s12Packets.removeFirst();
        }
        while (!s32Packets.isEmpty()) {
            Packet<?> packet = s32Packets.poll();

            Packet<PacketListener> p = (Packet<PacketListener>) packet;

            if (packet instanceof ClientboundSetEntityMotionPacket || packet instanceof ClientboundExplodePacket) continue;

            p.handle(mc.getConnection());
        }
    }

    private boolean poll() {
        while (!s32Packets.isEmpty()) {
            Packet<?> packet = s32Packets.poll();

            Packet<PacketListener> p = (Packet<PacketListener>) packet;

            if (packet instanceof ClientboundSetEntityMotionPacket || packet instanceof ClientboundExplodePacket) return true;

            p.handle(mc.getConnection());
        }
        return false;
    }
}
