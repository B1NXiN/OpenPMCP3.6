package pmcp.mode.move;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.WebBlock;
import pmcp.event.EventTarget;
import pmcp.event.mode.update.EventUpdate;
import pmcp.event.mode.world.EventWorld;
import pmcp.mode.Mode;
import pmcp.utils.block.BlockUtils;
import pmcp.utils.category.Category;

import pmcp.value.values.ModeValue;
import pmcp.value.values.NumberValue;


public class NoWeb extends Mode {

    public static NoWeb Instance;

    public final ModeValue<mode> modeValue = new ModeValue<>(this, "Mode", mode.values(), mode.Packet);

    public enum mode {
        GrimSlow,
        Packet
    }
    public NoWeb() {
        super("NoWeb", "无蜘蛛网减速", "让你没有蜘蛛网减速", Category.MOVE);
        Instance = this;
    }

    @EventTarget
    private void onUpdate(EventUpdate eventUpdate) {
        setSuffix(modeValue.getValue().toString());
    }
}
