package pmcp.mode.world;

import jnic.JNICInclude;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;

import pmcp.event.EventTarget;
import pmcp.event.mode.packet.EventPacket;
import pmcp.event.mode.player.*;
import pmcp.event.mode.render.EventRender3D;
import pmcp.event.mode.update.EventTick;
import pmcp.event.mode.update.EventUpdate;
import pmcp.mode.Mode;
import pmcp.utils.math.MathHelper;
import pmcp.utils.block.BlockData;
import pmcp.utils.block.BlockUtils;
import pmcp.utils.category.Category;
import pmcp.utils.category.MoveCategory;
import pmcp.utils.client.Helper;
import pmcp.utils.item.InventoryUtils;
import pmcp.utils.player.FallingPlayer;
import pmcp.utils.player.PlayerUtils;
import pmcp.utils.player.SpoofSlotManager;
import pmcp.utils.render.RenderUtils;
import pmcp.utils.rotation.RayTraceUtils;
import pmcp.utils.rotation.Rotation;
import pmcp.utils.rotation.RotationUtils;
import pmcp.utils.rotation.SilentRotation;
import pmcp.utils.time.MSTimer;
import pmcp.value.values.*;

import java.awt.*;

@JNICInclude
public class Scaffold extends Mode {

    public static Scaffold Instance;


    private final ModeValue<mode> modeValue = new ModeValue<>(this, "Mode", mode.values(), mode.Telly);

    enum mode {
        Telly,
        Normal,
        Sprint,
        Legit
    }

    private final NumberValue<Number> delay = new NumberValue<>(this, "Delay", 4,1,8,1,() -> modeValue.getValue().equals(mode.Telly));
    private final BooleanValue<Boolean> advancedBlockSearch = new BooleanValue<>(this, "BlockSearch", true);
    private final BooleanValue<Boolean> vulcan = new BooleanValue<>(this, "Vulcan bypass?", true,() -> modeValue.getValue().equals(mode.Telly));
    public Scaffold() {
        super("Scaffold", "自动搭路", "自动在你脚下放置方块", Category.WORLD);
        Instance = this;
    }

    private BlockData blockData;
    private final MSTimer timer = new MSTimer();
    private int slot;
    private int oldSlot;
    public static ItemStack oldStack = null;

    public static boolean isScaffold;

    private HitResult hitResult;

    private boolean shift;
    private double keeyYpos;
    public double blockSize;
    public double maxBlockSize;

    private int rotateCount = 0;
    public int bigVelocityTick = 0;
    public boolean reachable;
    private boolean stopMove;



