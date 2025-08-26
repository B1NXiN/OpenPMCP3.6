package pmcp.mode.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import pmcp.event.EventTarget;
import pmcp.event.mode.update.EventUpdate;
import pmcp.mode.Mode;
import pmcp.mode.combat.KillAura;
import pmcp.mode.world.Scaffold;
import pmcp.utils.category.Category;
import pmcp.utils.client.Helper;
import pmcp.utils.item.InventoryUtils;
import pmcp.utils.rotation.Rotation;
import pmcp.value.values.BooleanValue;

public class AutoPail extends Mode {

    public final BooleanValue<Boolean> auto = new BooleanValue<>(this, "Auto", false);

    public AutoPail() {
        super("AutoPail", "自动水桶", "自动落地水", Category.PLAYER);
    }

    private Rotation oldRotation;

    private boolean isUse;

    private int slot = -1;
    private int oldSlot = -1;

    @Override
    public void onEnable() {
        if (mc.player == null) {
            setEnable(false);
            return;
        }

        slot = InventoryUtils.getPailSlot();
        oldSlot = -1;

        oldRotation = null;
    }

    @Override
    public void onDisable() {
        oldSlot = -1;
        slot = -1;
        oldRotation = null;
    }

    @EventTarget
    public void onUpdate(EventUpdate eventUpdate) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) return;


        BlockPos pos = new BlockPos(mc.player.getPosition().add(0,-2,0));

        if (auto.getValue()) {

            if (mc.player.fallDistance > 8 && slot >= 0) {
                if (oldSlot == -1) oldSlot = mc.player.getInventory().selected;

                mc.player.getInventory().selected = slot;

                if (!mc.level.getBlockState(pos).isAir() && !isUse) {
                    use();
                    isUse = true;
                }
            } else {
                if (isUse) {
                    use();
                    isUse = false;
                }
                stop(false);
            }
        } else {
            if (!mc.player.onGround() && slot >= 0) {
                if (oldSlot == -1) oldSlot = mc.player.getInventory().selected;

                mc.player.getInventory().selected = slot;

                if (!mc.level.getBlockState(pos).isAir() && !isUse) {
                    use();
                    isUse = true;
                }
            } else {
                if (isUse) {
                    use();
                    isUse = false;
                }
                stop(true);
            }
        }
    }

    private void use() {
        oldRotation = mc.player.getRotations();
        mc.player.setXRot(90);
        mc.startUseItem();
        mc.player.setXRot(oldRotation.getPitch());
    }

    private void stop(boolean set) {
        if (oldSlot != -1) {
            mc.player.getInventory().selected = oldSlot;
            oldSlot = -1;
        }

        if (set) {
            setEnable(false);
        } else {
            isUse = false;
        }
    }
}
