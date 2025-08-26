package pmcp.utils.block;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class BlockUtils {

    public static final Set<Block> INVALID_BLOCKS = Set.of(
            Blocks.DEAD_BUSH,            // 枯萎的灌木（通常不可交互）
            Blocks.REDSTONE_WIRE,        // 红石线（在某些情况下无效）
            Blocks.SUGAR_CANE,           // 甘蔗（可能干扰机制）
            Blocks.TORCH,                // 火把（排除）
            Blocks.REDSTONE_TORCH,       // 红石火把（排除）
            Blocks.LEVER,                // 拉杆（排除）
            Blocks.STONE_BUTTON,         // 石按钮（排除）
            Blocks.IRON_TRAPDOOR,        // 铁陷阱门（排除）
            Blocks.ENCHANTING_TABLE,     // 附魔台（排除）
            Blocks.ENDER_CHEST,          // 末影箱（排除）
            Blocks.CHEST,                // 箱子（排除）
            Blocks.TRAPPED_CHEST,        // 陷阱箱（排除）
            Blocks.ANVIL,                // 铁砧（排除）
            Blocks.CAKE,                 // 蛋糕（排除）
            Blocks.FLOWER_POT,           // 花盆（排除）
            Blocks.REPEATER,             // 红石中继器（排除）
            Blocks.COMPARATOR            // 红石比较器（排除）
    );

    public static boolean isValidBlock(Block block) {
        return !(block instanceof LiquidBlock)
                && !(block instanceof AirBlock)
                && !(block instanceof ChestBlock)
                && !(block instanceof FurnaceBlock)
                && !(block instanceof LadderBlock)
                && !(block instanceof TntBlock);
    }

    public static boolean isAirBlock(BlockPos blockPos) {
        Block block = Minecraft.getInstance().level.getBlockState(blockPos).getBlock();
        return block instanceof AirBlock;
    }

    public static Block getBlock(BlockPos pos) {
        return Minecraft.getInstance().level.getBlockState(pos).getBlock();
    }

    public static boolean setAirBlock(BlockPos pos) {
        return Minecraft.getInstance().level.setBlock(pos, Blocks.AIR.defaultBlockState(), 1);
    }

    public static Map<BlockPos, Block> searchBlocks(int radius) {
        Map<BlockPos, Block> blocks = new HashMap<>();
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return blocks;
        }
        for (int x = radius; x >= -radius + 1; x--) {
            for (int y = radius; y >= -radius + 1; y--) {
                for (int z = radius; z >= -radius + 1; z--) {
                    BlockPos blockPos = new BlockPos(player.getX() + x, player.getY() + y, player.getZ() + z);
                    Block block = BlockUtils.getBlock(blockPos);
                    if (block == null) {
                        continue;
                    }
                    blocks.put(blockPos, block);
                }
            }
        }
        return blocks;
    }
}
