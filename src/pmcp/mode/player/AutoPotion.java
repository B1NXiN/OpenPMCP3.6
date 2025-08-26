package pmcp.mode.player;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import pmcp.event.EventTarget;
import pmcp.event.mode.player.EventMotion;
import pmcp.event.mode.update.EventTick;
import pmcp.event.mode.update.EventUpdate;
import pmcp.event.mode.world.EventWorld;
import pmcp.mode.Mode;
import pmcp.utils.category.Category;
import pmcp.utils.category.MoveCategory;
import pmcp.utils.client.Helper;
import pmcp.utils.client.OldNaming;
import pmcp.utils.item.InventoryUtils;
import pmcp.utils.rotation.Rotation;
import pmcp.utils.rotation.SilentRotation;
import pmcp.utils.time.MSTimer;
import pmcp.value.values.BooleanValue;
import pmcp.value.values.NumberValue;

public class AutoPotion extends Mode {

    public static AutoPotion Instance;

    private final NumberValue<Number> health = new NumberValue<>(this, "Health", 4.5,0.5,20.0,0.5);
    private final NumberValue<Number> delay = new NumberValue<>(this, "Delay", 1,0,10,1);
    private final BooleanValue<Boolean> autoManager = new BooleanValue<>(this, "AutoManager", false);

    public AutoPotion() {
        super("AutoPotion", "自动扔药水", "让你在打pvp的时候快速扔出药水回血", Category.PLAYER);
        Instance = this;
    }
    private final MSTimer timer = new MSTimer();
    public boolean isPotion;
    private int slot;

    @Override
    public void onEnable() {
        reset();
    }

    @EventTarget
    private void onUpdate(EventUpdate eventUpdate) {
        if (mc.player == null || mc.level == null) return;

        if (mc.screen == null) {
            if (!isPotion && timer.hasTimePassed(delay.getValue().intValue() * 100L)) {
                if (mc.player.getHealth() <= health.getValue().floatValue()) {
                    if (InventoryUtils.getPotion_HealingSlot() >= 0) {
                        slot = InventoryUtils.getPotion_HealingSlot();
                        isPotion = true;
                    }
                }
            }
        } else {
            isPotion = false;
        }
    }

    @EventTarget
    private void onMotion(EventMotion eventMotion) {
        if (mc.player == null || mc.level == null) return;

        if (eventMotion.getType() == EventMotion.Type.Pre) {
            if (mc.screen instanceof InventoryScreen) {
                if (autoManager.getValue()) {
                    for (int i = 0; i < 9; i++) {
                        ItemStack stack = mc.player.containerMenu.getSlot(i + 36).getItem();

                        if (stack.isEmpty()) {
                            sendSlotItem(i);
                        }
                    }
                }
            }
        }

        if (eventMotion.getType() == EventMotion.Type.Post) {
            if (isPotion && mc.screen == null && mc.player.onGround()) {
                Rotation oldR = new Rotation(mc.player.getYRot(), mc.player.getXRot());
                mc.player.setYRot(mc.player.getYRot() + 90);
                mc.player.setXRot(90);
                mc.player.connection.sendNoEvent(OldNaming.C09PacketHeldItemChange(slot));
                mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
                mc.player.connection.sendNoEvent(OldNaming.C09PacketHeldItemChange(mc.player.getInventory().selected));
                mc.player.setYRot(oldR.getYaw());
                mc.player.setXRot(oldR.getPitch());
                reset();
            }
        }
    }

    @EventTarget
    private void onWorld(EventWorld eventWorld) {
        reset();
    }

    private void sendSlotItem(int slot) {
        for (int slotIndex = 9; slotIndex < 36; slotIndex++) {
            ItemStack stack = mc.player.containerMenu.getSlot(slotIndex).getItem();

            if (stack.isEmpty() || !stack.getItem().getDescriptionId().equals("item.minecraft.splash_potion")) continue;

            InventoryUtils.putItemInSlot(slotIndex);
        }
    }

    private void reset() {
        timer.reset();
        slot = -1;
        isPotion = false;
    }
}
