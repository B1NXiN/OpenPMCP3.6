package pmcp.mode.player;

import net.minecraft.client.ClientRecipeBook;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.stats.StatsCounter;
import pmcp.event.EventTarget;
import pmcp.event.mode.packet.EventPacket;
import pmcp.event.mode.render.EventRender2D;
import pmcp.event.mode.render.EventRender3D;
import pmcp.event.mode.update.EventUpdate;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.utils.render.AnimationUtils;
import pmcp.utils.render.RenderUtils;
import pmcp.utils.time.MSTimer;
import pmcp.value.values.BooleanValue;
import pmcp.value.values.ModeValue;
import pmcp.value.values.NumberValue;

import java.awt.*;
import java.util.concurrent.LinkedBlockingQueue;

public class Blink extends Mode {

    public static Blink Instance;

    public final NumberValue<Number> maxPackets = new NumberValue<>(this, "MaxPacket", 35, 0, 200, 1);
    private final BooleanValue<Boolean> slowSend = new BooleanValue<>(this, "SlowSendPacket", true);
    private final NumberValue<Number> delay = new NumberValue<>(this, "Delay", 100, 0, 500, 1, slowSend::getValue);

    public Blink() {
        super("Blink", "瞬移", "让别人看不见你的移动包", Category.PLAYER);
        Instance = this;
    }

    public final LinkedBlockingQueue<Packet<?>> packets = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Packet<PacketListener>> serverPackets = new LinkedBlockingQueue<>();
    private final MSTimer timer = new MSTimer();
    private LocalPlayer fakerPlayer;

    @Override
    public void onEnable() {
        if (mc.player == null || mc.level == null) {
            setEnable(false);
            return;
        }

        fakerPlayer = mc.gameMode.createPlayer(mc.level, new StatsCounter(), new ClientRecipeBook());
        fakerPlayer.moveTo(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYRot(), mc.player.getXRot());
        fakerPlayer.setSprinting(mc.player.isSprinting());
        fakerPlayer.setShiftKeyDown(mc.player.isShiftKeyDown());

        packets.clear();
        serverPackets.clear();
        timer.reset();
    }

    @Override
    public void onDisable() {
        if (mc.player == null || mc.level == null) {
            return;
        }

        stop();
    }

    @EventTarget
    public void onRender3D(EventRender3D eventRender3D) {
        if (fakerPlayer != null) {
            fakerPlayer.getBoundingBox().inflate(0.2,0,0.2);
            RenderUtils.drawEntityBox3D(eventRender3D.getPoseStack(), fakerPlayer, new Color(255, 0, 0, 50));
        }
    }

    @EventTarget
    private void onUpdate(EventUpdate eventUpdate) {
        setSuffix(String.valueOf(packets.size()));

        if (mc.player == null || mc.level == null) return;

        if (packets.size() > maxPackets.getValue().intValue()) {
            stop();
        } else if (timer.hasTimePassed(delay.getValue().intValue()) && slowSend.getValue()) {
            poll();
        }
    }

    @EventTarget
    private void onPacket(EventPacket eventPacket) {
        Packet<?> packet = eventPacket.getPacket();

        if ((packet instanceof ClientboundSetEntityMotionPacket velocity
                && velocity.getId() == mc.player.getId()
                && velocity.getYa() > 0
                && (velocity.getXa() != 0 || velocity.getZa() != 0))) {
            stop();
        }

        if (packet instanceof ServerboundMovePlayerPacket.StatusOnly) {
            eventPacket.cancelEvent();
        }

        if (packet instanceof ServerboundMovePlayerPacket.Pos
                || packet instanceof ServerboundMovePlayerPacket.PosRot
                || packet instanceof ServerboundSetCarriedItemPacket
                || packet instanceof ServerboundSwingPacket
                || packet instanceof ServerboundUseItemOnPacket
                || packet instanceof ServerboundPongPacket
                || packet instanceof ServerboundPlayerCommandPacket
                || packet instanceof ServerboundInteractPacket) {
            eventPacket.cancelEvent();

            packets.add(packet);
        }
    }

    public void stop() {
        while (!packets.isEmpty()) {
            Packet<?> packet = packets.poll();

            if (packet instanceof ServerboundPlayerInputPacket) continue;

            sendPacket(packet);
            mc.player.connection.sendNoEvent(packet);
        }
    }

    public void poll() {
        if (!packets.isEmpty()) {

            Packet<?> packet = packets.poll();
            sendPacket(packet);
            mc.player.connection.sendNoEvent(packet);
        }
    }

    public void sendPacket(Packet<?> packet) {
        if (packet instanceof ServerboundMovePlayerPacket c03) {
            fakerPlayer.moveTo(c03.getX(fakerPlayer.getX()), c03.getY(fakerPlayer.getY()), c03.getZ(fakerPlayer.getZ()), c03.getYRot(fakerPlayer.getYRot()), c03.getXRot(fakerPlayer.getXRot()));
            fakerPlayer.setOnGround(c03.isOnGround());
        } else if (packet instanceof ServerboundPlayerCommandPacket commandPacket) {
            switch (commandPacket.getAction()) {
                case START_SPRINTING -> fakerPlayer.setSprinting(true);
                case STOP_SPRINTING -> fakerPlayer.setSprinting(false);
                case PRESS_SHIFT_KEY -> fakerPlayer.setShiftKeyDown(true);
                case RELEASE_SHIFT_KEY -> fakerPlayer.setShiftKeyDown(false);
            }
        } else if (packet instanceof ServerboundSetCarriedItemPacket itemPacket) {
            fakerPlayer.getInventory().selected = itemPacket.getSlot();
        }
    }

    public void pollPacket() {
        if (!packets.isEmpty()) {

            Packet<?> packet = packets.poll();

            if (!(packet instanceof ServerboundPlayerInputPacket)) {
                sendPacket(packet);
                mc.player.connection.sendNoEvent(packet);
            }
        }
    }

    public boolean stopSend() {
        while (!packets.isEmpty()) {
            Packet<?> packet = packets.poll();

            if (packet instanceof ServerboundPlayerInputPacket) return true;

            sendPacket(packet);
            mc.player.connection.sendNoEvent(packet);
        }
        return true;
    }
}
