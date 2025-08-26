package pmcp.mode.combat;

import jnic.JNICInclude;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.*;
import pmcp.event.EventTarget;
import pmcp.event.mode.packet.EventPacket;
import pmcp.event.mode.player.*;
import pmcp.event.mode.update.EventTick;
import pmcp.event.mode.update.EventUpdate;
import pmcp.event.mode.world.EventWorld;
import pmcp.mode.Mode;
import pmcp.mode.move.Fly;
import pmcp.mode.player.Blink;
import pmcp.mode.player.NoFall;
import pmcp.mode.world.Scaffold;
import pmcp.utils.block.BlockUtils;
import pmcp.utils.category.Category;
import pmcp.utils.player.PacketUtils;
import pmcp.utils.rotation.RayTraceUtils;
import pmcp.utils.time.MSTimer;
import pmcp.value.values.BooleanValue;
import pmcp.value.values.ModeValue;
import pmcp.value.values.NumberValue;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

@JNICInclude
public class Velocity extends Mode {

    private final ModeValue<mode> modeValue = new ModeValue<>(this, "Mode", mode.values(), mode.Simple);

    enum mode {
        Simple,
        Jump,
        GrimC07,
        GrimFull,
        Normal
    }

    private final NumberValue<Number> motionXZ = new NumberValue<>(this, "MotionXZ", 1.00,0.00,1.00,0.05, () -> modeValue.getValue().equals(mode.Simple));
    private final NumberValue<Number> motionY = new NumberValue<>(this, "MotionY", 1.00,0.00,1.00,0.05, () -> modeValue.getValue().equals(mode.Simple));
    private final NumberValue<Number> ticks = new NumberValue<>(this, "StuckTicks", 3,0,5,1, () -> modeValue.getValue().equals(mode.GrimFull) || modeValue.getValue().equals(mode.GrimC07));
    private final ModeValue<fullMode> fullModeValue = new ModeValue<>(this, "FullMode", fullMode.values(), fullMode.OFFGround_FIX, () -> modeValue.getValue().equals(mode.GrimFull));

    enum fullMode {
        OFFGround_FIX,
        Normal
    }

    private final BooleanValue<Boolean> test = new BooleanValue<>(this, "Test", false, () -> modeValue.getValue().equals(mode.GrimFull));
    private final BooleanValue<Boolean> offFix = new BooleanValue<>(this, "Only Ground", false, () -> modeValue.getValue().equals(mode.GrimFull));
    private final BooleanValue<Boolean> onlyKillAura = new BooleanValue<>(this, "Only KillAura", false, () -> modeValue.getValue().equals(mode.GrimFull));


    public Velocity() {
        super("Velocity", "反击退", "抵消或删除你受到的击退", Category.COMBAT);
    }

    public static boolean val = false;
    private final MSTimer timer = new MSTimer();

    /**
     * GrimFull by 离
     */
    private final LinkedBlockingDeque<Packet<ClientPacketListener>> inBound = new LinkedBlockingDeque<>();
    private BlockHitResult result = null;
    private int direction = 1;
    private final Object lock = new Object();

    /**
     * GrimC07 by 离
     */
    private boolean stopMove;
    private int stopTicks;

    @Override
    public void onEnable() {
        timer.reset();
        val = false;
        inBound.clear();
    }

    @Override
    public void onDisable() {
        reset();
    }

    @EventTarget
    private void onMotion(EventMotion eventMotion) {
        if (mc.player == null) return;

        if (modeValue.getValue().equals(mode.Jump)) {
            if (eventMotion.getType() == EventMotion.Type.Pre) {
                if (val) val = false;
            }
        }
    }

    @EventTarget
    private void onKey(EventKeyMoveInput eventKeyMoveInput) {
        if (mc.player == null || mc.level == null) return;

        if (modeValue.getValue().equals(mode.Jump)) {
            if (val && mc.player.isMoving()) {
                eventKeyMoveInput.setJumping(true);
            }
        }
    }

    @EventTarget
    private void onWorld(EventWorld eventWorld) {
        reset();
    }

    @EventTarget
    private void onUpdate(EventUpdate evenUpdate) {

        if (modeValue.getValue().equals(mode.Jump)) {
            setSuffix("JumpRest");
        } else {
            setSuffix(modeValue.getValue().toString());
        }

        if (mc.player == null || mc.level == null) return;

        if ((Fly.Instance.isEnable() && Fly.Instance.modeValue.getValue().equals(Fly.mode.LongJump))) {
            val = false;
            return;
        }

        switch (modeValue.getValue()) {
            case Simple -> {
                if (val) {
                    mc.player.setDeltaMovement(mc.player.getDeltaMovement().multiply(motionXZ.getValue().doubleValue(), motionY.getValue().doubleValue(), motionXZ.getValue().doubleValue()));
                    val = false;
                }
            }
        }
    }

