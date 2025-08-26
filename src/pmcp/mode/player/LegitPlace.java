package pmcp.mode.player;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import pmcp.event.EventTarget;
import pmcp.event.mode.update.EventUpdate;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.utils.item.InventoryUtils;

public class LegitPlace extends Mode {
    public LegitPlace() {
        super("LegitPlace", "合法放置", "让你可以随便自救、搭路什么的，你的瞄准最好给我练", Category.PLAYER);
    }

    private int oldSlot;

    @Override
    public void onEnable() {
        if (mc.player == null) return;

        oldSlot = mc.player.getInventory().selected;
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;

        mc.player.getInventory().selected = oldSlot;
    }

    @EventTarget
    private void onUpdate(EventUpdate update) {
        if (mc.player == null || mc.level == null) return;

        mc.player.getInventory().selected = InventoryUtils.getBlockSlot() >= 0 ? InventoryUtils.getBlockSlot() : oldSlot;

        BlockHitResult blockHitResult;
        if (mc.hitResult != null && mc.hitResult.getType() == HitResult.Type.BLOCK) {
            blockHitResult = (BlockHitResult) mc.hitResult;

            if (blockHitResult.getDirection() == Direction.UP) {
                if (blockHitResult.getBlockPos().getY() + 1 >= mc.player.getY() - 1) return;

            }

            if (mc.player.getMainHandItem().getItem() instanceof BlockItem) {
                if (mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, blockHitResult) == InteractionResult.SUCCESS) {
                    mc.player.swing(InteractionHand.MAIN_HAND);
                }
            }
        }
    }
}
