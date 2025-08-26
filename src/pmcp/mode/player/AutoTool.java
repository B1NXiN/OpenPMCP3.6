package pmcp.mode.player;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirtPathBlock;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.state.BlockState;
import pmcp.event.EventTarget;
import pmcp.event.mode.packet.EventPacket;
import pmcp.event.mode.world.EventWorld;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.utils.client.Helper;
import pmcp.utils.player.SpoofSlotManager;

import java.awt.*;

public class AutoTool extends Mode {
    public AutoTool() {
        super("AutoTool", "自动工具", "自动切换至合适的工具", Category.PLAYER);
    }


    private int oldSlot;

    @Override
    public void onEnable() {
        oldSlot = -1;
    }

    @EventTarget
    private void onPacket(EventPacket eventPacket) {
        if (mc.player == null || mc.level == null) return;

        if (mc.options.keyAttack.isDown()) {

            if (eventPacket.getPacket() instanceof ServerboundPlayerActionPacket c07) {
                if (c07.getAction() == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {
                    if (switchSlot(c07.getPos()) != -1) {
                        if (oldSlot == -1) oldSlot = mc.player.getInventory().selected;
                        if (oldSlot != -1) SpoofSlotManager.setSlot(oldSlot);
                        mc.player.getInventory().selected = switchSlot(c07.getPos());
                    }
                }
            }
        } else {
            if (oldSlot != -1) {
                mc.player.getInventory().selected = oldSlot;
                SpoofSlotManager.stop();
                oldSlot = -1;
            }
        }
    }

    @EventTarget
    private void onWorld(EventWorld eventWorld) {
        if (oldSlot != -1) {
            mc.player.getInventory().selected = oldSlot;
            SpoofSlotManager.stop();
            oldSlot = -1;
        }
    }

    private int switchSlot(BlockPos blockPos) {
        float bestSpeed = 1.0F;
        int bestSlot = -1;

        BlockState blockState = mc.level.getBlockState(blockPos);

        for (int i = 0; i <= 8; i++) {
            ItemStack item = mc.player.containerMenu.getSlot(i + 36).getItem();
            if (item.isEmpty()) {
                continue;
            }
            float speed = item.getDestroySpeed(blockState);

            if (speed > bestSpeed) {
                bestSpeed = speed;
                bestSlot = i;
            }
        }

        return bestSlot;
    }

}
