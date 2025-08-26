package pmcp.mode.move;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import pmcp.event.EventTarget;
import pmcp.event.mode.packet.EventPacket;
import pmcp.event.mode.player.EventSlow;
import pmcp.event.mode.update.EventTick;
import pmcp.event.mode.update.EventUpdate;
import pmcp.event.mode.world.EventWorld;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.utils.client.OldNaming;
import pmcp.utils.item.InventoryUtils;
import pmcp.value.values.ModeValue;
import pmcp.value.values.NumberValue;

public class NoSlow extends Mode {

    private final ModeValue<mode> modeValue = new ModeValue<>(this, "Mode", mode.values(), mode.GrimTick);

    enum mode {
        GrimTick,
        Normal
    }

    public NoSlow() {
        super("NoSlow", "无减速", "无使用物品减速", Category.MOVE);
    }

    private int tickCounter = 0;
    private boolean shouldCancel = false;

    @EventTarget
    private void onUpdate(EventUpdate eventUpdate) {
        if (mc.player == null || mc.level == null) return;

        setSuffix(modeValue.getValue().toString());

        if (modeValue.getValue() == mode.GrimTick) {
            if (mc.player.isUsingItem()) {
                tickCounter++;
                shouldCancel = tickCounter % 2 == 0;
            } else {
                shouldCancel = false;
                tickCounter = 0;
            }
        } else {
            shouldCancel = true;
        }
    }

    @EventTarget
    private void onSlow(EventSlow eventSlow) {
        switch (modeValue.getValue()) {
            case Normal -> eventSlow.cancelEvent();
            case GrimTick -> eventSlow.setCancelled(shouldCancel);
        }
    }

    @EventTarget
    private void onWorld(EventWorld eventWorld) {
        tickCounter = 0;
        shouldCancel = false;
    }
}