    @Override
    public void onEnable() {
        if (mc.player == null) {
            setEnable(false);
            return;
        }

        blockData = null;
        shift = false;
        rotateCount = 0;
        bigVelocityTick = 0;
        reachable = false;
        oldSlot = mc.player.getInventory().selected;
        blockSize = InventoryUtils.getBlockIndex();
        maxBlockSize = InventoryUtils.getBlockIndex();
        timer.reset();

        if (InventoryUtils.getBlockIndex() < 0) {
            Helper.sendMessage("Scaffold警告！", "背包中没有任何方块可用！");
            setEnable(false);
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;

        mc.player.getInventory().selected = oldSlot;
        if (shift) {
            mc.options.keyShift.setDown(false);
        }

        isScaffold = false;
        stopMove = false;
        maxBlockSize = 0;
        blockSize = 0;
        rotateCount = 0;
        bigVelocityTick = 0;
        reachable = false;
        SpoofSlotManager.stop();

        oldStack = null;
    }

    @EventTarget
    private void onTick(EventTick eventTick) {
        if (blockSize < 0) {
            Helper.sendMessage("Scaffold警告！", "背包中没有任何方块可用！");
            setEnable(false);
            return;
        }

        blockSize = InventoryUtils.getBlockIndex();

    }

    @EventTarget
    private void onMotion(EventMotion motion) {
        if (mc.level ==null || mc.player == null) return;

        if (modeValue.getValue().equals(mode.Legit)) {
            if (motion.getType() == EventMotion.Type.Pre) {
                shift = mc.level.getBlockState(new BlockPos(mc.player.getX(), mc.player.getY() - 1, mc.player.getZ())).getBlock() instanceof AirBlock;
                mc.options.keyShift.setDown(shift);
            }
        }
    }

    @EventTarget
    private void onKeyMoveInput(EventKeyMoveInput eventKeyMoveInput) {
        if (mc.level ==null || mc.player == null) return;

        if (vulcan.getValue()) {
            if (modeValue.getValue().equals(mode.Telly) && mc.player.onGround()) {
                eventKeyMoveInput.setShiftKeyDown(true);
            } else {
                eventKeyMoveInput.setShiftKeyDown(mc.options.keyShift.isDown());
            }
        }
    }

    @EventTarget
    private void onRender3D(EventRender3D render3D) {
        if (blockData != null) {
            switch (blockData.facing()) {
                case UP -> RenderUtils.drawBlockBox(render3D.getPoseStack(), blockData.pos().add(0, 1, 0), new Color(255, 255, 255, 50));
                case DOWN -> RenderUtils.drawBlockBox(render3D.getPoseStack(), blockData.pos().add(0, -1, 0), new Color(255, 255, 255, 50));
                case NORTH -> RenderUtils.drawBlockBox(render3D.getPoseStack(), blockData.pos().add(0, 0, -1), new Color(255, 255, 255, 50));
                case SOUTH -> RenderUtils.drawBlockBox(render3D.getPoseStack(), blockData.pos().add(0, 0, 1), new Color(255, 255, 255, 50));
                case WEST -> RenderUtils.drawBlockBox(render3D.getPoseStack(), blockData.pos().add(-1, 0, 0), new Color(255, 255, 255, 50));
                case EAST -> RenderUtils.drawBlockBox(render3D.getPoseStack(), blockData.pos().add(1, 0, 0), new Color(255, 255, 255, 50));
            }
        }
    }

    @EventTarget
    private void onPacket(EventPacket eventPacket) {
        Packet<?> packet = eventPacket.getPacket();

        if (mc.player == null || mc.level == null) return;

        if (packet instanceof ClientboundSetEntityMotionPacket motionPacket && motionPacket.getId() == mc.player.getId()) {
            double strength = new Vec3(motionPacket.getXa() / 8000D, 0, motionPacket.getZa() / 8000D).lengthVector();
            if (strength >= 1.5D) {
                bigVelocityTick = 60;
            }
        }
    }

    @EventTarget
    private void onUpdate(EventUpdate event) {
        setSuffix(modeValue.getValue().name());

        if (mc.player == null || mc.level == null) return;

        oldStack = mc.player.containerMenu.getSlot(oldSlot + 36).getItem();

        if (mc.player.onGround()) {
            keeyYpos = MathHelper.floor(mc.player.getY() - 1);
        }

        onPlace();

        if (bigVelocityTick > 0) {
            bigVelocityTick--;
        }
        if (mc.player.onGround() && bigVelocityTick <= 30) {
            bigVelocityTick = 0;
        }

        slot = InventoryUtils.getBlockSlot() >= 0 ? InventoryUtils.getBlockSlot() : oldSlot;

        switch (modeValue.getValue()) {
            case Telly -> {
                blockData = getBlockData(new BlockPos(mc.player.getX(), getPosY(), mc.player.getZ()));

                isScaffold = mc.player.offGroundTicks >= delay.getValue().intValue();
            }
            case Normal, Legit -> {
                blockData = getBlockData(new BlockPos(mc.player.getX(), mc.player.getY() - 1, mc.player.getZ()));

                isScaffold = true;
            }
            case Sprint -> {
                blockData = getBlockData(new BlockPos(mc.player.getX(), getPosY(), mc.player.getZ()));

                isScaffold = mc.level.getBlockState(new BlockPos(mc.player.getX(), getPosY(), mc.player.getZ())).getBlock() instanceof AirBlock;
            }
        }

        SpoofSlotManager.setSlot(oldSlot);

        mc.player.getInventory().selected = slot >= 0 ? slot : oldSlot;

        if (blockData != null) {
            Rotation rotation1 = RotationUtils.getRotation(blockData.pos().getCenter(), mc.player.getEyePosition());

            if (isScaffold) {
                reachable = true;
                if (mc.player.getMotionY() < -0.1) {
                    FallingPlayer fallingPlayer = new FallingPlayer(mc.player);
                    fallingPlayer.calculate(2);
                    if (blockData.pos().getY() > fallingPlayer.getY()) {
                        reachable = false;
                    }
                }

                if ((!reachable || bigVelocityTick > 0) && rotateCount <= 4 && blockSize > 0) {
                    rotateCount++;
                    SilentRotation.setRotation(rotation1, MoveCategory.Silent, true);
                    stopMove = true;
                } else {
                    rotateCount = 0;
                    stopMove = false;
                    SilentRotation.setRotation(rotation1, MoveCategory.Silent, true);
                    hitResult = RayTraceUtils.pick(6.0, false, rotation1);
                }
            } else {
                reachable = false;
                SilentRotation.setRotation(new Rotation(mc.player.getYRot(), 45), MoveCategory.Silent, true);
                hitResult = RayTraceUtils.pick(6.0, false, new Rotation(mc.player.getYRot(), 45));
            }
        }
    }

    @EventTarget
    private void onTickMove(EventTickMotion eventTickMotion) {
        if (stopMove) {
            eventTickMotion.setTicks(20);
            eventTickMotion.cancelEvent();
        }
    }


    private void onPlace() {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        if (blockData != null) {

            if (isScaffold) {
                if (mc.level.getBlockState(new BlockPos(mc.player.getX(), mc.player.getY() - 1, mc.player.getZ())).getBlock() instanceof AirBlock && mc.player.getMainHandItem().getItem() instanceof BlockItem) {
                    if (mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, new BlockHitResult(getVec3d(blockData.pos(), blockData.facing()), blockData.facing(), blockData.pos(), true)) == InteractionResult.SUCCESS) {
                        mc.player.swing(InteractionHand.MAIN_HAND);
                    }
                }
            }
        }
    }

