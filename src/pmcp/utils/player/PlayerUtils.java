package pmcp.utils.player;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import pmcp.utils.math.MathHelper;
import pmcp.utils.block.FacingData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlayerUtils {

    private static final Minecraft mc = Minecraft.getInstance();

    public static FacingData getEnumFacing(final Vec3 position) {
        for (int x2 = -1; x2 <= 1; x2 += 2) {
            if (!(block(position.x + x2, position.y, position.z) instanceof AirBlock)) {
                if (x2 > 0) {
                    return new FacingData(Direction.WEST, new Vec3(x2, 0, 0));
                } else {
                    return new FacingData(Direction.EAST, new Vec3(x2, 0, 0));
                }
            }
        }

        for (int y2 = -1; y2 <= 1; y2 += 2) {
            if (!(block(position.x, position.y + y2, position.z) instanceof AirBlock)) {
                if (y2 < 0) {
                    return new FacingData(Direction.UP, new Vec3(0, y2, 0));
                }
            }
        }

        for (int z2 = -1; z2 <= 1; z2 += 2) {
            if (!(block(position.x, position.y, position.z + z2) instanceof AirBlock)) {
                if (z2 < 0) {
                    return new FacingData(Direction.SOUTH, new Vec3(0, 0, z2));
                } else {
                    return new FacingData(Direction.NORTH, new Vec3(0, 0, z2));
                }
            }
        }

        return null;
    }

    public static Vec3 getPlacePossibility(BlockPos pos, float blockRange) {
        final List<Vec3> possibilities = new ArrayList<>();
        final int range = (int) blockRange + 1;

        for (int x = -range; x <= range; ++x) {
            for (int y = -range; y <= -1; ++y) {
                for (int z = -range; z <= range; ++z) {
                    final Block block = blockRelativeToPlayer(x, y, z);

                    if (!placable(block)) {
                        for (int x2 = -1; x2 <= 1; x2 += 1)
                            possibilities.add(new Vec3(pos.getX() + x + x2, mc.player.getY() + y, pos.getZ() + z));

                        for (int y2 = -1; y2 <= 1; y2 += 1)
                            possibilities.add(new Vec3(pos.getX() + x, mc.player.getY() + y + y2, pos.getZ() + z));

                        for (int z2 = -1; z2 <= 1; z2 += 1)
                            possibilities.add(new Vec3(pos.getX() + x, mc.player.getY() + y, pos.getZ() + z + z2));
                    }
                }
            }
        }

        possibilities.removeIf(vec3 -> mc.player.distanceTo(BlockPos.containing(vec3)) > blockRange || !(block(vec3.x, vec3.y, vec3.z) instanceof AirBlock));

        if (possibilities.isEmpty()) return null;

        possibilities.sort(Comparator.comparingDouble(vec3 -> {

            final double d0 = pos.getZ() - vec3.x;
            final double d1 = pos.getY() - vec3.y;
            final double d2 = pos.getZ() - vec3.z;
            return MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);

        }));

        return possibilities.getFirst();
    }

    private static boolean placable(Block block) {
        if (block == null) return false;
        return block instanceof AirBlock || block instanceof LiquidBlock || block instanceof BushBlock || block instanceof ButtonBlock || block instanceof FireBlock || block instanceof SimpleWaterloggedBlock;
    }

    private static Block block(final double x, final double y, final double z) {
        return mc.level.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    private static Block blockRelativeToPlayer(final double offsetX, final double offsetY, final double offsetZ) {
        return mc.level.getBlockState(new BlockPos(mc.player).add(offsetX, offsetY, offsetZ)).getBlock();
    }


    public static boolean isFallingToVoid(int y) {
        for (int i = 1; i < y; i++) {
            if (mc.level.getBlockState(new BlockPos(mc.player.getX(), mc.player.getY() - i, mc.player.getZ())).isAir()) continue;

            return true;
        }
        return false;
    }
}

