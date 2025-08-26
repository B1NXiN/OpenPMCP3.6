package pmcp.mode.player;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.phys.BlockHitResult;
import pmcp.event.EventTarget;
import pmcp.event.mode.player.EventMotion;
import pmcp.mode.Mode;
import pmcp.utils.block.BlockUtils;
import pmcp.utils.category.Category;
import pmcp.utils.client.Helper;
import pmcp.utils.player.PlayerUtils;

public class Eagle extends Mode {
    public Eagle() {
        super("Eagle", "边缘蹲", "自动在方块边缘蹲下", Category.PLAYER);
    }

    @Override
    public void onDisable() {
        mc.options.keyShift.setDown(false);
    }

    @EventTarget
    private void onMotion(EventMotion motion) {
        if (mc.level ==null || mc.player == null) return;

        if (motion.getType() == EventMotion.Type.Pre) {
            mc.options.keyShift.setDown(mc.level.getBlockState(new BlockPos(mc.player.getX(), mc.player.getY() - 1, mc.player.getZ())).getBlock() instanceof AirBlock);
        }
    }
}