    @EventTarget
    private void onStrafe(EventStrafe event) {
        if (mc.player != null) {
            if (modeValue.getValue().equals(mode.Telly) && mc.player.onGround() && !mc.options.keyJump.isDown()) {
                mc.player.jumpFromGround();
            }
        }
    }

    private double getPosY() {

        switch (modeValue.getValue()) {
            case Telly -> {
                if (mc.options.keyJump.isDown()) return mc.player.getY() - 1.0;
            }

            case Normal, Legit, Sprint -> {
                return !mc.player.isMoving() ? mc.player.getY() - 1.0 : keeyYpos;
            }

        }
        return !mc.player.isMoving() ? mc.player.getY() - 1.0 : keeyYpos;
    }

    public static Vec3 getVec3d(BlockPos pos, Direction face) {
        double x = (double) pos.getX() + 0.5;
        double y = (double) pos.getY() + 0.5;
        double z = (double) pos.getZ() + 0.5;
        if (face == Direction.UP || face == Direction.DOWN) {
            x += MathHelper.getRandomInRange(0.3, -0.3);
            z += MathHelper.getRandomInRange(0.3, -0.3);
        } else {
            y += 0.08;
        }
        if (face == Direction.WEST || face == Direction.EAST) {
            z += MathHelper.getRandomInRange(0.3, -0.3);
        }
        if (face == Direction.SOUTH || face == Direction.NORTH) {
            x += MathHelper.getRandomInRange(0.3, -0.3);
        }
        return new Vec3(x, y, z);
    }

    private BlockData getBlockData(BlockPos pos) {
        BlockPos blockPos = new BlockPos(mc.player.position().add(0,-1,0));

        if (!mc.player.isMoving()) {
            if (!mc.level.getBlockState(blockPos).isAir()) {
                return new BlockData(blockPos, Direction.UP);
            }
        }

        for (Direction dir : Direction.VALUES) {
            BlockPos neighbor = pos.relative(dir);

            if (dir == Direction.DOWN) continue;

            if (neighbor.getY() > pos.getY()) continue;
            if (!isPosSolid(neighbor)) continue;

            return new BlockData(neighbor, dir.getOpposite());
        }

        BlockPos playerPos = mc.player.blockPosition();
        BlockData closestBlockData = null;
        double minDistance = Double.MAX_VALUE;

        int range = 6;

        for (int dx = -range; dx <= range; dx++) {
            for (int dy = -range; dy <= range; dy++) {
                for (int dz = -range; dz <= range; dz++) {
                    BlockPos base = playerPos.offset(dx, dy, dz);

                    if (base.getY() > pos.getY()) continue;
                    if (!isPosSolid(base)) continue;

                    for (Direction dir : Direction.values()) {
                        if (dir == Direction.DOWN) continue;

                        BlockPos target = base.relative(dir);

                        if (!mc.level.getBlockState(target).isAir()) continue;

                        if (target.equals(pos)) {
                            return new BlockData(base, dir);
                        }

                        double distance = pos.distSqr(target);
                        if (distance < minDistance) {
                            minDistance = distance;
                            closestBlockData = new BlockData(base, dir);
                        }
                    }
                }
            }
        }

        return closestBlockData;
    }

    public boolean isPosSolid(BlockPos pos) {
        BlockState state = mc.level.getBlockState(pos);
        Block block = state.getBlock();

        if (BlockUtils.INVALID_BLOCKS.contains(block)) return false;

        return !state.isAir();
    }
}