    @EventTarget
    private void onTickMotion(EventTickMotion eventTickMotion) {
        if (modeValue.getValue().equals(mode.GrimC07)) {
            if (stopMove) {
                eventTickMotion.cancelEvent();
            }
        }
    }

    @EventTarget
    private void onPacket(EventPacket event) {
        Packet<?> packet = event.getPacket();

        if (mc.player == null || mc.level == null || mc.getConnection() == null) return;

        if ((Fly.Instance.isEnable() && Fly.Instance.modeValue.getValue().equals(Fly.mode.LongJump)) || Blink.Instance.isEnable()) {
            val = false;
            reset();
            return;
        }

        if (PacketUtils.isVelocity(packet)) {
            switch (modeValue.getValue()) {
                case Normal -> event.cancelEvent();
                case Simple, Jump -> val = true;
                case GrimC07 -> {
                    HitResult ray = RayTraceUtils.pick(4.5, false, mc.player.getYRot(), 90.0f);

                    if (ray instanceof BlockHitResult blockHitResult && !mc.level.getBlockState(blockHitResult.getBlockPos()).isAir()) {
                        mc.skipTicks = ticks.getValue().intValue();
                        event.cancelEvent();
                        processC07Stop(blockHitResult);
                    }
                }
            }
        }

        if (modeValue.getValue().equals(mode.GrimFull)) {
            if (!onlyKillAura.getValue() || KillAura.Instance.isEnable()) {
                if (event.getType() == EventPacket.Type.Server) {

                    if (mc.player.tickCount <= 20) return;
                    if (NoFall.shouldJump) return;

                    if (packet instanceof ClientboundPlayerPositionPacket) {
                        synchronized (lock) {
                            for (Packet<ClientPacketListener> p : inBound) {
                                ClientPacketListener handler = mc.player.connection;
                                p.handle(handler);
                            }
                        }
                        return;
                    }

                    if (shouldQueuePacket(packet)) {
                        event.cancelEvent();
                        synchronized (lock) {
                            inBound.add((Packet<ClientPacketListener>) packet);
                        }

                        if (packet instanceof ClientboundPingPacket) {
                            handlePingPacket();
                        }
                    }

                    if (hasNoVelocityPackets()) {
                        synchronized (lock) {
                            while (!inBound.isEmpty()) {
                                Packet<ClientPacketListener> p = inBound.poll();
                                p.handle(mc.getConnection());
                            }
                        }
                    }
                }
            }
        }
    }

    private void reset() {
        synchronized (lock) {
            while (!inBound.isEmpty()) {
                Packet<ClientPacketListener> p = inBound.poll();
                p.handle(mc.getConnection());
            }
            result = null;
            direction = 1;
        }
    }

    private boolean shouldQueuePacket(Packet<?> packet) {
        return (!(packet instanceof ClientboundSetEntityMotionPacket) || ((ClientboundSetEntityMotionPacket) packet).getId() == mc.player.getId()) &&
                !(packet instanceof ClientboundLoginPacket) &&
                !(packet instanceof ClientboundRespawnPacket) &&
                !(packet instanceof ClientboundExplodePacket) &&
                !(packet instanceof ClientboundSetTimePacket) &&
                !(packet instanceof ClientboundAddEntityPacket) &&
                !(packet instanceof ClientboundTeleportEntityPacket) &&
                !(packet instanceof ClientboundSoundPacket) &&
                !(packet instanceof ClientboundSetHealthPacket) &&
                !(packet instanceof ClientboundPlayerPositionPacket) &&
                !(packet instanceof ClientboundSystemChatPacket);
    }

