package pmcp.utils.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public record BlockData(BlockPos pos, Direction facing) {
}
