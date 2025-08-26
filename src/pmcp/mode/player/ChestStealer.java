package pmcp.mode.player;

import jnic.JNICInclude;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.ChestBlock;
import org.apache.commons.lang3.RandomUtils;
import pmcp.event.EventTarget;
import pmcp.event.mode.packet.EventPacket;
import pmcp.event.mode.player.EventMotion;
import pmcp.event.mode.render.EventRender3D;
import pmcp.event.mode.world.EventWorld;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.utils.item.InventoryUtils;
import pmcp.utils.render.RenderUtils;
import pmcp.utils.time.MSTimer;
import pmcp.value.values.*;

public class ChestStealer extends Mode {
    public static ChestStealer Instance;
    private final NumberValue<Number> MinDelay = new NumberValue<>(this,"MinDelay", 200, 0, 600, 10);
    private final NumberValue<Number> MaxDelay = new NumberValue<>(this,"MaxDelay", 200, 0, 600, 10);
    public final BooleanValue<Boolean> silent = new BooleanValue<>(this,"SilentGUI", true);
    public final BooleanValue<Boolean> StealFromOtherContainers = new BooleanValue<>(this,"StealFromOtherContainers", true);
    public ChestStealer() {
        super("ChestStealer", "箱子小偷", "偷走箱子里的所有东西", Category.PLAYER);
        Instance = this;
    }

    private final MSTimer timer = new MSTimer();
    private float nextClickDelay = 0F;
    private AbstractContainerMenu abstractContainerMenu;

    @Override
    public void onEnable() {
        timer.reset();
    }

    @EventTarget
    private void onMotion(EventMotion event) {
        setSuffix("Delay: " + MaxDelay.getValue().intValue() + "-" + MinDelay.getValue().intValue());

        if (mc.player == null || mc.level == null || mc.gameMode == null) return;

        if (event.getType() == EventMotion.Type.Pre) {
            if (mc.screen == null) {
                abstractContainerMenu = mc.player.containerMenu;
            }
        }

        if (event.getType() == EventMotion.Type.Post || abstractContainerMenu == null) return;

        if (mc.screen instanceof AbstractContainerScreen<?> containerScreen) {

            if (mc.screen instanceof InventoryScreen || !timer.hasTimePassed((long) nextClickDelay)) {
                return;
            }

            if (containerScreen.getMenu() instanceof ChestMenu || StealFromOtherContainers.getValue() && (containerScreen.getMenu() instanceof FurnaceMenu || containerScreen.getMenu() instanceof BrewingStandMenu)) {
                containerScreen.openTick++;

                for (int i = 0; i < (containerScreen.getMenu() instanceof ChestMenu ? ((ChestMenu) containerScreen.getMenu()).getRowCount() * 9 : containerScreen.getMenu().slots.size()); i++) {
                    ItemStack itemStack = containerScreen.getMenu().getSlot(i).getItem();

                    if (isGoodItem(itemStack, abstractContainerMenu)) {

                        mc.gameMode.handleInventoryMouseClick(containerScreen.getMenu().containerId, i, 0, ClickType.QUICK_MOVE, mc.player);

                        timer.reset();
                        nextClickDelay = RandomUtils.nextFloat(MinDelay.getValue().intValue(), MaxDelay.getValue().intValue());
                        return;
                    }
                }

                if (containerScreen.openTick > 3) {
                    mc.player.closeContainer();
                }
            }
        }
    }

    private boolean isGoodItem(ItemStack stack, AbstractContainerMenu abstractContainerMenu) {
        if (stack.isEmpty()) return false;

        Item item = stack.getItem();

        return switch (item) {
            case ArmorItem ignored -> InventoryUtils.isBestArmor(abstractContainerMenu, stack);
            case SwordItem ignored -> InventoryUtils.isBestSword(abstractContainerMenu, stack);
            case TridentItem ignored -> InventoryUtils.isBestSword(abstractContainerMenu, stack);
            case DiggerItem ignored -> InventoryUtils.isBestTool(abstractContainerMenu, stack);
            case BowItem ignored -> InventoryUtils.isBestBow(abstractContainerMenu, stack);
            case BlockItem ignored -> InvManager.Instance.isEnable() ? InventoryUtils.isBestBlock(stack) && InventoryUtils.getBlockIndex(abstractContainerMenu) < InvManager.Instance.blocks.getValue().intValue() : InventoryUtils.isBestBlock(stack) || ignored == Items.TNT;
            default -> true;
        };
    }
}