    private void handlePingPacket() {
        synchronized (lock) {
            int pingIndex = findFirstIndexOf(ClientboundPingPacket.class);

            // 更新 ping 前的 velocity 包的 count++
            int currentIndex = 0;
            for (Packet<?> packet : inBound) {
                if (packet instanceof ClientboundSetEntityMotionPacket velocityPacket) {
                    if (velocityPacket.count < 3 && currentIndex < pingIndex) {
                        velocityPacket.count++;
                    }
                }
                currentIndex++;
            }

            ClientboundSetEntityMotionPacket targetVelocityPacket = null;
            for (Packet<?> packet : inBound) {
                if (packet instanceof ClientboundSetEntityMotionPacket velocityPacket && velocityPacket.count >= 3) {
                    targetVelocityPacket = velocityPacket;
                    break;
                }
            }
            if (targetVelocityPacket == null) return;

            HitResult ray = RayTraceUtils.pick(4.5, false, mc.player.getYRot(), 90.0f);

            if (!(ray instanceof BlockHitResult blockRayTraceResult)) return;

            boolean shouldProcess = false;
            List<Packet<?>> pendingVelocityPackets = new ArrayList<>();

            while (!inBound.isEmpty()) {
                Packet<?> first = inBound.peek();

                if (first instanceof ClientboundSetEntityMotionPacket velocityPacket && velocityPacket != targetVelocityPacket) {
                    pendingVelocityPackets.add(velocityPacket);
                } else if (first instanceof ClientboundPingPacket) {
                    targetVelocityPacket.count--;
                }

                if (targetVelocityPacket.count == 0) break;

                Packet<?> polled = inBound.poll();
                if (polled == targetVelocityPacket) {
                    shouldProcess = checkShouldProcess(targetVelocityPacket, blockRayTraceResult);
                    if (shouldProcess) continue;
                }

                Packet<ClientPacketListener> p = (Packet<ClientPacketListener>) polled;

                p.handle(mc.getConnection());
            }

            // 回填未处理的 velocity 包
            Collections.reverse(pendingVelocityPackets);
            for (Packet<?> pkt : pendingVelocityPackets) {
                inBound.addFirst((Packet<ClientPacketListener>) pkt);
            }

            if (shouldProcess) {

                switch (fullModeValue.getValue()) {
                    case OFFGround_FIX -> {
                        BlockPos pos = new BlockPos(mc.player.getPosition());
                        BlockHitResult result1;

                        if (!mc.player.onGround()) {
                            result1 = new BlockHitResult(Scaffold.getVec3d(pos, blockRayTraceResult.getDirection()), blockRayTraceResult.getDirection(), pos, true);
                        } else {
                            result1 = blockRayTraceResult;
                        }

                        processBlockInteraction(result1);
                    }

                    case Normal -> processBlockInteraction(blockRayTraceResult);
                }
            }
        }
    }

    private boolean checkShouldProcess(ClientboundSetEntityMotionPacket velocityPacket, BlockHitResult hitResult) {
        return mc.player != null && PacketUtils.isVelocity(velocityPacket)
                && !BlockUtils.isAirBlock(hitResult.getBlockPos())
                && (!offFix.getValue() || mc.player.onGround());
    }

    private void processBlockInteraction(BlockHitResult ray) {
        result = new BlockHitResult(ray.getLocation(), ray.getDirection(), ray.getBlockPos(), false);

        direction *= -1;
        if (direction == 0) direction = 1;

        float pitch = 90;
        float yaw = mc.player.yRotLast;
        mc.player.yRotLast = yaw;
        mc.player.xRotLast = pitch;
        mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, mc.player.onGround()));

        if (test.getValue()) mc.player.hurtTime = 0;

        if (result != null) {
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, result);
            mc.setSkipTicks(ticks.getValue().intValue());
            mc.hitResult = result;
        }

        result = null;
    }

    private void processC07Stop(BlockHitResult blockHitResult) {
        mc.player.tickCount += 5;

        float pitch = 90;
        float yaw = mc.player.yRotLast;
        mc.player.yRotLast = yaw;
        mc.player.xRotLast = pitch;
        if (mc.getConnection() != null) {
            mc.getConnection().send(new ServerboundMovePlayerPacket.Rot(yaw, pitch, mc.player.onGround()));
            mc.getConnection().send(new ServerboundPlayerActionPacket(ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK, blockHitResult.getBlockPos(), blockHitResult.getDirection()));
            mc.getConnection().send(new ServerboundMovePlayerPacket.StatusOnly(mc.player.onGround()));
        }

        mc.timer.lastMs += 50L;
    }

    private boolean hasNoVelocityPackets() {
        synchronized (lock) {
            for (Packet<?> p : inBound) {
                if (p instanceof ClientboundSetEntityMotionPacket vel &&
                        vel.getId() == mc.player.getId()) {
                    return false;
                }
            }

            return true;
        }
    }

    // 查找某类 Packet 的第一个索引
    private int findFirstIndexOf(Class<?> clazz) {
        int index = 0;
        for (Packet<ClientPacketListener> packet : inBound) {
            if (clazz.isInstance(packet)) return index;
            index++;
        }
        return Integer.MAX_VALUE; // 表示未找到
    }
}
