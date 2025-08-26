package pmcp.mode.render;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import pmcp.event.EventTarget;
import pmcp.event.mode.packet.EventPacket;
import pmcp.event.mode.render.EventRender3D;
import pmcp.event.mode.update.EventUpdate;
import pmcp.event.mode.world.EventWorld;
import pmcp.mode.Mode;
import pmcp.utils.block.BlockUtils;
import pmcp.utils.category.Category;
import pmcp.utils.render.RenderUtils;
import pmcp.value.values.NumberValue;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheatESP extends Mode {

    private final NumberValue<Number> range = new NumberValue<>(this, "Range", 12, 1, 128, 1);

    public CheatESP() {
        super("ChestESP", "箱子透视", "将你附近的箱子显示出来并渲染ESP", Category.RENDER);
    }

    private final List<BlockPos> blockPosList = new ArrayList<>();
    private final List<BlockPos> openChestPos = new ArrayList<>();

    @Override
    public void onEnable() {
        openChestPos.clear();
        blockPosList.clear();
    }

    @EventTarget
    private void onRender3D(EventRender3D eventRender3D) {
        if (mc.level == null || mc.player == null) return;

        if (!blockPosList.isEmpty()) {
            for (BlockPos pos : blockPosList) {

                boolean isOpen = !openChestPos.isEmpty() && openChestPos.contains(pos);

                if (BlockUtils.getBlock(pos) instanceof ChestBlock) {
                    RenderUtils.drawBlockBox(eventRender3D.getPoseStack(), pos, isOpen ? new Color(0,255,0, 50) : new Color(255,0,0, 50));
                }
            }
        }
    }

    @EventTarget
    private void onWorld(EventWorld eventWorld) {
        openChestPos.clear();
        blockPosList.clear();
    }

    @EventTarget
    private void onUpdate(EventUpdate e) {
        if (mc.level == null || mc.player == null) return;

        Map<BlockPos, Block> searchBlock = BlockUtils.searchBlocks(range.getValue().intValue());

        for (Map.Entry<BlockPos, Block> block : searchBlock.entrySet()) {
            if (!(BlockUtils.getBlock(block.getKey()) instanceof ChestBlock) || blockPosList.contains(block.getKey())) continue;

            blockPosList.add(block.getKey());
        }
    }

    @EventTarget
    private void onPacket(EventPacket eventPacket) {
        if (mc.level == null || mc.player == null) return;

        Packet<?> packet = eventPacket.getPacket();

        if (packet instanceof ServerboundUseItemOnPacket onPacket) {
            BlockPos blockPos = onPacket.getHitResult().getBlockPos();
            if (BlockUtils.getBlock(blockPos) instanceof ChestBlock && !openChestPos.contains(blockPos)) {
                openChestPos.add(blockPos);
            }
        }
    }
}
